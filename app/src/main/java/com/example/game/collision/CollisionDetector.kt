package com.example.game.collision

import com.example.game.model.Arrow
import com.example.game.model.Board
import com.example.game.model.GridPoint
import kotlin.math.hypot

object CollisionDetector {

    /**
     * Determines whether the given [arrow] can legally escape off the board
     * in its movement direction without colliding with any other active arrows.
     *
     * @param arrow The arrow attempting to escape.
     * @param activeArrows All currently active arrows on the board (including [arrow]).
     * @param board The puzzle board bounds.
     * @return true if the movement corridor is completely clear, false if blocked.
     */
    fun canArrowEscape(
        arrow: Arrow,
        activeArrows: List<Arrow>,
        board: Board
    ): Boolean {
        val otherArrows = activeArrows.filter { it.id != arrow.id }
        if (otherArrows.isEmpty()) return true

        val dx = arrow.direction.dx
        val dy = arrow.direction.dy

        // Build a fast lookup set of all points occupied by obstacles
        val obstaclePoints = mutableSetOf<GridPoint>()
        for (other in otherArrows) {
            obstaclePoints.addAll(other.occupiedPoints)
        }

        // Primary corridor check: Ahead of the arrowhead towards board edge
        val maxSteps = maxOf(board.width, board.height) + 2
        for (k in 1..maxSteps) {
            val corridorPoint = GridPoint(arrow.head.x + k * dx, arrow.head.y + k * dy)
            if (obstaclePoints.contains(corridorPoint)) {
                return false
            }
        }

        return true
    }

    /**
     * Finds which other arrow is blocking [arrow], if any.
     * Useful for debugging, visual emphasis, or smart hints.
     */
    fun findBlockingArrow(
        arrow: Arrow,
        activeArrows: List<Arrow>,
        board: Board
    ): Arrow? {
        val otherArrows = activeArrows.filter { it.id != arrow.id }
        val dx = arrow.direction.dx
        val dy = arrow.direction.dy
        val maxSteps = maxOf(board.width, board.height) + 2

        // Check directly ahead of head
        for (k in 1..maxSteps) {
            val corridorPoint = GridPoint(arrow.head.x + k * dx, arrow.head.y + k * dy)
            for (other in otherArrows) {
                if (other.occupies(corridorPoint)) {
                    return other
                }
            }
        }

        return null
    }

    /**
     * Calculates the exact distance (in grid units) from [arrow.head] to the nearest blocking obstacle
     * along its movement direction, stopping at a safe visual clearance (0.35 units before impact).
     *
     * @return Distance in grid units for the failed move snake animation to travel forward before impact.
     */
    fun calculateCollisionDistance(
        arrow: Arrow,
        activeArrows: List<Arrow>,
        board: Board
    ): Float {
        val otherArrows = activeArrows.filter { it.id != arrow.id }
        val dx = arrow.direction.dx
        val dy = arrow.direction.dy
        val maxSteps = maxOf(board.width, board.height) + 2

        for (k in 1..maxSteps) {
            val corridorPoint = GridPoint(arrow.head.x + k * dx, arrow.head.y + k * dy)
            if (otherArrows.any { it.occupies(corridorPoint) }) {
                // Nearest obstacle is at distance k grid units.
                // Stop at safe visual clearance (0.35 cell units) so the arrowhead does not penetrate into the blocker.
                return (k - 0.35f).coerceAtLeast(0.35f)
            }
        }

        return 0.5f
    }

    /**
     * Identifies the closest active arrow tapped by the user within [touchTolerancePx].
     *
     * @param tapX Screen X pixel of the tap.
     * @param tapY Screen Y pixel of the tap.
     * @param activeArrows List of currently active arrows.
     * @param boardOriginX Pixel X origin of the board (grid 0,0).
     * @param boardOriginY Pixel Y origin of the board (grid 0,0).
     * @param cellSize Size of one grid cell in pixels.
     * @param touchTolerancePx Maximum allowable distance in pixels to register a tap.
     * @return The tapped [Arrow], or null if no arrow was close enough.
     */
    fun findTappedArrow(
        tapX: Float,
        tapY: Float,
        activeArrows: List<Arrow>,
        boardOriginX: Float,
        boardOriginY: Float,
        cellSize: Float,
        touchTolerancePx: Float
    ): Arrow? {
        if (activeArrows.isEmpty() || cellSize <= 0f) return null

        val gridX = (tapX - boardOriginX) / cellSize
        val gridY = (tapY - boardOriginY) / cellSize
        val toleranceInGridUnits = touchTolerancePx / cellSize

        var closestArrow: Arrow? = null
        var minDistance = Float.MAX_VALUE

        for (arrow in activeArrows) {
            for (segment in arrow.segments) {
                val dist = distanceToSegment(
                    px = gridX,
                    py = gridY,
                    x1 = segment.first.x.toFloat(),
                    y1 = segment.first.y.toFloat(),
                    x2 = segment.second.x.toFloat(),
                    y2 = segment.second.y.toFloat()
                )
                if (dist < minDistance && dist <= toleranceInGridUnits) {
                    minDistance = dist
                    closestArrow = arrow
                }
            }
        }

        return closestArrow
    }

    private fun distanceToSegment(
        px: Float, py: Float,
        x1: Float, y1: Float,
        x2: Float, y2: Float
    ): Float {
        val dx = x2 - x1
        val dy = y2 - y1
        val lenSq = dx * dx + dy * dy
        if (lenSq == 0f) {
            return hypot(px - x1, py - y1)
        }

        val t = (((px - x1) * dx + (py - y1) * dy) / lenSq).coerceIn(0f, 1f)
        val projX = x1 + t * dx
        val projY = y1 + t * dy
        return hypot(px - projX, py - projY)
    }
}
