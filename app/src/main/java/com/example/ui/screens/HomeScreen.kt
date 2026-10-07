package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.persistence.GamePreferences
import com.example.ui.theme.ArrowBackground
import com.example.ui.theme.ArrowBackgroundAlt
import com.example.ui.theme.ArrowBlockedPink
import com.example.ui.theme.ArrowNavy
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    preferences: GamePreferences,
    onPlayClicked: (Int) -> Unit,
    onLevelSelectClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    onTutorialClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentLevel = preferences.currentLevel

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ArrowBackground)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Settings Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onTutorialClicked,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("home_tutorial_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                        contentDescription = "How to Play",
                        modifier = Modifier.size(18.dp),
                        tint = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Guide", fontSize = 13.sp, color = TextPrimary)
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = onSettingsClicked,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("home_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        modifier = Modifier.size(18.dp),
                        tint = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Settings", fontSize = 13.sp, color = TextPrimary)
                }
            }

            // Center Hero Branding
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Vector Geometric Logo
                OriginalGameLogo(
                    modifier = Modifier.size(100.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Arrows",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "PUZZLE ESCAPE",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary,
                    letterSpacing = 3.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Stats overview pill
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ArrowBackgroundAlt),
                    modifier = Modifier.fillMaxWidth(0.85f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${preferences.totalPuzzlesSolved}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Solved",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .height(28.dp)
                                .width(1.dp)
                                .background(Color(0xFFCBD5E1))
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Lvl ${preferences.highestUnlockedLevel}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Unlocked",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // Bottom Actions
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Primary Play Button
                Button(
                    onClick = { onPlayClicked(currentLevel) },
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ArrowNavy),
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(58.dp)
                        .testTag("home_play_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Play Level $currentLevel",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Level Select Button
                OutlinedButton(
                    onClick = onLevelSelectClicked,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(52.dp)
                        .testTag("home_level_select_button")
                ) {
                    Text(
                        text = "Level Select",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

/**
 * Custom minimalist vector logo for Arrows – Puzzle Escape.
 */
@Composable
fun OriginalGameLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Dark navy arrow turning and pointing right
        val path1 = Path().apply {
            moveTo(w * 0.25f, h * 0.72f)
            lineTo(w * 0.25f, h * 0.35f)
            lineTo(w * 0.65f, h * 0.35f)
        }
        drawPath(
            path = path1,
            color = ArrowNavy,
            style = Stroke(width = 12f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        // Arrowhead 1
        val head1 = Path().apply {
            moveTo(w * 0.80f, h * 0.35f)
            lineTo(w * 0.62f, h * 0.23f)
            lineTo(w * 0.66f, h * 0.35f)
            lineTo(w * 0.62f, h * 0.47f)
            close()
        }
        drawPath(path = head1, color = ArrowNavy, style = Fill)

        // Coral pink arrow turning and escaping up
        val path2 = Path().apply {
            moveTo(w * 0.75f, h * 0.72f)
            lineTo(w * 0.50f, h * 0.72f)
            lineTo(w * 0.50f, h * 0.48f)
        }
        drawPath(
            path = path2,
            color = ArrowBlockedPink,
            style = Stroke(width = 12f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        // Arrowhead 2
        val head2 = Path().apply {
            moveTo(w * 0.50f, h * 0.33f)
            lineTo(w * 0.38f, h * 0.51f)
            lineTo(w * 0.50f, h * 0.47f)
            lineTo(w * 0.62f, h * 0.51f)
            close()
        }
        drawPath(path = head2, color = ArrowBlockedPink, style = Fill)
    }
}
