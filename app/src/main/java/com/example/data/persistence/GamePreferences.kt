package com.example.data.persistence

import android.content.Context
import android.content.SharedPreferences

class GamePreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("arrows_puzzle_escape_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CURRENT_LEVEL = "key_current_level"
        private const val KEY_HIGHEST_UNLOCKED = "key_highest_unlocked"
        private const val KEY_SOUND_ENABLED = "key_sound_enabled"
        private const val KEY_HAPTICS_ENABLED = "key_haptics_enabled"
        private const val KEY_HINTS_COUNT = "key_hints_count"
        private const val KEY_TOTAL_MOVES = "key_total_moves"
        private const val KEY_TOTAL_SOLVED = "key_total_solved"
        private const val PREFIX_LEVEL_STARS = "key_stars_level_"

        @Volatile
        private var INSTANCE: GamePreferences? = null

        fun getInstance(context: Context): GamePreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: GamePreferences(context).also { INSTANCE = it }
            }
        }
    }

    var currentLevel: Int
        get() = prefs.getInt(KEY_CURRENT_LEVEL, 1)
        set(value) = prefs.edit().putInt(KEY_CURRENT_LEVEL, value).apply()

    var highestUnlockedLevel: Int
        get() = prefs.getInt(KEY_HIGHEST_UNLOCKED, 1)
        set(value) = prefs.edit().putInt(KEY_HIGHEST_UNLOCKED, value).apply()

    var isSoundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_SOUND_ENABLED, value).apply()


    var isHapticsEnabled: Boolean
        get() = prefs.getBoolean(KEY_HAPTICS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTICS_ENABLED, value).apply()

    var hintsAvailable: Int
        get() = prefs.getInt(KEY_HINTS_COUNT, 5)
        set(value) = prefs.edit().putInt(KEY_HINTS_COUNT, value).apply()

    var totalMovesMade: Int
        get() = prefs.getInt(KEY_TOTAL_MOVES, 0)
        set(value) = prefs.edit().putInt(KEY_TOTAL_MOVES, value).apply()

    var totalPuzzlesSolved: Int
        get() = prefs.getInt(KEY_TOTAL_SOLVED, 0)
        set(value) = prefs.edit().putInt(KEY_TOTAL_SOLVED, value).apply()

    fun getStarsForLevel(levelId: Int): Int {
        return prefs.getInt("$PREFIX_LEVEL_STARS$levelId", 0)
    }

    fun isLevelUnlocked(levelId: Int): Boolean {
        return levelId <= highestUnlockedLevel
    }

    fun recordLevelCompletion(levelId: Int, stars: Int) {
        val currentStars = getStarsForLevel(levelId)
        val newStars = maxOf(currentStars, stars.coerceIn(1, 3))

        val editor = prefs.edit()
        editor.putInt("$PREFIX_LEVEL_STARS$levelId", newStars)

        if (levelId >= highestUnlockedLevel) {
            editor.putInt(KEY_HIGHEST_UNLOCKED, levelId + 1)
        }
        editor.putInt(KEY_TOTAL_SOLVED, totalPuzzlesSolved + 1)
        editor.apply()
    }

    fun incrementMoves() {
        totalMovesMade++
    }

    fun consumeHint(): Boolean {
        val current = hintsAvailable
        if (current > 0) {
            hintsAvailable = current - 1
            return true
        }
        return false
    }

    fun addHints(amount: Int = 3) {
        hintsAvailable += amount
    }

    fun resetAllProgress() {
        val editor = prefs.edit()
        editor.putInt(KEY_CURRENT_LEVEL, 1)
        editor.putInt(KEY_HIGHEST_UNLOCKED, 1)
        editor.putInt(KEY_HINTS_COUNT, 5)
        editor.putInt(KEY_TOTAL_MOVES, 0)
        editor.putInt(KEY_TOTAL_SOLVED, 0)

        // Clear all level stars
        for (i in 1..200) {
            editor.remove("$PREFIX_LEVEL_STARS$i")
        }
        editor.apply()
    }
}
