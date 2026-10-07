package com.example.game.engine

import com.example.audio.SoundManager
import com.example.data.persistence.GamePreferences
import com.example.data.repository.LevelRepository
import com.example.game.collision.CollisionDetector
import com.example.game.model.EscapingArrow
import com.example.game.model.GameState
import com.example.game.model.GameStatus
import com.example.game.model.Level
import com.example.game.solver.HintEngine
import com.example.haptics.HapticManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameEngine(
    private val preferences: GamePreferences,
    private val soundManager: SoundManager,
    private val hapticManager: HapticManager,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    private var sessionCounter = 1L

    private val _state = MutableStateFlow(
        GameState(sessionId = sessionCounter++, level = LevelRepository.getLevel(preferences.currentLevel))
    )
    val state: StateFlow<GameState> = _state.asStateFlow()

    private var animationJob: Job? = null
    private var wrongMoveJob: Job? = null
    private var blockedResetJob: Job? = null
    private var hintResetJob: Job? = null

    init {
        loadLevel(preferences.currentLevel)
    }

    /**
     * Loads a specific level and resets the play session.
     */
    fun loadLevel(levelId: Int) {
        animationJob?.cancel()
        wrongMoveJob?.cancel()
        blockedResetJob?.cancel()
        hintResetJob?.cancel()

        val level = LevelRepository.getLevel(levelId)
        preferences.currentLevel = levelId

        _state.value = GameState(
            sessionId = sessionCounter++,
            level = level,
            activeArrows = level.arrows,
            removedArrowIds = emptySet(),
            hearts = 3,
            maxHearts = 3,
            status = GameStatus.PLAYING,
            movesCount = 0,
            escapingArrow = null,
            wrongMoveArrow = null,
            blockedArrowId = null,
            hintArrowId = null
        )
    }

    /**
     * Restarts the current active level.
     */
    fun restartLevel() {
        loadLevel(_state.value.level.id)
    }

    /**
     * Proceeds to the subsequent level.
     */
    fun nextLevel() {
        val nextId = _state.value.level.id + 1
        loadLevel(nextId)
    }

    /**
     * Handles tapping an arrow on the board.
     * Starts arrow escape or wrong move immediately with zero latency.
     */
    fun onArrowTapped(arrowId: String) {
        val current = _state.value
        if (current.status != GameStatus.PLAYING) return
        if (current.escapingArrow != null) return // Ignore while an arrow is actively moving
        if (current.wrongMoveArrow != null) return // Ignore while wrong move animation is active
        if (current.blockedArrowId != null) return // Locked until returned

        val arrow = current.activeArrows.find { it.id == arrowId } ?: return

        // Dispatch persistent preferences off the critical touch-to-render path
        scope.launch(Dispatchers.IO) {
            preferences.incrementMoves()
        }

        val canEscape = CollisionDetector.canArrowEscape(arrow, current.activeArrows, current.level.board)

        if (canEscape) {
            handleArrowEscape(arrow)
        } else {
            handleArrowWrongMove(arrow)
        }
    }

    private fun handleArrowEscape(arrow: com.example.game.model.Arrow) {
        // 1. Immediately update state so ArrowBoardView starts snake movement on the exact same frame
        _state.update {
            it.copy(
                escapingArrow = EscapingArrow(arrow = arrow, progress = 0f),
                hintArrowId = if (it.hintArrowId == arrow.id) null else it.hintArrowId,
                movesCount = it.movesCount + 1
            )
        }

        // 2. Play audio & haptics non-blockingly
        soundManager.playArrowEscape()
        hapticManager.vibrateSuccess()

        // 3. Safety fallback job: auto-completes escape if UI animation callback was missed or in headless tests
        animationJob?.cancel()
        animationJob = scope.launch {
            delay(2000L)
            finishArrowEscape(arrow.id)
        }
    }

    /**
     * Called when the snake-like escape animation has fully traversed and exited the board.
     * Completes arrow removal and checks for puzzle solution.
     */
    fun finishArrowEscape(arrowId: String) {
        animationJob?.cancel()
        val current = _state.value
        val escaping = current.escapingArrow
        if (escaping == null || escaping.arrow.id != arrowId) {
            // Already handled or not the current escaping arrow
            return
        }

        // Arrow fully exited: remove from active set
        val remainingAfterRemoval = current.activeArrows.filter { it.id != arrowId }
        val removedIds = current.removedArrowIds + arrowId

        if (remainingAfterRemoval.isEmpty()) {
            // Victory! Level Complete!
            val heartsRemaining = current.hearts
            val stars = heartsRemaining.coerceIn(1, 3)
            preferences.recordLevelCompletion(current.level.id, stars)

            soundManager.playLevelComplete()
            hapticManager.vibrateLevelComplete()

            _state.update {
                it.copy(
                    activeArrows = emptyList(),
                    removedArrowIds = removedIds,
                    escapingArrow = null,
                    status = GameStatus.LEVEL_COMPLETE
                )
            }
        } else {
            _state.update {
                it.copy(
                    activeArrows = remainingAfterRemoval,
                    removedArrowIds = removedIds,
                    escapingArrow = null
                )
            }
        }
    }

    private fun handleArrowWrongMove(arrow: com.example.game.model.Arrow) {
        val current = _state.value
        val collisionDist = CollisionDetector.calculateCollisionDistance(arrow, current.activeArrows, current.level.board)
        val blocker = CollisionDetector.findBlockingArrow(arrow, current.activeArrows, current.level.board)

        _state.update {
            it.copy(
                wrongMoveArrow = com.example.game.model.WrongMoveState(
                    arrow = arrow,
                    collisionDistance = collisionDist,
                    blockerArrowId = blocker?.id
                ),
                blockedArrowId = arrow.id,
                movesCount = it.movesCount + 1
            )
        }

        // Safety fallback job for headless tests: triggers impact then completes return
        wrongMoveJob?.cancel()
        wrongMoveJob = scope.launch {
            delay(1200L)
            onWrongMoveImpact(arrow.id)
            delay(400L)
            finishWrongMove(arrow.id)
        }
    }

    /**
     * Triggered at the exact moment the wrong arrow impacts the blocking obstacle.
     * Plays error sound and haptics, and deducts exactly ONE heart.
     */
    fun onWrongMoveImpact(arrowId: String) {
        val current = _state.value
        val wrongMove = current.wrongMoveArrow
        if (wrongMove == null || wrongMove.arrow.id != arrowId) return

        soundManager.playBlocked()
        hapticManager.vibrateBlocked()

        // Deduct exactly 1 heart AT IMPACT
        val newHearts = (current.hearts - 1).coerceAtLeast(0)

        _state.update {
            it.copy(
                hearts = newHearts,
                lastErrorTimestamp = System.currentTimeMillis()
            )
        }

        if (newHearts == 0) {
            soundManager.playLifeLost()
        }
    }

    /**
     * Triggered when the wrong arrow has smoothly reversed along its path back to its
     * exact original position, restored its original navy color, and returns to IDLE.
     */
    fun finishWrongMove(arrowId: String) {
        wrongMoveJob?.cancel()
        val current = _state.value
        val wrongMove = current.wrongMoveArrow
        if (wrongMove == null || wrongMove.arrow.id != arrowId) return

        val isGameOver = current.hearts <= 0

        _state.update {
            it.copy(
                wrongMoveArrow = null,
                blockedArrowId = null,
                status = if (isGameOver) GameStatus.LEVEL_FAILED else it.status
            )
        }
    }

    private fun handleArrowBlocked(arrowId: String, timestamp: Long) {
        // Kept for backward compatibility if invoked directly
        val arrow = _state.value.activeArrows.find { it.id == arrowId }
        if (arrow != null) {
            handleArrowWrongMove(arrow)
        }
    }

    /**
     * Requests a hint to highlight a valid move.
     */
    fun requestHint(): Boolean {
        val current = _state.value
        if (current.status != GameStatus.PLAYING) return false
        if (current.activeArrows.isEmpty()) return false

        if (!preferences.consumeHint()) {
            return false
        }

        val hint = HintEngine.getNextHint(current.activeArrows, current.level.board)
        if (hint.arrowId != null) {
            soundManager.playHint()
            hapticManager.vibrateTap()

            _state.update { it.copy(hintArrowId = hint.arrowId) }

            hintResetJob?.cancel()
            hintResetJob = scope.launch {
                delay(3500L)
                _state.update {
                    if (it.hintArrowId == hint.arrowId) it.copy(hintArrowId = null) else it
                }
            }
            return true
        }
        return false
    }

    fun pauseGame() {
        if (_state.value.status == GameStatus.PLAYING) {
            _state.update { it.copy(status = GameStatus.PAUSED) }
        }
    }

    fun resumeGame() {
        if (_state.value.status == GameStatus.PAUSED) {
            _state.update { it.copy(status = GameStatus.PLAYING) }
        }
    }
}
