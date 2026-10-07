package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.audio.SoundManager
import com.example.data.persistence.GamePreferences
import com.example.game.engine.GameEngine
import com.example.haptics.HapticManager
import com.example.ui.screens.GameplayScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LevelIntroScreen
import com.example.ui.screens.LevelSelectScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TutorialScreen

sealed interface Screen {
    data object Home : Screen
    data class LevelIntro(val levelId: Int) : Screen
    data class Gameplay(val levelId: Int) : Screen
    data object LevelSelect : Screen
    data object Settings : Screen
    data object Tutorial : Screen
}

@Composable
fun ArrowsAppRoot(
    preferences: GamePreferences,
    soundManager: SoundManager,
    hapticManager: HapticManager,
    gameEngine: GameEngine,
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    Crossfade(targetState = currentScreen, label = "screenTransition") { screen ->
        when (screen) {
            is Screen.Home -> {
                HomeScreen(
                    preferences = preferences,
                    onPlayClicked = { levelId ->
                        currentScreen = Screen.LevelIntro(levelId)
                    },
                    onLevelSelectClicked = {
                        currentScreen = Screen.LevelSelect
                    },
                    onSettingsClicked = {
                        currentScreen = Screen.Settings
                    },
                    onTutorialClicked = {
                        currentScreen = Screen.Tutorial
                    }
                )
            }

            is Screen.LevelIntro -> {
                BackHandler { currentScreen = Screen.Home }
                LevelIntroScreen(
                    levelNumber = screen.levelId,
                    onPlay = {
                        gameEngine.loadLevel(screen.levelId)
                        currentScreen = Screen.Gameplay(screen.levelId)
                    },
                    onBack = { currentScreen = Screen.Home }
                )
            }

            is Screen.Gameplay -> {
                BackHandler {
                    gameEngine.pauseGame()
                }
                GameplayScreen(
                    gameEngine = gameEngine,
                    preferences = preferences,
                    onNavigateHome = { currentScreen = Screen.Home },
                    onNavigateLevelSelect = { currentScreen = Screen.LevelSelect },
                    onNavigateSettings = { currentScreen = Screen.Settings }
                )
            }

            is Screen.LevelSelect -> {
                BackHandler { currentScreen = Screen.Home }
                LevelSelectScreen(
                    preferences = preferences,
                    onLevelSelected = { selectedLevelId ->
                        currentScreen = Screen.LevelIntro(selectedLevelId)
                    },
                    onBack = { currentScreen = Screen.Home }
                )
            }

            is Screen.Settings -> {
                BackHandler { currentScreen = Screen.Home }
                SettingsScreen(
                    preferences = preferences,
                    soundManager = soundManager,
                    onBack = { currentScreen = Screen.Home },
                    onNavigateTutorial = { currentScreen = Screen.Tutorial }
                )
            }

            is Screen.Tutorial -> {
                BackHandler { currentScreen = Screen.Home }
                TutorialScreen(
                    onBack = { currentScreen = Screen.Home }
                )
            }
        }
    }
}
