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
        val milestoneLevels = listOf(1, 5, 10, 15, 20, 25, 30, 35, 36, 45, 55, 65, 75, 85, 95, 100)

        for (levelId in milestoneLevels) {
            val level = LevelRepository.getLevel(levelId)
            assertNotNull("Level $levelId must be loadable", level)
            assertEquals("Level id must match requested id", levelId, level.id)

            val validation = LevelOverlapValidator.validateLevel(level)

            // Verify requested difficulty progression curve: strictly increasing arrow count
            when (levelId) {
                1 -> assertTrue("Level 1 must start with 10 arrows (Medium difficulty), had ${level.arrows.size}", level.arrows.size == 10)
                5 -> assertTrue("Level 5 must have 18 arrows, had ${level.arrows.size}", level.arrows.size == 18)
                10 -> assertTrue("Level 10 must have 34 arrows, had ${level.arrows.size}", level.arrows.size == 34)
                15 -> assertTrue("Level 15 must have 56 arrows, had ${level.arrows.size}", level.arrows.size == 56)
            }

            assertTrue(
                "Level $levelId failed validation: ${validation.reason}",
                validation.isValid
            )
        }

        // Verify strictly increasing arrow count for handcrafted levels 1..15
        var previousCount = 0
        for (lvl in 1..15) {
            val level = LevelRepository.getLevel(lvl)!!
            assertTrue("Level $lvl arrows (${level.arrows.size}) must be > previous ($previousCount)", level.arrows.size > previousCount)
            previousCount = level.arrows.size
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
