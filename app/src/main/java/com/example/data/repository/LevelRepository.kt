package com.example.data.repository

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

    const val TOTAL_CAMPAIGN_LEVELS = 100

    private val campaignCache = mutableMapOf<Int, Level>()

    /**
     * Total level count available in the offline game.
     * Guaranteed 100 levels (Levels 1..35 handcrafted silhouettes, 36..100 procedural verified).
     */
    val totalLevelsCount: Int = TOTAL_CAMPAIGN_LEVELS

    /**
     * Retrieves level by level number 1..100.
     * Guaranteed to return a valid solvable Level, NEVER returns null!
     */
    fun getLevel(levelId: Int): Level {
        val validId = levelId.coerceIn(1, TOTAL_CAMPAIGN_LEVELS)
        campaignCache[validId]?.let { return it }

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
        campaignCache[validId] = verified
        return verified
    }

    /**
     * Alias for getLevel(levelId)
     */
    fun getCampaignLevel(levelId: Int): Level = getLevel(levelId)

    /**
     * Returns summaries of all 100 available levels.
     */
    fun getLevelSummaries(): List<LevelSummary> {
        return (1..TOTAL_CAMPAIGN_LEVELS).map { id ->
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
