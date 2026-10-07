package com.example

import com.example.game.generator.pattern.HandcraftedLevelLibrary
import com.example.game.validation.LevelOverlapValidator
import org.junit.Assert.assertTrue
import org.junit.Test

class HandcraftedLevelsValidationTest {

    @Test
    fun testAllHandcraftedLevelsAreValidAndSolvable() {
        val failures = mutableListOf<String>()
        for (id in 1..35) {
            val level = HandcraftedLevelLibrary.getLevel(id)
            if (level == null) {
                failures.add("Level $id does not exist")
                continue
            }
            val validation = LevelOverlapValidator.validateLevel(level)
            if (!validation.isValid) {
                println("FAILED LEVEL $id (${level.name}): ${validation.reason}")
                failures.add("Level $id: ${validation.reason}")
            }
        }
        assertTrue(
            "Validation failures:\n" + failures.joinToString("\n"),
            failures.isEmpty()
        )
    }
}
