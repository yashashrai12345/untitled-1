package com.example.data.repository

import com.example.game.generator.LevelGenerator
import com.example.game.generator.pattern.HandcraftedLevelLibrary
import com.example.game.model.Level
import com.example.game.validation.LevelOverlapValidator

data class LevelSummary(
    val id: Int,
    val name: String,
    val difficulty: Int,
    val arrowCount: Int
)

object LevelRepository {

    const val TOTAL_LEVELS = 100

    private val levelCache = mutableMapOf<Int, Level>()

    /**
     * Retrieves a guaranteed solvable, non-overlapping level by [levelId].
     * Puzzles 1..35 are drawn from the handcrafted pattern library.
     * Puzzles 36..100+ are generated via the procedural template library.
     */
    fun getLevel(levelId: Int): Level {
        val validId = levelId.coerceAtLeast(1)
        levelCache[validId]?.let { return it }

        // Load level via generator (which prioritizes HandcraftedLevelLibrary for 1..35)
        val level = LevelGenerator.generateLevel(validId)
        val validation = LevelOverlapValidator.validateLevel(level)
        val verified = if (validation.isValid) {
            level
        } else {
            // Safe fallback to Level 1
            HandcraftedLevelLibrary.getLevel(1)!!.copy(id = validId, name = "Level $validId")
        }

        levelCache[validId] = verified
        return verified
    }

    /**
     * Returns a summary list of all levels up to [TOTAL_LEVELS].
     */
    fun getLevelSummaries(): List<LevelSummary> {
        return (1..TOTAL_LEVELS).map { id ->
            val lvl = getLevel(id)
            LevelSummary(
                id = id,
                name = lvl.name,
                difficulty = lvl.difficulty,
                arrowCount = lvl.arrows.size
            )
        }
    }
}
