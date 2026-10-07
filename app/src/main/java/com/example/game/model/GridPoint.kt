package com.example.game.model

/**
 * Represents a discrete 2D point on the puzzle grid.
 */
data class GridPoint(val x: Int, val y: Int) {
    fun plus(dir: Direction): GridPoint = GridPoint(x + dir.dx, y + dir.dy)
    fun plus(dx: Int, dy: Int): GridPoint = GridPoint(x + dx, y + dy)

    fun distanceTo(other: GridPoint): Double {
        val dx = (x - other.x).toDouble()
        val dy = (y - other.y).toDouble()
        return kotlin.math.sqrt(dx * dx + dy * dy)
    }
}
