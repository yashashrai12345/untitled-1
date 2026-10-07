package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ArrowBackground
import com.example.ui.theme.ArrowNavy
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Level introduction screen inspired by the reference layout:
 * Centered title "Arrows", "Level X", and large rounded "Play" button.
 */
@Composable
fun LevelIntroScreen(
    levelNumber: Int,
    onPlay: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ArrowBackground)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        // Back navigation button
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(16.dp)
                .size(48.dp)
                .align(Alignment.TopStart)
                .testTag("intro_back_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = ArrowNavy
            )
        }

        // Center visual hierarchy
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Elegant title
            Text(
                text = "ARROWS",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Subtitle
            Text(
                text = "Level $levelNumber",
                fontSize = 20.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondary,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(56.dp))

            // Large rounded Play button
            Button(
                onClick = onPlay,
                shape = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ArrowNavy),
                modifier = Modifier
                    .fillMaxWidth(0.65f)
                    .height(60.dp)
                    .testTag("intro_play_button")
            ) {
                Text(
                    text = "PLAY",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ArrowBackground,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}
