package com.example.data.repository

import com.example.data.repository.custom.CustomLevelRepository
import com.example.game.generator.LevelGenerator
import com.example.game.generator.pattern.HandcraftedLevelLibrary
import com.example.game.model.Board
import com.example.game.model.Level
import com.example.game.validation.LevelOverlapValidator

data class LevelSummary(
    val id: Int,
    val name: String,
    val difficulty: Int,
    val arrowCount: Int
)

object LevelRepository {

    private val levelCache = mutableMapOf<Int, Level>()

    val totalLevelsCount: Int
        get() {
            val customCount = CustomLevelRepository.customLevels.value.size
            return if (customCount > 0) customCount else 100
        }

    /**
     * Retrieves level by [levelId].
     * If published custom levels exist from Admin Panel, custom level 1 maps to Level 1,
     * custom level 2 maps to Level 2, etc.
     */
    fun getLevel(levelId: Int): Level {
        val validId = levelId.coerceAtLeast(1)

        val customList = CustomLevelRepository.customLevels.value
        if (customList.isNotEmpty()) {
            val customIdx = (validId - 1).coerceIn(0, customList.size - 1)
            val customLvl = customList[customIdx]
            return customLvl.copy(id = validId)
        }

        levelCache[validId]?.let { return it }

        // Fallback generator if no custom levels are published yet
        val level = LevelGenerator.generateLevel(validId)
        val validation = LevelOverlapValidator.validateLevel(level)
        val verified = if (validation.isValid) {
            level
        } else {
            HandcraftedLevelLibrary.getLevel(1)?.copy(id = validId, name = "Level $validId")
                ?: Level(
                    id = validId,
                    name = "Level $validId",
                    board = Board(6, 6),
                    arrows = emptyList()
                )
        }

        levelCache[validId] = verified
        return verified
    }

    /**
     * Returns a summary list of all available levels.
     */
    fun getLevelSummaries(): List<LevelSummary> {
        val total = totalLevelsCount
        return (1..total).map { id ->
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
