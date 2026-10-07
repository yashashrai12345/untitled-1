package com.example.game.model

/**
 * Represents the bounds and dimensions of the puzzle board.
 */
data class Board(
    val width: Int,
    val height: Int
) {
    init {
        require(width >= 3 && height >= 3) { "Board dimensions must be at least 3x3" }
    }

    fun isInside(point: GridPoint): Boolean {
        return point.x in 0 until width && point.y in 0 until height
    }
}
