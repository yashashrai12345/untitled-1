package com.example.game.generator.pattern

/**
 * Categorization of puzzle pattern structures in "Arrows – Puzzle Escape".
 */
enum class PatternType(val displayName: String, val category: PatternCategory) {
    // Group 1: Tutorial
    TUTORIAL_STRAIGHT("Tutorial Straight", PatternCategory.TUTORIAL),
    TUTORIAL_BLOCKER("Tutorial Blocker", PatternCategory.TUTORIAL),
    TUTORIAL_CHAIN("Tutorial Chain", PatternCategory.TUTORIAL),
    TUTORIAL_CROSSROADS("Tutorial Crossroads", PatternCategory.TUTORIAL),

    // Group 2: Bends & Corners
    BASIC_L("L-Shape", PatternCategory.BENDS),
    REVERSE_L("Reverse L-Shape", PatternCategory.BENDS),
    CORNER_TURN("Corner Turn", PatternCategory.BENDS),
    STAIRCACE("Staircase", PatternCategory.BENDS),
    DOUBLE_BEND("Double Bend", PatternCategory.BENDS),

    // Group 3: Corridors & Parallels
    DOUBLE_CORRIDOR("Double Corridor", PatternCategory.CORRIDORS),
    REVERSE_LADDER("Reverse Ladder", PatternCategory.CORRIDORS),
    PARALLEL_LINES("Parallel Lines", PatternCategory.CORRIDORS),
    VERTICAL_SHAFTS("Vertical Shafts", PatternCategory.CORRIDORS),
    CROSSED_CORRIDORS("Crossed Corridors", PatternCategory.CORRIDORS),

    // Group 4: U, C, S Shapes
    SIMPLE_U("U-Shape", PatternCategory.CURVES),
    REVERSE_U("Inverted U-Shape", PatternCategory.CURVES),
    C_SHAPE("C-Shape", PatternCategory.CURVES),
    FLOWING_S("Flowing S", PatternCategory.CURVES),
    TRIPLE_GATE("Triple Gate", PatternCategory.CURVES),
    ZIGZAG("Zig-Zag", PatternCategory.CURVES),

    // Group 5: Nested Patterns
    NESTED_U("Nested U", PatternCategory.NESTED),
    NESTED_ESCAPE("Nested Escape", PatternCategory.NESTED),
    CENTRAL_LOCK("Central Lock", PatternCategory.NESTED),
    PARALLEL_NESTED("Parallel Nested", PatternCategory.NESTED),
    CONCENTRIC_MAZE("Concentric Maze", PatternCategory.NESTED),

    // Group 6: Deep Dependency Chains
    FOUR_CORNER("Four Corner", PatternCategory.CHAINS),
    BRANCHING_TREE("Branching Tree", PatternCategory.CHAINS),
    MIRROR_TRAP("Mirror Trap", PatternCategory.CHAINS),
    BROKEN_SPIRAL("Broken Spiral", PatternCategory.CHAINS),
    DEEP_CHAIN("Deep Chain", PatternCategory.CHAINS),

    // Group 7: Multiple Openings
    MULTIPLE_OPENINGS("Multiple Openings", PatternCategory.BRANCHING),
    SYMMETRIC_CHOICE("Symmetric Choice", PatternCategory.BRANCHING),

    // Group 8: Misleading Layouts
    MISLEADING_CORRIDOR("Misleading Corridor", PatternCategory.DECEPTIVE),
    FALSE_EXIT("False Exit", PatternCategory.DECEPTIVE),

    // Group 9: Dense Patterns
    SERPENTINE_CLUSTER("Serpentine Cluster", PatternCategory.DENSE),
    TWIN_SPIRAL("Twin Spiral", PatternCategory.DENSE),
    DENSE_GRID("Dense Grid", PatternCategory.DENSE),

    // Group 10: Advanced Mazes
    THE_GAUNTLET("The Gauntlet", PatternCategory.ADVANCED),
    EXPERT_LABYRINTH("Expert Labyrinth", PatternCategory.ADVANCED),
    PROCEDURAL_MASTER("Procedural Master", PatternCategory.ENDLESS),

    // 20 Required Geometric Pattern Families (Box-free layouts)
    WAVES("Waves", PatternCategory.CURVES),
    LADDER("Ladder / Rung", PatternCategory.CORRIDORS),
    SNAKE("Snake / Meander", PatternCategory.CURVES),
    SPIRAL("Spiral-Like", PatternCategory.CHAINS),
    PINWHEEL("Pinwheel / Rotational", PatternCategory.BRANCHING),
    STAR("Star / Burst", PatternCategory.BRANCHING),
    FAN("Fan", PatternCategory.BRANCHING),
    COMB("Comb", PatternCategory.CORRIDORS),
    FORK("Fork / Branch", PatternCategory.BRANCHING),
    TREE("Tree / Hierarchical", PatternCategory.BRANCHING),
    LETTER_SILHOUETTES("Abstract Silhouettes", PatternCategory.ADVANCED),
    BROKEN_GRID("Broken Grid", PatternCategory.DECEPTIVE),
    OFFSET_PARALLELS("Offset Parallels", PatternCategory.CORRIDORS),
    DIAGONAL_FLOW("Diagonal Composition", PatternCategory.ADVANCED),
    TUNNEL_CORRIDOR("Tunnel / Corridor", PatternCategory.CORRIDORS),
    SWITCHBACK("Switchback", PatternCategory.CURVES),
    CLUSTER("Cluster", PatternCategory.DENSE),
    ASYMMETRIC_ORGANIC("Asymmetric Organic", PatternCategory.ADVANCED);

    companion object {
        fun forLevel(levelNumber: Int): PatternType {
            return when (levelNumber) {
                1 -> TUTORIAL_STRAIGHT
                2 -> TUTORIAL_BLOCKER
                3 -> TUTORIAL_CHAIN
                4 -> TUTORIAL_CROSSROADS
                5 -> BASIC_L
                6 -> REVERSE_L
                7 -> CORNER_TURN
                8 -> STAIRCACE
                9 -> DOUBLE_BEND
                10 -> DOUBLE_CORRIDOR
                11 -> DOUBLE_CORRIDOR
                12 -> REVERSE_LADDER
                13 -> PARALLEL_LINES
                14 -> VERTICAL_SHAFTS
                15 -> CROSSED_CORRIDORS
                16 -> SIMPLE_U
                17 -> REVERSE_U
                18 -> C_SHAPE
                19 -> FLOWING_S
                20 -> TRIPLE_GATE
                21 -> NESTED_U
                22 -> NESTED_ESCAPE
                23 -> CENTRAL_LOCK
                24 -> PARALLEL_NESTED
                25 -> CONCENTRIC_MAZE
                26 -> FOUR_CORNER
                27 -> BRANCHING_TREE
                28 -> MIRROR_TRAP
                29 -> BROKEN_SPIRAL
                30 -> DEEP_CHAIN
                in 31..40 -> NESTED_ESCAPE
                in 41..50 -> DEEP_CHAIN
                in 51..60 -> MULTIPLE_OPENINGS
                in 61..70 -> MISLEADING_CORRIDOR
                in 71..80 -> SERPENTINE_CLUSTER
                in 81..90 -> TWIN_SPIRAL
                in 91..100 -> EXPERT_LABYRINTH
                else -> PROCEDURAL_MASTER
            }
        }
    }
}

enum class PatternCategory {
    TUTORIAL,
    BENDS,
    CORRIDORS,
    CURVES,
    NESTED,
    CHAINS,
    BRANCHING,
    DECEPTIVE,
    DENSE,
    ADVANCED,
    ENDLESS
}
