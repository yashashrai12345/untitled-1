package com.example.game.model

/**
 * Represents an arrow entity on the board.
 * The polyline is defined by [points]. The arrowhead is located at [points.last()]
 * and points in [direction].
 */
data class Arrow(
    val id: String,
    val direction: Direction,
    val points: List<GridPoint>
) {
    init {
        require(points.size >= 2) { "Arrow must have at least 2 points (id: $id)" }
    }

    val head: GridPoint get() = points.last()
    val tail: GridPoint get() = points.first()

    /**
     * All line segments forming the arrow's body.
     */
    val segments: List<Pair<GridPoint, GridPoint>> by lazy {
        points.zipWithNext()
    }

    /**
     * Set of all integer grid points occupied by the arrow's body and head.
     */
    val occupiedPoints: Set<GridPoint> by lazy {
        val set = mutableSetOf<GridPoint>()
        for ((p1, p2) in segments) {
            if (p1.x == p2.x) {
                val minY = minOf(p1.y, p2.y)
                val maxY = maxOf(p1.y, p2.y)
                for (y in minY..maxY) {
                    set.add(GridPoint(p1.x, y))
                }
            } else if (p1.y == p2.y) {
                val minX = minOf(p1.x, p2.x)
                val maxX = maxOf(p1.x, p2.x)
                for (x in minX..maxX) {
                    set.add(GridPoint(x, p1.y))
                }
            } else {
                // Diagonal or arbitrary: add both endpoints
                set.add(p1)
                set.add(p2)
            }
        }
        set
    }

    /**
     * Checks if this arrow occupies a specific grid point.
     */
    fun occupies(point: GridPoint): Boolean = occupiedPoints.contains(point)

    /**
     * Number of 90-degree bends along the arrow's polyline.
     */
    val bendCount: Int get() = (points.size - 2).coerceAtLeast(0)

    /**
     * Total Manhattan length of the arrow's body in grid units.
     */
    val pathLength: Int get() = segments.sumOf { (p1, p2) ->
        kotlin.math.abs(p2.x - p1.x) + kotlin.math.abs(p2.y - p1.y)
    }
}
