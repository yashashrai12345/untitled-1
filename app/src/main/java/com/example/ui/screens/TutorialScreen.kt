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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ArrowBackground
import com.example.ui.theme.ArrowBackgroundAlt
import com.example.ui.theme.ArrowBlockedPink
import com.example.ui.theme.ArrowNavy
import com.example.ui.theme.HeartActive
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TutorialScreen(
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
                    modifier = Modifier.testTag("tutorial_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = ArrowNavy
                    )
                }

                Text(
                    text = "How to Play",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Step 1: Tap to Move
            TutorialStepCard(
                stepNumber = "1",
                title = "Tap an Arrow",
                description = "Each arrow has a starting path, corner bends, and one directional arrowhead. Tap any part of its line to select it.",
                icon = Icons.Default.Check,
                iconColor = ArrowNavy
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Step 2: Clear Movement Corridor
            TutorialStepCard(
                stepNumber = "2",
                title = "Check the Exit Path",
                description = "An arrow can leave the board only if the space immediately ahead of its arrowhead all the way to the boundary is empty.",
                icon = Icons.Default.Check,
                iconColor = Color(0xFF10B981)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Step 3: Blocked moves
            TutorialStepCard(
                stepNumber = "3",
                title = "Avoid Blockades",
                description = "If another arrow's path obstructs the corridor, the arrow will react in coral pink and you will lose 1 heart!",
                icon = Icons.Default.Close,
                iconColor = ArrowBlockedPink
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Step 4: Hearts & Win Condition
            TutorialStepCard(
                stepNumber = "4",
                title = "Clear All Arrows",
                description = "You start with 3 hearts. Deduce the correct order of departures so arrows unlock one another. Remove all arrows to complete the puzzle!",
                icon = Icons.Default.Favorite,
                iconColor = HeartActive
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TutorialStepCard(
    stepNumber: String,
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ArrowBackgroundAlt),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconColor.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Step $stepNumber: $title",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 13.5.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
