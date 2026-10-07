package com.example.game.generator.pattern

import com.example.game.model.Arrow
import com.example.game.model.Board
import com.example.game.model.Direction
import com.example.game.model.GridPoint
import com.example.game.model.Level
import kotlin.random.Random

/**
 * Procedural template builder for Levels 6 to 100 and endless 100+ mode.
 * Generates structured, architecturally intentional puzzles matching exact difficulty and arrow count curves.
 */
object ProceduralTemplateLibrary {

    fun generateTemplateLevel(levelNumber: Int, seed: Long): Level {
        // Compose rich non-box geometric patterns from the 20 pattern families
        val composed = PatternComposer.composeLevel(levelNumber, seed)
        return composed
    }

    // 6-10: Early Challenge (8-12 arrows, Hard / Very Hard)
    private fun buildEarlyChallengeVariation(levelNumber: Int, random: Random): Level {
        val board = Board(8, 8)
        val arrows = listOf(
            Arrow("ec_p1", Direction.RIGHT, listOf(GridPoint(0, 0), GridPoint(7, 0))),
            Arrow("ec_p2", Direction.DOWN, listOf(GridPoint(7, 1), GridPoint(7, 7))),
            Arrow("ec_p3", Direction.LEFT, listOf(GridPoint(6, 7), GridPoint(0, 7))),
            Arrow("ec_p4", Direction.UP, listOf(GridPoint(0, 6), GridPoint(0, 1))),
            Arrow("ec_c1", Direction.LEFT, listOf(GridPoint(3, 2), GridPoint(1, 2))),
            Arrow("ec_c2", Direction.UP, listOf(GridPoint(2, 4), GridPoint(2, 3))),
            Arrow("ec_c3", Direction.RIGHT, listOf(GridPoint(4, 5), GridPoint(6, 5))),
            Arrow("ec_c4", Direction.DOWN, listOf(GridPoint(5, 3), GridPoint(5, 4))),
            Arrow("ec_c5", Direction.RIGHT, listOf(GridPoint(3, 3), GridPoint(4, 3))),
            Arrow("ec_c6", Direction.DOWN, listOf(GridPoint(3, 5), GridPoint(3, 6))),
            Arrow("ec_c7", Direction.DOWN, listOf(GridPoint(6, 2), GridPoint(6, 3)))
        )
        val diff = if (levelNumber <= 8) 4 else 5
        return applyDeterministicTransforms(
            Level(levelNumber, "Level $levelNumber", board, arrows, difficulty = diff, patternType = PatternType.REVERSE_L.name),
            random
        )
    }

    // 11-20: Corridors & Parallels (10-14 arrows, Hard / Very Hard)
    private fun buildCorridorParallelVariation(levelNumber: Int, random: Random): Level {
        val board = Board(8, 8)
        val arrows = listOf(
            Arrow("cp_1", Direction.RIGHT, listOf(GridPoint(1, 1), GridPoint(6, 1))),
            Arrow("cp_2", Direction.DOWN, listOf(GridPoint(6, 2), GridPoint(6, 6))),
            Arrow("cp_3", Direction.LEFT, listOf(GridPoint(5, 6), GridPoint(1, 6))),
            Arrow("cp_4", Direction.UP, listOf(GridPoint(1, 5), GridPoint(1, 2))),
            Arrow("cp_5", Direction.RIGHT, listOf(GridPoint(2, 2), GridPoint(5, 2))),
            Arrow("cp_6", Direction.LEFT, listOf(GridPoint(5, 5), GridPoint(2, 5))),
            Arrow("cp_7", Direction.RIGHT, listOf(GridPoint(2, 3), GridPoint(4, 3))),
            Arrow("cp_8", Direction.LEFT, listOf(GridPoint(5, 4), GridPoint(3, 4))),
            Arrow("cp_9", Direction.RIGHT, listOf(GridPoint(0, 0), GridPoint(6, 0))),
            Arrow("cp_10", Direction.DOWN, listOf(GridPoint(7, 0), GridPoint(7, 7))),
            Arrow("cp_11", Direction.LEFT, listOf(GridPoint(6, 7), GridPoint(0, 7))),
            Arrow("cp_12", Direction.UP, listOf(GridPoint(0, 6), GridPoint(0, 1)))
        )
        return applyDeterministicTransforms(
            Level(levelNumber, "Level $levelNumber", board, arrows, difficulty = 4, patternType = PatternType.PARALLEL_LINES.name),
            random
        )
    }

    // 21-35: Advanced Nested Patterns (11-16 arrows, Very Hard)
    private fun buildAdvancedNestedVariation(levelNumber: Int, random: Random): Level {
        val board = Board(9, 9)
        val arrows = listOf(
            // Outer Ring
            Arrow("an_o1", Direction.RIGHT, listOf(GridPoint(0, 0), GridPoint(7, 0))),
            Arrow("an_o2", Direction.DOWN, listOf(GridPoint(8, 0), GridPoint(8, 8))),
            Arrow("an_o3", Direction.LEFT, listOf(GridPoint(7, 8), GridPoint(0, 8))),
            Arrow("an_o4", Direction.UP, listOf(GridPoint(0, 7), GridPoint(0, 1))),
            // Second Ring
            Arrow("an_m1", Direction.RIGHT, listOf(GridPoint(1, 1), GridPoint(6, 1))),
            Arrow("an_m2", Direction.DOWN, listOf(GridPoint(7, 1), GridPoint(7, 7))),
            Arrow("an_m3", Direction.LEFT, listOf(GridPoint(6, 7), GridPoint(1, 7))),
            Arrow("an_m4", Direction.UP, listOf(GridPoint(1, 6), GridPoint(1, 2))),
            // Core Ring
            Arrow("an_c1", Direction.RIGHT, listOf(GridPoint(2, 2), GridPoint(5, 2))),
            Arrow("an_c2", Direction.DOWN, listOf(GridPoint(6, 2), GridPoint(6, 6))),
            Arrow("an_c3", Direction.LEFT, listOf(GridPoint(5, 6), GridPoint(2, 6))),
            Arrow("an_c4", Direction.UP, listOf(GridPoint(2, 5), GridPoint(2, 3))),
            // Core Center
            Arrow("an_in1", Direction.RIGHT, listOf(GridPoint(3, 4), GridPoint(4, 4))),
            Arrow("an_in2", Direction.LEFT, listOf(GridPoint(4, 3), GridPoint(3, 3)))
        )
        return applyDeterministicTransforms(
            Level(levelNumber, "Level $levelNumber", board, arrows, difficulty = 5, patternType = PatternType.NESTED_U.name),
            random
        )
    }

    // 36-40: Nested Structures (12-18 arrows, Very Hard)
    private fun buildNestedVariation(levelNumber: Int, random: Random): Level {
        val board = Board(8, 8)
        val arrows = mutableListOf<Arrow>()

        // Outer ring
        arrows.add(Arrow("n_out_top", Direction.RIGHT, listOf(GridPoint(1, 1), GridPoint(6, 1))))
        arrows.add(Arrow("n_out_right", Direction.DOWN, listOf(GridPoint(6, 2), GridPoint(6, 6))))
        arrows.add(Arrow("n_out_bot", Direction.LEFT, listOf(GridPoint(5, 6), GridPoint(1, 6))))
        arrows.add(Arrow("n_out_left", Direction.UP, listOf(GridPoint(1, 5), GridPoint(1, 2))))

        // Middle ring
        arrows.add(Arrow("n_mid_top", Direction.RIGHT, listOf(GridPoint(2, 2), GridPoint(5, 2))))
        arrows.add(Arrow("n_mid_bot", Direction.LEFT, listOf(GridPoint(5, 5), GridPoint(2, 5))))

        // Inner core
        arrows.add(Arrow("n_core_1", Direction.RIGHT, listOf(GridPoint(2, 3), GridPoint(4, 3))))
        arrows.add(Arrow("n_core_2", Direction.LEFT, listOf(GridPoint(5, 4), GridPoint(3, 4))))

        // Perimeter gatekeepers
        arrows.add(Arrow("p_g1", Direction.RIGHT, listOf(GridPoint(0, 0), GridPoint(6, 0))))
        arrows.add(Arrow("p_g2", Direction.DOWN, listOf(GridPoint(7, 0), GridPoint(7, 7))))
        arrows.add(Arrow("p_g3", Direction.LEFT, listOf(GridPoint(6, 7), GridPoint(0, 7))))
        arrows.add(Arrow("p_g4", Direction.UP, listOf(GridPoint(0, 6), GridPoint(0, 1))))

        return applyDeterministicTransforms(Level(levelNumber, "Level $levelNumber", board, arrows, difficulty = 5, patternType = PatternType.NESTED_ESCAPE.name), random)
    }

    // 41-50: Deep Dependency Chains (13-16 arrows, Very Hard)
    private fun buildDeepChainVariation(levelNumber: Int, random: Random): Level {
        val board = Board(8, 8)
        val arrows = mutableListOf<Arrow>()

        arrows.add(Arrow("dc_1", Direction.RIGHT, listOf(GridPoint(1, 1), GridPoint(6, 1))))
        arrows.add(Arrow("dc_2", Direction.DOWN, listOf(GridPoint(5, 2), GridPoint(5, 4))))
        arrows.add(Arrow("dc_3", Direction.LEFT, listOf(GridPoint(6, 3), GridPoint(2, 3))))
        arrows.add(Arrow("dc_4", Direction.UP, listOf(GridPoint(1, 4), GridPoint(1, 2))))
        arrows.add(Arrow("dc_5", Direction.RIGHT, listOf(GridPoint(0, 5), GridPoint(6, 5))))
        arrows.add(Arrow("dc_6", Direction.DOWN, listOf(GridPoint(2, 0), GridPoint(2, 2))))
        arrows.add(Arrow("dc_7", Direction.LEFT, listOf(GridPoint(6, 6), GridPoint(1, 6))))
        arrows.add(Arrow("dc_8", Direction.UP, listOf(GridPoint(7, 5), GridPoint(7, 1))))
        arrows.add(Arrow("dc_9", Direction.RIGHT, listOf(GridPoint(0, 7), GridPoint(6, 7))))
        arrows.add(Arrow("dc_10", Direction.DOWN, listOf(GridPoint(0, 1), GridPoint(0, 4))))
        arrows.add(Arrow("dc_11", Direction.LEFT, listOf(GridPoint(5, 0), GridPoint(3, 0))))
        arrows.add(Arrow("dc_12", Direction.DOWN, listOf(GridPoint(7, 6), GridPoint(7, 7))))
        arrows.add(Arrow("dc_13", Direction.LEFT, listOf(GridPoint(4, 4), GridPoint(3, 4))))

        return applyDeterministicTransforms(Level(levelNumber, "Level $levelNumber", board, arrows, difficulty = 5, patternType = PatternType.DEEP_CHAIN.name), random)
    }

    // 51-60: Multiple Valid Openings (14-17 arrows, Very Hard)
    private fun buildMultipleOpeningsVariation(levelNumber: Int, random: Random): Level {
        val board = Board(8, 8)
        val arrows = mutableListOf<Arrow>()

        // Left wing opening
        arrows.add(Arrow("lw_open", Direction.LEFT, listOf(GridPoint(2, 1), GridPoint(0, 1))))
        arrows.add(Arrow("lw_chain", Direction.DOWN, listOf(GridPoint(1, 2), GridPoint(1, 5))))
        arrows.add(Arrow("lw_bot", Direction.RIGHT, listOf(GridPoint(0, 6), GridPoint(3, 6))))

        // Right wing opening
        arrows.add(Arrow("rw_open", Direction.RIGHT, listOf(GridPoint(5, 6), GridPoint(7, 6))))
        arrows.add(Arrow("rw_chain", Direction.UP, listOf(GridPoint(6, 5), GridPoint(6, 2))))
        arrows.add(Arrow("rw_top", Direction.LEFT, listOf(GridPoint(7, 1), GridPoint(4, 1))))

        // Central cross-locking core
        arrows.add(Arrow("c_top", Direction.RIGHT, listOf(GridPoint(2, 2), GridPoint(5, 2))))
        arrows.add(Arrow("c_bot", Direction.LEFT, listOf(GridPoint(5, 5), GridPoint(2, 5))))
        arrows.add(Arrow("c_in_1", Direction.RIGHT, listOf(GridPoint(2, 3), GridPoint(4, 3))))
        arrows.add(Arrow("c_in_2", Direction.LEFT, listOf(GridPoint(5, 4), GridPoint(3, 4))))
        arrows.add(Arrow("c_lat_1", Direction.DOWN, listOf(GridPoint(3, 0), GridPoint(3, 1))))
        arrows.add(Arrow("c_lat_2", Direction.UP, listOf(GridPoint(4, 7), GridPoint(4, 6))))

        // Perimeter gatekeepers
        arrows.add(Arrow("p_top", Direction.RIGHT, listOf(GridPoint(0, 0), GridPoint(2, 0))))
        arrows.add(Arrow("p_bot", Direction.LEFT, listOf(GridPoint(7, 7), GridPoint(5, 7))))
        arrows.add(Arrow("p_side", Direction.DOWN, listOf(GridPoint(7, 2), GridPoint(7, 5))))

        return applyDeterministicTransforms(Level(levelNumber, "Level $levelNumber", board, arrows, difficulty = 5, patternType = PatternType.MULTIPLE_OPENINGS.name), random)
    }

    // 61-70: Misleading Corridors (15-18 arrows, Expert)
    private fun buildMisleadingCorridorVariation(levelNumber: Int, random: Random): Level {
        val board = Board(8, 8)
        val arrows = mutableListOf<Arrow>()

        arrows.add(Arrow("trap_1", Direction.RIGHT, listOf(GridPoint(1, 2), GridPoint(3, 2))))
        arrows.add(Arrow("trap_blocker", Direction.DOWN, listOf(GridPoint(5, 1), GridPoint(5, 3))))
        arrows.add(Arrow("trap_releaser", Direction.LEFT, listOf(GridPoint(6, 0), GridPoint(4, 0))))
        arrows.add(Arrow("trap_guide", Direction.UP, listOf(GridPoint(6, 3), GridPoint(6, 1))))

        arrows.add(Arrow("bt_1", Direction.LEFT, listOf(GridPoint(6, 5), GridPoint(4, 5))))
        arrows.add(Arrow("bt_blocker", Direction.UP, listOf(GridPoint(2, 6), GridPoint(2, 4))))
        arrows.add(Arrow("bt_releaser", Direction.RIGHT, listOf(GridPoint(1, 7), GridPoint(3, 7))))
        arrows.add(Arrow("bt_guide", Direction.DOWN, listOf(GridPoint(1, 4), GridPoint(1, 6))))

        arrows.add(Arrow("m_1", Direction.RIGHT, listOf(GridPoint(2, 3), GridPoint(4, 3))))
        arrows.add(Arrow("m_2", Direction.LEFT, listOf(GridPoint(5, 4), GridPoint(3, 4))))
        arrows.add(Arrow("p_top", Direction.RIGHT, listOf(GridPoint(0, 1), GridPoint(3, 1))))
        arrows.add(Arrow("p_bot", Direction.LEFT, listOf(GridPoint(7, 6), GridPoint(4, 6))))

        // Additional perimeter anchors
        arrows.add(Arrow("p_out1", Direction.RIGHT, listOf(GridPoint(0, 0), GridPoint(3, 0))))
        arrows.add(Arrow("p_out2", Direction.DOWN, listOf(GridPoint(7, 0), GridPoint(7, 5))))
        arrows.add(Arrow("p_out3", Direction.LEFT, listOf(GridPoint(7, 7), GridPoint(4, 7))))

        return applyDeterministicTransforms(Level(levelNumber, "Level $levelNumber", board, arrows, difficulty = 6, patternType = PatternType.MISLEADING_CORRIDOR.name), random)
    }

    // 71-80: Serpentine Clusters & Staircases (16 arrows, Expert)
    private fun buildSerpentineClusterVariation(levelNumber: Int, random: Random): Level {
        val board = Board(9, 9)
        val arrows = listOf(
            // Ring 0 (Outer)
            Arrow("sc_r0_top", Direction.RIGHT, listOf(GridPoint(0, 0), GridPoint(7, 0))),
            Arrow("sc_r0_right", Direction.DOWN, listOf(GridPoint(8, 0), GridPoint(8, 7))),
            Arrow("sc_r0_bot", Direction.LEFT, listOf(GridPoint(7, 8), GridPoint(1, 8))),
            Arrow("sc_r0_left", Direction.UP, listOf(GridPoint(0, 7), GridPoint(0, 1))),

            // Ring 1
            Arrow("sc_r1_top", Direction.RIGHT, listOf(GridPoint(1, 1), GridPoint(6, 1))),
            Arrow("sc_r1_right", Direction.DOWN, listOf(GridPoint(7, 1), GridPoint(7, 6))),
            Arrow("sc_r1_bot", Direction.LEFT, listOf(GridPoint(6, 7), GridPoint(2, 7))),
            Arrow("sc_r1_left", Direction.UP, listOf(GridPoint(1, 6), GridPoint(1, 2))),

            // Ring 2
            Arrow("sc_r2_top", Direction.RIGHT, listOf(GridPoint(2, 2), GridPoint(5, 2))),
            Arrow("sc_r2_right", Direction.DOWN, listOf(GridPoint(6, 2), GridPoint(6, 5))),
            Arrow("sc_r2_bot", Direction.LEFT, listOf(GridPoint(5, 6), GridPoint(3, 6))),
            Arrow("sc_r2_left", Direction.UP, listOf(GridPoint(2, 5), GridPoint(2, 3))),

            // Ring 3 (Center Pinwheel)
            Arrow("sc_c_up", Direction.UP, listOf(GridPoint(3, 4), GridPoint(3, 3))),
            Arrow("sc_c_right", Direction.RIGHT, listOf(GridPoint(4, 3), GridPoint(5, 3))),
            Arrow("sc_c_down", Direction.DOWN, listOf(GridPoint(5, 4), GridPoint(5, 5))),
            Arrow("sc_c_left", Direction.LEFT, listOf(GridPoint(4, 5), GridPoint(3, 5)))
        )
        return applyDeterministicTransforms(
            Level(levelNumber, "Level $levelNumber", board, arrows, difficulty = 6, patternType = PatternType.SERPENTINE_CLUSTER.name),
            random
        )
    }

    // 81-90: Twin Spirals & Concentric Mazes (16 arrows, Expert)
    private fun buildTwinSpiralVariation(levelNumber: Int, random: Random): Level {
        val board = Board(9, 9)
        val arrows = listOf(
            // Ring 0 (Outer)
            Arrow("ts_r0_top", Direction.RIGHT, listOf(GridPoint(0, 0), GridPoint(7, 0))),
            Arrow("ts_r0_right", Direction.DOWN, listOf(GridPoint(8, 0), GridPoint(8, 7))),
            Arrow("ts_r0_bot", Direction.LEFT, listOf(GridPoint(7, 8), GridPoint(1, 8))),
            Arrow("ts_r0_left", Direction.UP, listOf(GridPoint(0, 7), GridPoint(0, 1))),

            // Ring 1
            Arrow("ts_r1_top", Direction.RIGHT, listOf(GridPoint(1, 1), GridPoint(6, 1))),
            Arrow("ts_r1_right", Direction.DOWN, listOf(GridPoint(7, 1), GridPoint(7, 6))),
            Arrow("ts_r1_bot", Direction.LEFT, listOf(GridPoint(6, 7), GridPoint(2, 7))),
            Arrow("ts_r1_left", Direction.UP, listOf(GridPoint(1, 6), GridPoint(1, 2))),

            // Ring 2
            Arrow("ts_r2_top", Direction.RIGHT, listOf(GridPoint(2, 2), GridPoint(5, 2))),
            Arrow("ts_r2_right", Direction.DOWN, listOf(GridPoint(6, 2), GridPoint(6, 5))),
            Arrow("ts_r2_bot", Direction.LEFT, listOf(GridPoint(5, 6), GridPoint(3, 6))),
            Arrow("ts_r2_left", Direction.UP, listOf(GridPoint(2, 5), GridPoint(2, 3))),

            // Ring 3 (Center Pinwheel)
            Arrow("ts_c_up", Direction.UP, listOf(GridPoint(3, 4), GridPoint(3, 3))),
            Arrow("ts_c_right", Direction.RIGHT, listOf(GridPoint(4, 3), GridPoint(5, 3))),
            Arrow("ts_c_down", Direction.DOWN, listOf(GridPoint(5, 4), GridPoint(5, 5))),
            Arrow("ts_c_left", Direction.LEFT, listOf(GridPoint(4, 5), GridPoint(3, 5)))
        )
        return applyDeterministicTransforms(
            Level(levelNumber, "Level $levelNumber", board, arrows, difficulty = 6, patternType = PatternType.TWIN_SPIRAL.name),
            random
        )
    }

    // 91-100: Expert Labyrinth (20 arrows, The Gauntlet)
    private fun buildExpertLabyrinthVariation(levelNumber: Int, random: Random): Level {
        val board = Board(11, 11)
        val arrows = listOf(
            // Ring 0 (indices 0..10)
            Arrow("ex_r0_top", Direction.RIGHT, listOf(GridPoint(0, 0), GridPoint(9, 0))),
            Arrow("ex_r0_right", Direction.DOWN, listOf(GridPoint(10, 0), GridPoint(10, 9))),
            Arrow("ex_r0_bot", Direction.LEFT, listOf(GridPoint(9, 10), GridPoint(1, 10))),
            Arrow("ex_r0_left", Direction.UP, listOf(GridPoint(0, 9), GridPoint(0, 1))),

            // Ring 1 (indices 1..9)
            Arrow("ex_r1_top", Direction.RIGHT, listOf(GridPoint(1, 1), GridPoint(8, 1))),
            Arrow("ex_r1_right", Direction.DOWN, listOf(GridPoint(9, 1), GridPoint(9, 8))),
            Arrow("ex_r1_bot", Direction.LEFT, listOf(GridPoint(8, 9), GridPoint(2, 9))),
            Arrow("ex_r1_left", Direction.UP, listOf(GridPoint(1, 8), GridPoint(1, 2))),

            // Ring 2 (indices 2..8)
            Arrow("ex_r2_top", Direction.RIGHT, listOf(GridPoint(2, 2), GridPoint(7, 2))),
            Arrow("ex_r2_right", Direction.DOWN, listOf(GridPoint(8, 2), GridPoint(8, 7))),
            Arrow("ex_r2_bot", Direction.LEFT, listOf(GridPoint(7, 8), GridPoint(3, 8))),
            Arrow("ex_r2_left", Direction.UP, listOf(GridPoint(2, 7), GridPoint(2, 3))),

            // Ring 3 (indices 3..7)
            Arrow("ex_r3_top", Direction.RIGHT, listOf(GridPoint(3, 3), GridPoint(6, 3))),
            Arrow("ex_r3_right", Direction.DOWN, listOf(GridPoint(7, 3), GridPoint(7, 6))),
            Arrow("ex_r3_bot", Direction.LEFT, listOf(GridPoint(6, 7), GridPoint(4, 7))),
            Arrow("ex_r3_left", Direction.UP, listOf(GridPoint(3, 6), GridPoint(3, 4))),

            // Ring 4 (Center Pinwheel, indices 4..6)
            Arrow("ex_c_up", Direction.UP, listOf(GridPoint(4, 5), GridPoint(4, 4))),
            Arrow("ex_c_right", Direction.RIGHT, listOf(GridPoint(5, 4), GridPoint(6, 4))),
            Arrow("ex_c_down", Direction.DOWN, listOf(GridPoint(6, 5), GridPoint(6, 6))),
            Arrow("ex_c_left", Direction.LEFT, listOf(GridPoint(5, 6), GridPoint(4, 6)))
        )
        return applyDeterministicTransforms(
            Level(levelNumber, "Level $levelNumber", board, arrows, difficulty = 6, patternType = PatternType.THE_GAUNTLET.name),
            random
        )
    }

    // 100+: Endless Master Mode (Seed-directed procedural expansion)
    private fun buildEndlessMasterVariation(levelNumber: Int, random: Random): Level {
        val baseType = when ((levelNumber - 100).mod(6)) {
            0 -> PatternType.NESTED_ESCAPE
            1 -> PatternType.DEEP_CHAIN
            2 -> PatternType.MULTIPLE_OPENINGS
            3 -> PatternType.MISLEADING_CORRIDOR
            4 -> PatternType.SERPENTINE_CLUSTER
            else -> PatternType.THE_GAUNTLET
        }

        val baseLevel = when (baseType) {
            PatternType.NESTED_ESCAPE -> buildNestedVariation(levelNumber, random)
            PatternType.DEEP_CHAIN -> buildDeepChainVariation(levelNumber, random)
            PatternType.MULTIPLE_OPENINGS -> buildMultipleOpeningsVariation(levelNumber, random)
            PatternType.MISLEADING_CORRIDOR -> buildMisleadingCorridorVariation(levelNumber, random)
            PatternType.SERPENTINE_CLUSTER -> buildSerpentineClusterVariation(levelNumber, random)
            else -> buildExpertLabyrinthVariation(levelNumber, random)
        }

        return baseLevel.copy(
            id = levelNumber,
            name = "Level $levelNumber",
            difficulty = 6,
            patternType = PatternType.PROCEDURAL_MASTER.name
        )
    }

    private fun applyDeterministicTransforms(baseLevel: Level, random: Random): Level {
        var currentArrows = baseLevel.arrows
        var currentBoard = baseLevel.board

        // Deterministic rotation (0, 90, 180, 270)
        val rotation = random.nextInt(4)
        val (rotatedArrows, rotatedBoard) = when (rotation) {
            1 -> PatternTransform.rotate90(currentArrows, currentBoard)
            2 -> PatternTransform.rotate180(currentArrows, currentBoard)
            3 -> PatternTransform.rotate270(currentArrows, currentBoard)
            else -> Pair(currentArrows, currentBoard)
        }
        currentArrows = rotatedArrows
        currentBoard = rotatedBoard

        // Optional deterministic reflection
        if (random.nextBoolean()) {
            val (flipped, _) = PatternTransform.flipHorizontal(currentArrows, currentBoard)
            currentArrows = flipped
        }

        return baseLevel.copy(board = currentBoard, arrows = currentArrows)
    }
}
