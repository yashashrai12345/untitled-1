package com.example.game.generator.pattern

/**
 * The 20 Required Structural Geometric Families in "Arrows – Puzzle Escape".
 * Each family represents a fundamentally distinct spatial topology and silhouette.
 */
enum class PatternFamily(val displayName: String, val isBoxLike: Boolean = false) {
    WAVES("Waves"),
    ZIGZAG("Zigzag / Serpentine"),
    LADDER("Ladder / Rung"),
    STAIRCACE("Staircase"),
    SNAKE("Snake / Meander"),
    SPIRAL("Spiral-Like"),
    PINWHEEL("Pinwheel / Rotational"),
    STAR("Star / Burst"),
    FAN("Fan"),
    COMB("Comb"),
    FORK("Fork / Branch"),
    TREE("Tree / Hierarchical"),
    LETTER_SILHOUETTES("Abstract Silhouettes"),
    BROKEN_GRID("Broken Grid"),
    OFFSET_PARALLELS("Offset Parallels"),
    DIAGONAL_FLOW("Diagonal Composition"),
    TUNNEL_CORRIDOR("Tunnel / Corridor"),
    SWITCHBACK("Switchback"),
    CLUSTER("Cluster"),
    ASYMMETRIC_ORGANIC("Asymmetric Organic");
}
