package com.example.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.data.persistence.GamePreferences

class HapticManager(
    private val context: Context,
    private val preferences: GamePreferences
) {
    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: HapticManager? = null

        fun getInstance(context: Context, preferences: GamePreferences): HapticManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: HapticManager(context, preferences).also { INSTANCE = it }
            }
        }
    }

    fun vibrateTap() {
        if (!preferences.isHapticsEnabled) return
        performVibration(20, 50)
    }

    fun vibrateSuccess() {
        if (!preferences.isHapticsEnabled) return
        performVibration(35, 120)
    }

    fun vibrateBlocked() {
        if (!preferences.isHapticsEnabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 40, 50, 70)
            val amplitudes = intArrayOf(0, 180, 0, 240)
            try {
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } catch (e: Exception) {
                performVibration(80, 200)
            }
        } else {
            performVibration(80, 200)
        }
    }

    fun vibrateLevelComplete() {
        if (!preferences.isHapticsEnabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 30, 40, 50, 40, 90)
            val amplitudes = intArrayOf(0, 100, 0, 160, 0, 255)
            try {
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } catch (e: Exception) {
                performVibration(120, 255)
            }
        } else {
            performVibration(120, 255)
        }
    }

    private fun performVibration(durationMs: Long, amplitude: Int) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val clampedAmp = amplitude.coerceIn(1, 255)
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, clampedAmp))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (e: Exception) {
            // Ignored
        }
    }
}
