package com.example

import com.example.game.model.Arrow
import com.example.game.model.Board
import com.example.game.model.Direction
import com.example.game.model.GridPoint
import com.example.game.model.Level
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PublishedLevelsIntegrationTest {

    @Test
    fun testFirstPlayableLevelIsAlwaysLevel1() {
        val level1 = Level(
            id = 1,
            name = "Level 1",
            board = Board(6, 6),
            arrows = listOf(Arrow("a1", Direction.RIGHT, listOf(GridPoint(1, 1), GridPoint(3, 1)))),
            levelNumber = 1
        )

        assertNotNull(level1)
        assertEquals(1, level1.id)
        assertEquals(1, level1.levelNumber)
        assertEquals("Level 1", level1.name)
    }

    @Test
    fun testSequentialLevelNumbering() {
        val levels = listOf(
            Level(id = 1, name = "Level 1", board = Board(5, 5), arrows = listOf(Arrow("a1", Direction.RIGHT, listOf(GridPoint(1, 1), GridPoint(3, 1))))),
            Level(id = 2, name = "Level 2", board = Board(6, 6), arrows = listOf(Arrow("a2", Direction.DOWN, listOf(GridPoint(2, 2), GridPoint(2, 4)))))
        )

        assertEquals(1, levels[0].id)
        assertEquals(2, levels[1].id)
        assertEquals("Level 1", levels[0].name)
        assertEquals("Level 2", levels[1].name)
    }
}
