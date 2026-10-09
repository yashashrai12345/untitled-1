package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.persistence.GamePreferences
import com.example.data.repository.LevelRepository
import com.example.ui.theme.ArrowBackground
import com.example.ui.theme.ArrowBackgroundAlt
import com.example.ui.theme.ArrowNavy
import com.example.ui.theme.HeartInactive
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun LevelSelectScreen(
    preferences: GamePreferences,
    onLevelSelected: (Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalLevels = LevelRepository.totalLevelsCount
    val highestUnlocked = preferences.highestUnlockedLevel
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
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("level_select_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = ArrowNavy
                    )
                }

                Text(
                    text = "Select Level",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            if (totalLevels == 0) {
                // Clean Empty State Screen
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = ArrowBackgroundAlt),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = ArrowNavy,
                                modifier = Modifier.size(48.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "No Levels Available",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "No published levels available yet. Please check back later or publish levels from the Admin Panel!",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                // Grid of Published Levels (Level 1, Level 2, Level 3...)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    contentPadding = PaddingValues(vertical = 12.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("level_grid")
                ) {
                    items(totalLevels) { index ->
                        val levelId = index + 1
                        val isUnlocked = levelId <= highestUnlocked
                        val isCurrent = levelId == currentLevel
                        val stars = preferences.getStarsForLevel(levelId)

                        LevelCardItem(
                            levelId = levelId,
                            displayName = "$levelId",
                            isUnlocked = isUnlocked,
                            isCurrent = isCurrent,
                            stars = stars,
                            onClick = {
                                if (isUnlocked) {
                                    onLevelSelected(levelId)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LevelCardItem(
    levelId: Int,
    displayName: String,
    isUnlocked: Boolean,
    isCurrent: Boolean,
    stars: Int,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        !isUnlocked -> Color(0xFFF1F5F9)
        isCurrent -> ArrowNavy
        else -> ArrowBackgroundAlt
    }
    val contentColor = when {
        !isUnlocked -> Color(0xFF94A3B8)
        isCurrent -> Color.White
        else -> TextPrimary
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrent) 4.dp else 0.dp),
        modifier = Modifier
            .size(72.dp)
            .clickable(enabled = isUnlocked, onClick = onClick)
            .testTag("level_card_$levelId")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!isUnlocked) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = displayName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Stars row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(1.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..3) {
                        val isStarEarned = i <= stars
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (isStarEarned) Color(0xFFFBBF24) else {
                                if (isCurrent) Color.White.copy(alpha = 0.3f) else HeartInactive
                            },
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
            }
        }
    }
}
