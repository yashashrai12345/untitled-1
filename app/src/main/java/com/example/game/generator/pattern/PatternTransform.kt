package com.example.game.generator.pattern

import com.example.game.model.Arrow
import com.example.game.model.Board
import com.example.game.model.Direction
import com.example.game.model.GridPoint

object PatternTransform {

    fun rotate90(arrows: List<Arrow>, board: Board): Pair<List<Arrow>, Board> {
        val newBoard = Board(width = board.height, height = board.width)
        val transformed = arrows.map { arrow ->
            val newPoints = arrow.points.map { p ->
                GridPoint(x = board.height - 1 - p.y, y = p.x)
            }
            val newDir = when (arrow.direction) {
                Direction.UP -> Direction.RIGHT
                Direction.RIGHT -> Direction.DOWN
                Direction.DOWN -> Direction.LEFT
                Direction.LEFT -> Direction.UP
            }
            Arrow(id = arrow.id, direction = newDir, points = newPoints)
        }
        return Pair(transformed, newBoard)
    }

    fun rotate180(arrows: List<Arrow>, board: Board): Pair<List<Arrow>, Board> {
        val transformed = arrows.map { arrow ->
            val newPoints = arrow.points.map { p ->
                GridPoint(x = board.width - 1 - p.x, y = board.height - 1 - p.y)
            }
            val newDir = when (arrow.direction) {
                Direction.UP -> Direction.DOWN
                Direction.DOWN -> Direction.UP
                Direction.LEFT -> Direction.RIGHT
                Direction.RIGHT -> Direction.LEFT
            }
            Arrow(id = arrow.id, direction = newDir, points = newPoints)
        }
        return Pair(transformed, board)
    }

    fun rotate270(arrows: List<Arrow>, board: Board): Pair<List<Arrow>, Board> {
        val newBoard = Board(width = board.height, height = board.width)
        val transformed = arrows.map { arrow ->
            val newPoints = arrow.points.map { p ->
                GridPoint(x = p.y, y = board.width - 1 - p.x)
            }
            val newDir = when (arrow.direction) {
                Direction.UP -> Direction.LEFT
                Direction.LEFT -> Direction.DOWN
                Direction.DOWN -> Direction.RIGHT
                Direction.RIGHT -> Direction.UP
            }
            Arrow(id = arrow.id, direction = newDir, points = newPoints)
        }
        return Pair(transformed, newBoard)
    }

    fun flipHorizontal(arrows: List<Arrow>, board: Board): Pair<List<Arrow>, Board> {
        val transformed = arrows.map { arrow ->
            val newPoints = arrow.points.map { p ->
                GridPoint(x = board.width - 1 - p.x, y = p.y)
            }
            val newDir = when (arrow.direction) {
                Direction.LEFT -> Direction.RIGHT
                Direction.RIGHT -> Direction.LEFT
                Direction.UP -> Direction.UP
                Direction.DOWN -> Direction.DOWN
            }
            Arrow(id = arrow.id, direction = newDir, points = newPoints)
        }
        return Pair(transformed, board)
    }

    fun flipVertical(arrows: List<Arrow>, board: Board): Pair<List<Arrow>, Board> {
        val transformed = arrows.map { arrow ->
            val newPoints = arrow.points.map { p ->
                GridPoint(x = p.x, y = board.height - 1 - p.y)
            }
            val newDir = when (arrow.direction) {
                Direction.UP -> Direction.DOWN
                Direction.DOWN -> Direction.UP
                Direction.LEFT -> Direction.LEFT
                Direction.RIGHT -> Direction.RIGHT
            }
            Arrow(id = arrow.id, direction = newDir, points = newPoints)
        }
        return Pair(transformed, board)
    }

    fun translate(arrows: List<Arrow>, dx: Int, dy: Int, board: Board): List<Arrow>? {
        val shifted = arrows.map { arrow ->
            val newPoints = arrow.points.map { p ->
                val nx = p.x + dx
                val ny = p.y + dy
                if (nx < 0 || nx >= board.width || ny < 0 || ny >= board.height) {
                    return null // Out of bounds
                }
                GridPoint(nx, ny)
            }
            Arrow(id = arrow.id, direction = arrow.direction, points = newPoints)
        }
        return shifted
    }
}
