package com.example

import com.example.data.repository.LevelRepository
import com.example.game.generator.LevelGenerator
import com.example.game.validation.LevelOverlapValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LevelProgressionValidationTest {

    @Test
    fun testKeyProgressionMilestonesAreValidAndSolvable() {
        val milestoneLevels = listOf(1, 5, 10, 15, 20, 25)

        for (levelId in milestoneLevels) {
            val level = LevelGenerator.generateLevel(levelId)
            assertNotNull("Level $levelId must be loadable", level)
            assertEquals("Level id must match requested id", levelId, level.id)

            val validation = LevelOverlapValidator.validateLevel(level)
            assertTrue(
                "Generated Level $levelId failed validation: ${validation.reason}",
                validation.isValid
            )
        }
    }

    @Test
    fun testLevelGenerationIsDeterministic() {
        for (levelId in listOf(1, 10, 35, 42, 77, 100)) {
            val run1 = LevelGenerator.generateLevel(levelId)
            val run2 = LevelGenerator.generateLevel(levelId)

            assertEquals("Arrow counts must match", run1.arrows.size, run2.arrows.size)
            for (i in run1.arrows.indices) {
                val a1 = run1.arrows[i]
                val a2 = run2.arrows[i]
                assertEquals("Arrow id must match", a1.id, a2.id)
                assertEquals("Arrow direction must match", a1.direction, a2.direction)
                assertEquals("Arrow points must match", a1.points, a2.points)
            }
        }
    }
}
