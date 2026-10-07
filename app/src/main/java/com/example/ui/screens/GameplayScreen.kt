package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.persistence.GamePreferences
import com.example.game.engine.GameEngine
import com.example.game.model.GameStatus
import com.example.ui.components.ArrowsBoardView
import com.example.ui.components.GameOverDialog
import com.example.ui.components.HeartsIndicator
import com.example.ui.components.LevelCompleteDialog
import com.example.ui.components.PauseDialog
import com.example.ui.components.PuzzleProgressBar
import com.example.ui.theme.ArrowBackground
import com.example.ui.theme.ArrowHintCyan
import com.example.ui.theme.ArrowNavy
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun GameplayScreen(
    gameEngine: GameEngine,
    preferences: GamePreferences,
    onNavigateHome: () -> Unit,
    onNavigateLevelSelect: () -> Unit,
    onNavigateSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by gameEngine.state.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ArrowBackground)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Pause button
                IconButton(
                    onClick = { gameEngine.pauseGame() },
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("pause_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pause",
                        tint = ArrowNavy
                    )
                }

                // Centered Hearts
                HeartsIndicator(
                    hearts = state.hearts,
                    maxHearts = state.maxHearts
                )

                // Right actions: Restart & Hint
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Restart button
                    IconButton(
                        onClick = { gameEngine.restartLevel() },
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("restart_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Restart Level",
                            tint = TextSecondary
                        )
                    }

                    // Hint button with remaining badge
                    IconButton(
                        onClick = { gameEngine.requestHint() },
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("hint_button")
                    ) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = ArrowNavy,
                                    contentColor = ArrowBackground,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                ) {
                                    Text(
                                        text = "${preferences.hintsAvailable}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = "Use Hint",
                                tint = if (preferences.hintsAvailable > 0) ArrowHintCyan else TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Level title indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Level ${state.level.id}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = "${state.clearedArrowsCount}/${state.totalArrows}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Progress Bar
            PuzzleProgressBar(
                progress = state.progressFraction,
                sessionId = state.sessionId,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Central Interactive Board Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                ArrowsBoardView(
                    gameState = state,
                    onArrowTapped = { arrowId ->
                        gameEngine.onArrowTapped(arrowId)
                    },
                    onEscapeFinished = { arrowId ->
                        gameEngine.finishArrowEscape(arrowId)
                    },
                    onWrongMoveImpact = { arrowId ->
                        gameEngine.onWrongMoveImpact(arrowId)
                    },
                    onWrongMoveFinished = { arrowId ->
                        gameEngine.finishWrongMove(arrowId)
                    }
                )
            }
        }

        // Modals & Overlays
        when (state.status) {
            GameStatus.PAUSED -> {
                PauseDialog(
                    onResume = { gameEngine.resumeGame() },
                    onRestart = { gameEngine.restartLevel() },
                    onLevelSelect = onNavigateLevelSelect,
                    onHome = onNavigateHome
                )
            }

            GameStatus.LEVEL_COMPLETE -> {
                val stars = state.hearts.coerceIn(1, 3)
                LevelCompleteDialog(
                    levelNumber = state.level.id,
                    stars = stars,
                    moves = state.movesCount,
                    onNextLevel = { gameEngine.nextLevel() },
                    onReplay = { gameEngine.restartLevel() },
                    onLevelSelect = onNavigateLevelSelect
                )
            }

            GameStatus.LEVEL_FAILED -> {
                GameOverDialog(
                    levelNumber = state.level.id,
                    onRetry = { gameEngine.restartLevel() },
                    onLevelSelect = onNavigateLevelSelect
                )
            }

            else -> {
                // Nothing extra to display
            }
        }
    }
}
