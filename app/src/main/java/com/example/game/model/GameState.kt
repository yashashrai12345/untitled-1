package com.example.game.model

/**
 * High-level status of the game session.
 */
enum class GameStatus {
    INTRO,
    PLAYING,
    PAUSED,
    LEVEL_COMPLETE,
    LEVEL_FAILED
}

/**
 * Explicit movement states for an arrow during animation or interaction.
 */
enum class ArrowMovementPhase {
    IDLE,
    MOVING_SUCCESS,
    MOVING_WRONG,
    IMPACT,
    RETURNING,
    REMOVED
}

/**
 * Represents an arrow currently in flight out of the board.
 */
data class EscapingArrow(
    val arrow: Arrow,
    val progress: Float = 0f // 0f (at board position) to 1f (fully exited)
)

/**
 * Represents an arrow attempting a wrong/blocked move.
 * Tracks the arrow, dynamic collision distance to the obstacle, and blocker.
 */
data class WrongMoveState(
    val arrow: Arrow,
    val collisionDistance: Float, // distance in grid units to collision point
    val blockerArrowId: String? = null
)

/**
 * Immutable representation of the puzzle state during a game attempt.
 */
data class GameState(
    val sessionId: Long = 0L,
    val level: Level,
    val activeArrows: List<Arrow> = level.arrows,
    val removedArrowIds: Set<String> = emptySet(),
    val hearts: Int = 3,
    val maxHearts: Int = 3,
    val status: GameStatus = GameStatus.PLAYING,
    val movesCount: Int = 0,
    val escapingArrow: EscapingArrow? = null,
    val wrongMoveArrow: WrongMoveState? = null,
    val blockedArrowId: String? = null,
    val hintArrowId: String? = null,
    val lastErrorTimestamp: Long = 0L
) {
    val isSolved: Boolean get() = activeArrows.isEmpty() && escapingArrow == null
    val totalArrows: Int get() = level.arrows.size
    val clearedArrowsCount: Int
        get() = (removedArrowIds.size + if (escapingArrow != null) 1 else 0).coerceAtMost(totalArrows)
    val progressFraction: Float
        get() = if (totalArrows > 0) clearedArrowsCount.toFloat() / totalArrows.toFloat() else 0f
}

