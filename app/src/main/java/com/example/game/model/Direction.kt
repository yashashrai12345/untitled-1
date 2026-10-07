package com.example.game.model

/**
 * 4-way orthogonal movement direction for arrows.
 */
enum class Direction(val dx: Int, val dy: Int, val degrees: Float) {
    UP(0, -1, 270f),
    RIGHT(1, 0, 0f),
    DOWN(0, 1, 90f),
    LEFT(-1, 0, 180f);

    val opposite: Direction
        get() = when (this) {
            UP -> DOWN
            DOWN -> UP
            LEFT -> RIGHT
            RIGHT -> LEFT
        }

    companion object {
        fun fromDelta(dx: Int, dy: Int): Direction? {
            return when {
                dx > 0 && dy == 0 -> RIGHT
                dx < 0 && dy == 0 -> LEFT
                dx == 0 && dy > 0 -> DOWN
                dx == 0 && dy < 0 -> UP
                else -> null
            }
        }
    }
}
