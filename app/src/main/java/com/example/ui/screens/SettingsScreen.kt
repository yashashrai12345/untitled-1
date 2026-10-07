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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundManager
import com.example.data.persistence.GamePreferences
import com.example.ui.theme.ArrowBackground
import com.example.ui.theme.ArrowBackgroundAlt
import com.example.ui.theme.ArrowBlockedPink
import com.example.ui.theme.ArrowNavy
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    preferences: GamePreferences,
    soundManager: SoundManager,
    onBack: () -> Unit,
    onNavigateTutorial: () -> Unit,
    modifier: Modifier = Modifier
) {
    var soundEnabled by remember { mutableStateOf(preferences.isSoundEnabled) }
    var musicEnabled by remember { mutableStateOf(preferences.isMusicEnabled) }
    var hapticsEnabled by remember { mutableStateOf(preferences.isHapticsEnabled) }
    var showResetDialog by remember { mutableStateOf(false) }

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
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("settings_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = ArrowNavy
                    )
                }

                Text(
                    text = "Settings",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Audio & Feedback Card
            Text(
                text = "AUDIO & FEEDBACK",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
            )

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = ArrowBackgroundAlt),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    // Sound FX
                    SettingsToggleRow(
                        title = "Sound Effects",
                        subtitle = "Taps, chimes and buzzers",
                        checked = soundEnabled,
                        testTag = "toggle_sound",
                        onCheckedChange = {
                            soundEnabled = it
                            preferences.isSoundEnabled = it
                            if (it) soundManager.playTap()
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Ambient Music
                    SettingsToggleRow(
                        title = "Ambient Music",
                        subtitle = "Calming meditative drone loops",
                        checked = musicEnabled,
                        testTag = "toggle_music",
                        onCheckedChange = {
                            musicEnabled = it
                            preferences.isMusicEnabled = it
                            soundManager.updateMusicState()
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Haptics
                    SettingsToggleRow(
                        title = "Vibration Feedback",
                        subtitle = "Haptic bumps on movement & blocks",
                        checked = hapticsEnabled,
                        testTag = "toggle_haptics",
                        onCheckedChange = {
                            hapticsEnabled = it
                            preferences.isHapticsEnabled = it
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Help & Guides Card
            Text(
                text = "GAMEPLAY",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
            )

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = ArrowBackgroundAlt),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedButton(
                        onClick = onNavigateTutorial,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("settings_how_to_play_button")
                    ) {
                        Text("How to Play (Guide)", color = TextPrimary, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Danger Zone Card
            Text(
                text = "PROGRESSION",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
            )

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = ArrowBackgroundAlt),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = { showResetDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ArrowBlockedPink.copy(alpha = 0.12f),
                            contentColor = ArrowBlockedPink
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("reset_progress_button")
                    ) {
                        Text("Reset All Game Progress", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // About section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Arrows – Puzzle Escape",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Version 1.0 • 100% Offline",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        // Confirmation dialog for Reset Progress
        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = {
                    Text(text = "Reset Progress?", fontWeight = FontWeight.Bold)
                },
                text = {
                    Text("This will restore level progress to Level 1 and reset all unlocked stars. This cannot be undone.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            preferences.resetAllProgress()
                            showResetDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ArrowBlockedPink),
                        modifier = Modifier.testTag("confirm_reset_button")
                    ) {
                        Text("Reset", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text("Cancel", color = TextPrimary)
                    }
                }
            )
        }
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    testTag: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(text = subtitle, fontSize = 12.sp, color = TextSecondary)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = ArrowNavy,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFCBD5E1)
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}
