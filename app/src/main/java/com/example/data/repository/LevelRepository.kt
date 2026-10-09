package com.example.data.repository

import com.example.data.repository.custom.CustomLevelRepository
import com.example.game.model.Level

data class LevelSummary(
    val id: Int,
    val name: String,
    val difficulty: Int,
    val arrowCount: Int
)

object LevelRepository {

    /**
     * Total count of published custom levels from Admin Panel.
     */
    val totalLevelsCount: Int
        get() = CustomLevelRepository.customLevels.value.size

    /**
     * Retrieves level by [levelId] (1-based index 1..totalLevelsCount).
     * Returns null if no published custom levels exist or if levelId is out of bounds.
     */
    fun getLevel(levelId: Int): Level? {
        val customList = CustomLevelRepository.customLevels.value
        if (customList.isEmpty()) return null

        val validId = levelId.coerceIn(1, customList.size)
        val customLvl = customList[validId - 1]
        return customLvl.copy(id = validId, name = customLvl.name.ifEmpty { "Level $validId" })
    }

    /**
     * Returns a summary list of all available published levels.
     */
    fun getLevelSummaries(): List<LevelSummary> {
        val total = totalLevelsCount
        return (1..total).mapNotNull { id ->
            val lvl = getLevel(id)
            if (lvl != null) {
                LevelSummary(
                    id = id,
                    name = lvl.name,
                    difficulty = lvl.difficulty,
                    arrowCount = lvl.arrows.size
                )
            } else null
        }
    }
}
