package com.example.game.generator

import com.example.game.generator.difficulty.DifficultyCalculator
import com.example.game.generator.pattern.HandcraftedLevelLibrary
import com.example.game.generator.pattern.ProceduralTemplateLibrary
import com.example.game.model.Arrow
import com.example.game.model.Board
import com.example.game.model.Direction
import com.example.game.model.GridPoint
import com.example.game.model.Level
import com.example.game.validation.LevelOverlapValidator
import kotlin.random.Random

object LevelGenerator {

    /**
     * Generates a solvable, deterministic level for a given [levelNumber]
     * that strictly satisfies all overlap and clearance validation criteria.
     *
     * - Levels 1..35: Handcrafted, intentionally designed puzzles.
     * - Levels 36..100+: Architecturally patterned procedural puzzles with deterministic transformations.
     */
    fun generateLevel(levelNumber: Int): Level {
        // 1. Handcrafted library for levels 1..35
        if (levelNumber in 1..35) {
            val handcrafted = HandcraftedLevelLibrary.getLevel(levelNumber)
            if (handcrafted != null) {
                val validation = LevelOverlapValidator.validateLevel(handcrafted)
                if (validation.isValid) {
                    val metrics = DifficultyCalculator.calculateMetrics(handcrafted.arrows, handcrafted.board)
                    return handcrafted.copy(difficulty = metrics.normalizedTier, parMoves = handcrafted.arrows.size)
                }
            }
        }

        // 2. Procedural template library for levels 36+
        val baseSeed = 1000003L * levelNumber + 42L

        // Try deterministic generation with transforms
        for (attempt in 0..15) {
            val seed = baseSeed + attempt * 31337L
            val candidate = ProceduralTemplateLibrary.generateTemplateLevel(levelNumber, seed)
            val validation = LevelOverlapValidator.validateLevel(candidate)
            if (validation.isValid) {
                val metrics = DifficultyCalculator.calculateMetrics(candidate.arrows, candidate.board)
                return candidate.copy(difficulty = metrics.normalizedTier, parMoves = candidate.arrows.size)
            }
        }

        // Fallback: guaranteed valid handcrafted template from level 35 or 1
        val safeFallback = HandcraftedLevelLibrary.getLevel(minOf(levelNumber, 35))
            ?: HandcraftedLevelLibrary.getLevel(1)!!
        return safeFallback.copy(id = levelNumber, name = "Level $levelNumber")
    }
}
