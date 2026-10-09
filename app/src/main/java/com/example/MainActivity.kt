package com.example

import android.os.Bundle

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier

import com.example.audio.SoundManager
import com.example.data.persistence.GamePreferences
import com.example.game.engine.GameEngine
import com.example.haptics.HapticManager
import com.example.ui.navigation.ArrowsAppRoot
import com.example.ui.theme.ArrowsTheme

class MainActivity : ComponentActivity() {

    private lateinit var preferences: GamePreferences
    private lateinit var soundManager: SoundManager
    private lateinit var hapticManager: HapticManager
    private lateinit var gameEngine: GameEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TEST: Load Tiled level


        enableEdgeToEdge()

        preferences = GamePreferences.getInstance(applicationContext)
        soundManager = SoundManager.getInstance(preferences)
        hapticManager = HapticManager.getInstance(
            applicationContext,
            preferences
        )

        gameEngine = GameEngine(
            preferences,
            soundManager,
            hapticManager
        )

        setContent {
            ArrowsTheme {
                ArrowsAppRoot(
                    preferences = preferences,
                    soundManager = soundManager,
                    hapticManager = hapticManager,
                    gameEngine = gameEngine,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}