package com.example

import com.example.data.levels.BuiltInLevels
import com.example.game.animation.SnakePathCalculator
import com.example.game.collision.CollisionDetector
import com.example.game.generator.LevelGenerator
import com.example.game.model.Arrow
import com.example.game.model.Board
import com.example.game.model.Direction
import com.example.game.model.EscapingArrow
import com.example.game.model.GameState
import com.example.game.model.GameStatus
import com.example.game.model.GridPoint
import com.example.game.model.Level
import com.example.game.model.WrongMoveState
import com.example.game.solver.HintEngine
import com.example.game.solver.PuzzleSolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ArrowsEngineUnitTest {

    @Test
    fun testArrowModelProperties() {
        val arrow = Arrow(
            id = "test_1",
            direction = Direction.RIGHT,
            points = listOf(GridPoint(1, 1), GridPoint(3, 1))
        )

        assertEquals("test_1", arrow.id)
        assertEquals(Direction.RIGHT, arrow.direction)
        assertEquals(GridPoint(1, 1), arrow.tail)
        assertEquals(GridPoint(3, 1), arrow.head)
        assertTrue(arrow.occupies(GridPoint(2, 1)))
        assertTrue(arrow.occupies(GridPoint(1, 1)))
        assertTrue(arrow.occupies(GridPoint(3, 1)))
        assertFalse(arrow.occupies(GridPoint(4, 1)))
    }

    @Test
    fun testCollisionDetection_FreeArrowCanEscape() {
        val board = Board(5, 5)
        // Arrow pointing RIGHT near edge with no obstacles
        val arrow = Arrow(
            id = "free_1",
            direction = Direction.RIGHT,
            points = listOf(GridPoint(1, 1), GridPoint(3, 1))
        )

        val canEscape = CollisionDetector.canArrowEscape(
            arrow = arrow,
            activeArrows = listOf(arrow),
            board = board
        )

        assertTrue("Solo arrow with clear corridor must be able to escape", canEscape)
    }

    @Test
    fun testCollisionDetection_BlockedArrowCannotEscape() {
        val board = Board(5, 5)

        // a1 is horizontal at y=1, from x=1 to x=3
        val a1 = Arrow("a1", Direction.RIGHT, listOf(GridPoint(1, 1), GridPoint(3, 1)))

        // a2 is vertical at x=2, from y=3 to y=2, pointing UP.
        // Its head is at (2, 2) pointing UP. Its corridor ray goes through (2, 1) where a1 is!
        val a2 = Arrow("a2", Direction.UP, listOf(GridPoint(2, 3), GridPoint(2, 2)))

        val active = listOf(a1, a2)

        val a1CanEscape = CollisionDetector.canArrowEscape(a1, active, board)
        val a2CanEscape = CollisionDetector.canArrowEscape(a2, active, board)

        assertTrue("a1 has clear path to the right", a1CanEscape)
        assertFalse("a2 is blocked by a1 in its corridor", a2CanEscape)

        // Once a1 is removed:
        val remaining = listOf(a2)
        val a2CanNowEscape = CollisionDetector.canArrowEscape(a2, remaining, board)
        assertTrue("a2 must be free to escape once a1 is cleared", a2CanNowEscape)
    }

    private val testFixtureLevel = Level(
        id = 999,
        name = "Fixture Level",
        board = Board(5, 5),
        arrows = listOf(
            Arrow("a1", Direction.RIGHT, listOf(GridPoint(1, 1), GridPoint(3, 1))),
            Arrow("a2", Direction.UP, listOf(GridPoint(2, 3), GridPoint(2, 2)))
        ),
        difficulty = 1
    )

    @Test
    fun testPuzzleSolver_SolvesFixtureLevel() {
        val level1 = testFixtureLevel
        val result = PuzzleSolver.solvePuzzle(level1.arrows, level1.board)

        assertTrue("Fixture level must be solvable", result.isSolvable)
        assertEquals("Solution sequence size must match arrow count", level1.arrows.size, result.solutionSequence.size)
    }

    @Test
    fun testPuzzleSolver_AllBuiltInLevelsAreSolvable() {
        for (level in BuiltInLevels.levels) {
            val result = PuzzleSolver.solvePuzzle(level.arrows, level.board)
            assertTrue("Built-in level ${level.id} (${level.name}) must be solvable", result.isSolvable)
            assertEquals("Level ${level.id} solution length must match arrow count", level.arrows.size, result.solutionSequence.size)
        }
    }

    @Test
    fun testHintEngine_SuggestsValidMove() {
        val level1 = testFixtureLevel
        val hint = HintEngine.getNextHint(level1.arrows, level1.board)

        assertNotNull("Hint must not be null", hint.arrowId)
        val suggestedArrow = level1.arrows.first { it.id == hint.arrowId }
        val canEscape = CollisionDetector.canArrowEscape(suggestedArrow, level1.arrows, level1.board)
        assertTrue("Hinted arrow must be able to escape legally", canEscape)
    }

    @Test
    fun testLevelGenerator_ProducesSolvablePuzzles() {
        for (levelNum in listOf(16, 20, 25)) {
            val generated = LevelGenerator.generateLevel(levelNum)
            val solveResult = PuzzleSolver.solvePuzzle(generated.arrows, generated.board)
            assertTrue("Generated level $levelNum must be solvable", solveResult.isSolvable)
        }
    }

    @Test
    fun testHitDetection_FindsTappedArrow() {
        val arrow = Arrow("target", Direction.RIGHT, listOf(GridPoint(2, 2), GridPoint(4, 2)))
        val cellSize = 100f
        val originX = 50f
        val originY = 50f

        // Tap right near (3, 2) which is screen coord (50 + 300, 50 + 200) = (350, 250)
        val found = CollisionDetector.findTappedArrow(
            tapX = 352f,
            tapY = 251f,
            activeArrows = listOf(arrow),
            boardOriginX = originX,
            boardOriginY = originY,
            cellSize = cellSize,
            touchTolerancePx = 40f
        )

        assertNotNull("Should detect tap close to arrow segment", found)
        assertEquals("target", found?.id)

        // Tap far away at (100, 100)
        val miss = CollisionDetector.findTappedArrow(
            tapX = 100f,
            tapY = 100f,
            activeArrows = listOf(arrow),
            boardOriginX = originX,
            boardOriginY = originY,
            cellSize = cellSize,
            touchTolerancePx = 40f
        )
        assertTrue("Should miss tap far from arrow", miss == null)
    }

    @Test
    fun testCollisionDetection_MultipleBlockers() {
        val board = Board(6, 6)
        // Arrow pointing right
        val target = Arrow("target", Direction.RIGHT, listOf(GridPoint(0, 2), GridPoint(1, 2)))
        // Two blockers ahead of it
        val blocker1 = Arrow("b1", Direction.DOWN, listOf(GridPoint(3, 1), GridPoint(3, 3)))
        val blocker2 = Arrow("b2", Direction.UP, listOf(GridPoint(5, 4), GridPoint(5, 1)))

        val active = listOf(target, blocker1, blocker2)
        assertFalse(CollisionDetector.canArrowEscape(target, active, board))

        // Remove first blocker - still blocked by blocker2
        val activeAfterB1 = listOf(target, blocker2)
        assertFalse(CollisionDetector.canArrowEscape(target, activeAfterB1, board))

        // Remove blocker2 - now free
        val activeAfterB2 = listOf(target)
        assertTrue(CollisionDetector.canArrowEscape(target, activeAfterB2, board))
    }

    @Test
    fun testCollisionDetection_BoundaryCases() {
        val board = Board(5, 5)
        // Arrow directly touching top-right boundary pointing UP
        val borderArrow = Arrow("border_up", Direction.UP, listOf(GridPoint(4, 2), GridPoint(4, 0)))
        assertTrue("Arrow already at border facing out can escape", CollisionDetector.canArrowEscape(borderArrow, listOf(borderArrow), board))

        // Arrow at bottom edge pointing DOWN
        val bottomArrow = Arrow("border_down", Direction.DOWN, listOf(GridPoint(1, 2), GridPoint(1, 4)))
        assertTrue("Arrow at bottom boundary facing down can escape", CollisionDetector.canArrowEscape(bottomArrow, listOf(bottomArrow), board))
    }

    @Test
    fun testCollisionDetection_CornerAndLShapedArrows() {
        val board = Board(5, 5)
        // L-shaped arrow: from (1, 3) to (1, 1) to (3, 1), head at (3, 1) pointing RIGHT
        val lArrow = Arrow("l1", Direction.RIGHT, listOf(GridPoint(1, 3), GridPoint(1, 1), GridPoint(3, 1)))
        val blocker = Arrow("blocker", Direction.DOWN, listOf(GridPoint(4, 0), GridPoint(4, 2)))

        assertFalse("Blocker at (4, 1) should block lArrow", CollisionDetector.canArrowEscape(lArrow, listOf(lArrow, blocker), board))

        // Clear blocker
        assertTrue("Without blocker, lArrow escapes to the right", CollisionDetector.canArrowEscape(lArrow, listOf(lArrow), board))
    }

    @Test
    fun testSnakePath_StraightHorizontalArrow() {
        val board = Board(5, 5)
        val arrow = Arrow("straight_h", Direction.RIGHT, listOf(GridPoint(1, 2), GridPoint(3, 2)))
        val track = SnakePathCalculator.buildEscapeTrack(arrow, board)

        assertEquals("Initial length should be 2 units", 2.0f, track.initialArrowLength, 0.001f)
        assertTrue("Total escape distance must clear board", track.totalEscapeDistance > 3.0f)

        // Snapshot at distance 0 (initial state)
        val initialSnap = track.sampleSnake(0f)
        assertEquals("Tail at start should be (1, 2)", 1.0f, initialSnap.tailPoint.x, 0.01f)
        assertEquals("Tail at start should be (1, 2)", 2.0f, initialSnap.tailPoint.y, 0.01f)
        assertEquals("Head at start should be (3, 2)", 3.0f, initialSnap.headPoint.x, 0.01f)
        assertEquals("Head at start should be (3, 2)", 2.0f, initialSnap.headPoint.y, 0.01f)

        // Advance distance by 1 unit
        val midSnap = track.sampleSnake(1.0f)
        assertEquals("Tail should move 1 unit right to (2, 2)", 2.0f, midSnap.tailPoint.x, 0.01f)
        assertEquals("Head should move 1 unit right to (4, 2)", 4.0f, midSnap.headPoint.x, 0.01f)
        assertEquals("Head orientation must remain RIGHT (0 rad)", 0f, midSnap.headAngleRad, 0.01f)

        // At end of escape distance: tail must be past board right boundary (x >= 4.0 + 2.0)
        val endSnap = track.sampleSnake(track.totalEscapeDistance)
        assertTrue("Tail must be well past board edge", endSnap.tailPoint.x >= 5.5f)
        assertTrue("Head must be well past tail", endSnap.headPoint.x > endSnap.tailPoint.x)
    }

    @Test
    fun testSnakePath_LShapedArrowTraversesCorner() {
        val board = Board(5, 5)
        // Tail at (1, 3), corner at (1, 1), head at (3, 1) pointing RIGHT
        val arrow = Arrow("l_arrow", Direction.RIGHT, listOf(GridPoint(1, 3), GridPoint(1, 1), GridPoint(3, 1)))
        val track = SnakePathCalculator.buildEscapeTrack(arrow, board)

        assertEquals("Arrow length should be 2 + 2 = 4 units", 4.0f, track.initialArrowLength, 0.001f)

        // At d = 0: initial snake
        val s0 = track.sampleSnake(0f)
        assertEquals(3, s0.points.size)
        assertEquals(1.0f, s0.points[0].x, 0.01f)
        assertEquals(3.0f, s0.points[0].y, 0.01f)
        assertEquals(1.0f, s0.points[1].x, 0.01f)
        assertEquals(1.0f, s0.points[1].y, 0.01f)
        assertEquals(3.0f, s0.points[2].x, 0.01f)
        assertEquals(1.0f, s0.points[2].y, 0.01f)

        // At d = 1.0: tail moves up from (1, 3) to (1, 2), corner at (1, 1) is still part of the body
        val s1 = track.sampleSnake(1.0f)
        assertEquals(1.0f, s1.tailPoint.x, 0.01f)
        assertEquals(2.0f, s1.tailPoint.y, 0.01f)
        assertEquals(4.0f, s1.headPoint.x, 0.01f)
        assertEquals(1.0f, s1.headPoint.y, 0.01f)
        // Corner (1, 1) must be preserved in points list
        val hasCorner = s1.points.any { kotlin.math.abs(it.x - 1.0f) < 0.01f && kotlin.math.abs(it.y - 1.0f) < 0.01f }
        assertTrue("Corner at (1, 1) must be preserved while tail is before corner", hasCorner)

        // At d = 2.5: tail has turned the corner! Tail is now on horizontal segment at x=1.5, y=1.0
        val s25 = track.sampleSnake(2.5f)
        assertEquals(1.5f, s25.tailPoint.x, 0.01f)
        assertEquals(1.0f, s25.tailPoint.y, 0.01f)
        assertEquals(5.5f, s25.headPoint.x, 0.01f)
        assertEquals(1.0f, s25.headPoint.y, 0.01f)
    }

    @Test
    fun testSnakePath_VerticalUpAndDownArrows() {
        val board = Board(5, 5)

        // UP arrow
        val upArrow = Arrow("up", Direction.UP, listOf(GridPoint(2, 4), GridPoint(2, 2)))
        val upTrack = SnakePathCalculator.buildEscapeTrack(upArrow, board)
        val upSnap = upTrack.sampleSnake(1.0f)
        assertEquals(2.0f, upSnap.tailPoint.x, 0.01f)
        assertEquals(3.0f, upSnap.tailPoint.y, 0.01f)
        assertEquals(2.0f, upSnap.headPoint.x, 0.01f)
        assertEquals(1.0f, upSnap.headPoint.y, 0.01f)

        // DOWN arrow
        val downArrow = Arrow("down", Direction.DOWN, listOf(GridPoint(3, 1), GridPoint(3, 3)))
        val downTrack = SnakePathCalculator.buildEscapeTrack(downArrow, board)
        val downSnap = downTrack.sampleSnake(1.0f)
        assertEquals(3.0f, downSnap.tailPoint.x, 0.01f)
        assertEquals(2.0f, downSnap.tailPoint.y, 0.01f)
        assertEquals(3.0f, downSnap.headPoint.x, 0.01f)
        assertEquals(4.0f, downSnap.headPoint.y, 0.01f)
    }

    @Test
    fun testSnakePath_MultiBendZigZagArrow() {
        val board = Board(6, 6)
        // Multi-bend: (1, 4) -> (1, 2) -> (3, 2) -> (3, 1), head at (3, 1) pointing UP
        val multiArrow = Arrow(
            "multi",
            Direction.UP,
            listOf(GridPoint(1, 4), GridPoint(1, 2), GridPoint(3, 2), GridPoint(3, 1))
        )
        val track = SnakePathCalculator.buildEscapeTrack(multiArrow, board)
        // Total length = 2 + 2 + 1 = 5
        assertEquals(5.0f, track.initialArrowLength, 0.01f)

        val s0 = track.sampleSnake(0f)
        assertEquals(4, s0.points.size)
        assertEquals(1.0f, s0.tailPoint.x, 0.01f)
        assertEquals(4.0f, s0.tailPoint.y, 0.01f)
        assertEquals(3.0f, s0.headPoint.x, 0.01f)
        assertEquals(1.0f, s0.headPoint.y, 0.01f)
    }

    @Test
    fun testLevelOverlapValidator_AllBuiltInLevelsAreStrictlyNonOverlapping() {
        for (level in BuiltInLevels.levels) {
            val validation = com.example.game.validation.LevelOverlapValidator.validateLevel(level)
            assertTrue(
                "Built-in level ${level.id} (${level.name}) must have zero overlaps and satisfy all clearance checks. Reason: ${validation.reason}",
                validation.isValid
            )
        }
    }

    @Test
    fun testLevelOverlapValidator_DetectsCrossingSegments() {
        val board = Board(5, 5)
        // Two crossing arrows intersecting at (2, 2)
        val horizontal = Arrow("h", Direction.RIGHT, listOf(GridPoint(1, 2), GridPoint(3, 2)))
        val vertical = Arrow("v", Direction.DOWN, listOf(GridPoint(2, 1), GridPoint(2, 3)))

        val level = com.example.game.model.Level(99, "Invalid Crossing", board, listOf(horizontal, vertical), 1)
        val validation = com.example.game.validation.LevelOverlapValidator.validateLevel(level)

        assertFalse("Crossing arrows must be rejected by LevelOverlapValidator", validation.isValid)
    }

    @Test
    fun testLevelOverlapValidator_DetectsSharedGridPoint() {
        val board = Board(5, 5)
        // Two arrows sharing point (2, 2)
        val a1 = Arrow("a1", Direction.RIGHT, listOf(GridPoint(1, 2), GridPoint(2, 2)))
        val a2 = Arrow("a2", Direction.DOWN, listOf(GridPoint(2, 2), GridPoint(2, 4)))

        val isClear = com.example.game.validation.LevelOverlapValidator.areArrowsClear(a1, a2)
        assertFalse("Arrows sharing a grid point must be rejected", isClear)
    }

    @Test
    fun testCollisionDistance_CalculatedDynamicallyBasedOnBlockerDistance() {
        val board = Board(7, 7)
        // Arrow pointing RIGHT with head at (1, 3)
        val arrow = Arrow("subject", Direction.RIGHT, listOf(GridPoint(0, 3), GridPoint(1, 3)))

        // Blocker 1 cell away at (2, 3) -> distance k = 1
        val blockerClose = Arrow("b_close", Direction.DOWN, listOf(GridPoint(2, 2), GridPoint(2, 4)))
        val distClose = CollisionDetector.calculateCollisionDistance(arrow, listOf(arrow, blockerClose), board)
        assertTrue("Distance to immediate neighbor blocker must be around 0.65 units", distClose in 0.5f..0.8f)

        // Blocker 3 cells away at (4, 3) -> distance k = 3
        val blockerFar = Arrow("b_far", Direction.DOWN, listOf(GridPoint(4, 2), GridPoint(4, 4)))
        val distFar = CollisionDetector.calculateCollisionDistance(arrow, listOf(arrow, blockerFar), board)
        assertTrue("Distance to far blocker (3 cells away) must be around 2.65 units", distFar in 2.4f..2.8f)
        assertTrue("Distance must scale dynamically with blocker position", distFar > distClose)
    }

    @Test
    fun testMeterProgress_ImmediateIncreaseOnEscapeWithoutResetOrDrop() {
        val testLevel = testFixtureLevel // 2 arrows: a1 and a2
        var state = GameState(sessionId = 1L, level = testLevel)

        assertEquals("Initial progress must be 0.0", 0.0f, state.progressFraction, 0.001f)
        assertEquals("Initial cleared count is 0", 0, state.clearedArrowsCount)

        // Tapping a1 (legal escape): state immediately transitions to escaping
        val a1 = state.activeArrows.first { it.id == "a1" }
        state = state.copy(
            escapingArrow = com.example.game.model.EscapingArrow(arrow = a1, progress = 0f)
        )

        // MUST immediately be 1/2 (50%), NEVER reset to 0 or negative
        assertEquals("Cleared count is 1 immediately while escaping", 1, state.clearedArrowsCount)
        assertEquals("Progress is 50% immediately upon tap", 0.5f, state.progressFraction, 0.001f)

        // Arrow finishes escaping and exits board:
        state = state.copy(
            activeArrows = state.activeArrows.filter { it.id != "a1" },
            removedArrowIds = setOf("a1"),
            escapingArrow = null
        )

        // Progress MUST remain exactly 50% without dropping or glitching
        assertEquals("Cleared count remains 1 after flight finish", 1, state.clearedArrowsCount)
        assertEquals("Progress remains exactly 50% after flight finish", 0.5f, state.progressFraction, 0.001f)
    }

    @Test
    fun testMeterProgress_WrongMoveDoesNotChangeProgress() {
        val testLevel = testFixtureLevel
        val a2 = testLevel.arrows.first { it.id == "a2" } // a2 is blocked by a1

        var state = GameState(sessionId = 1L, level = testLevel)
        assertEquals(0.0f, state.progressFraction, 0.001f)

        // Wrong move initiated:
        state = state.copy(
            wrongMoveArrow = com.example.game.model.WrongMoveState(arrow = a2, collisionDistance = 1.0f)
        )

        // Meter MUST NOT change for wrong move!
        assertEquals("Cleared count remains 0 during wrong move", 0, state.clearedArrowsCount)
        assertEquals("Progress remains 0.0 for wrong move", 0.0f, state.progressFraction, 0.001f)

        // Impact happens, heart lost:
        state = state.copy(hearts = 2)
        assertEquals("Progress remains 0.0 after impact", 0.0f, state.progressFraction, 0.001f)

        // Wrong move finishes returning to origin:
        state = state.copy(wrongMoveArrow = null)
        assertEquals("Progress remains 0.0 after return", 0.0f, state.progressFraction, 0.001f)
    }

    @Test
    fun testMeterProgress_FullLevelProgressionTo100Percent() {
        val testLevel = testFixtureLevel // 2 arrows
        var state = GameState(sessionId = 1L, level = testLevel)

        // Step 1: a1 escapes
        val a1 = testLevel.arrows.first { it.id == "a1" }
        state = state.copy(escapingArrow = com.example.game.model.EscapingArrow(a1))
        assertEquals(0.5f, state.progressFraction, 0.001f)

        // a1 completes exit
        state = state.copy(
            activeArrows = listOf(testLevel.arrows.first { it.id == "a2" }),
            removedArrowIds = setOf("a1"),
            escapingArrow = null
        )
        assertEquals(0.5f, state.progressFraction, 0.001f)

        // Step 2: a2 escapes (final arrow)
        val a2 = testLevel.arrows.first { it.id == "a2" }
        state = state.copy(escapingArrow = com.example.game.model.EscapingArrow(a2))
        assertEquals("Final progress reaches exactly 1.0 (100%)", 1.0f, state.progressFraction, 0.001f)

        // a2 completes exit -> level complete
        state = state.copy(
            activeArrows = emptyList<Arrow>(),
            removedArrowIds = setOf("a1", "a2"),
            escapingArrow = null,
            status = GameStatus.LEVEL_COMPLETE
        )
        assertEquals(1.0f, state.progressFraction, 0.001f)
        assertTrue(state.isSolved)
    }
}
