package com.example.game.animation

import com.example.game.model.Arrow
import com.example.game.model.Board
import com.example.game.model.Direction
import com.example.game.model.GridPoint
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.hypot

/**
 * 2D point representation for smooth sub-pixel vector animation.
 */
data class EscapePoint(val x: Float, val y: Float)

/**
 * Snapshot of the snake's sampled polyline at a specific frame.
 */
data class SnakeSnapshot(
    val points: List<EscapePoint>,
    val headPoint: EscapePoint,
    val headAngleRad: Float,
    val tailPoint: EscapePoint,
    val length: Float
)

internal data class ArcLengthSample(
    val point: EscapePoint,
    val segmentIndex: Int,
    val tangentAngleRad: Float
)

/**
 * Precomputed geometric escape track that an arrow follows like a snake.
 */
data class EscapeTrack(
    val vertices: List<EscapePoint>,
    val cumulativeDistances: FloatArray,
    val initialArrowLength: Float,
    val totalEscapeDistance: Float,
    val direction: Direction
) {
    /**
     * Samples the snake's polyline at a given [distanceTraveled] (in grid units).
     * Returns the polyline points from tail to head, along with the head tangent orientation.
     */
    fun sampleSnake(distanceTraveled: Float): SnakeSnapshot {
        val sTail = distanceTraveled.coerceAtLeast(0f)
        val sHead = sTail + initialArrowLength

        val tailSample = pointAtArcLength(sTail)
        val headSample = pointAtArcLength(sHead)

        val points = ArrayList<EscapePoint>(vertices.size + 2)
        points.add(tailSample.point)

        // Include any intermediate track corner vertices that the body is currently traversing
        if (tailSample.segmentIndex < headSample.segmentIndex) {
            for (j in (tailSample.segmentIndex + 1)..headSample.segmentIndex) {
                val vertex = vertices[j]
                // Prevent duplicate points within epsilon
                val last = points.last()
                if (hypot(vertex.x - last.x, vertex.y - last.y) > 0.01f) {
                    points.add(vertex)
                }
            }
        }

        // Add the leading head point
        val last = points.last()
        if (hypot(headSample.point.x - last.x, headSample.point.y - last.y) > 0.01f) {
            points.add(headSample.point)
        } else if (points.size == 1) {
            // Ensure at least 2 points for a valid polyline
            points.add(headSample.point)
        }

        return SnakeSnapshot(
            points = points,
            headPoint = headSample.point,
            headAngleRad = headSample.tangentAngleRad,
            tailPoint = tailSample.point,
            length = initialArrowLength
        )
    }

    private fun pointAtArcLength(s: Float): ArcLengthSample {
        val totalLength = cumulativeDistances.last()
        val clampedS = s.coerceIn(0f, totalLength)

        // Locate segment index
        var segIndex = 0
        for (i in 0 until cumulativeDistances.size - 1) {
            if (clampedS >= cumulativeDistances[i] && clampedS <= cumulativeDistances[i + 1]) {
                segIndex = i
                break
            }
        }

        val d0 = cumulativeDistances[segIndex]
        val d1 = cumulativeDistances[segIndex + 1]
        val segLen = (d1 - d0).coerceAtLeast(0.0001f)
        val t = ((clampedS - d0) / segLen).coerceIn(0f, 1f)

        val v1 = vertices[segIndex]
        val v2 = vertices[segIndex + 1]

        val px = v1.x + (v2.x - v1.x) * t
        val py = v1.y + (v2.y - v1.y) * t

        val currentDx = v2.x - v1.x
        val currentDy = v2.y - v1.y
        var currentAngle = atan2(currentDy, currentDx)

        // Smooth angular interpolation when approaching a corner (within 0.25 grid units)
        val cornerBlendDist = 0.25f
        if (segIndex < cumulativeDistances.size - 2) {
            val distToCorner = d1 - clampedS
            if (distToCorner in 0f..cornerBlendDist) {
                val v3 = vertices[segIndex + 2]
                val nextDx = v3.x - v2.x
                val nextDy = v3.y - v2.y
                val nextAngle = atan2(nextDy, nextDx)

                var angleDiff = (nextAngle - currentAngle) % (2f * PI.toFloat())
                if (angleDiff > PI.toFloat()) angleDiff -= 2f * PI.toFloat()
                if (angleDiff < -PI.toFloat()) angleDiff += 2f * PI.toFloat()

                val blendFraction = (1f - (distToCorner / cornerBlendDist)) * 0.5f
                currentAngle += angleDiff * blendFraction
            }
        }

        return ArcLengthSample(
            point = EscapePoint(px, py),
            segmentIndex = segIndex,
            tangentAngleRad = currentAngle
        )
    }
}

object SnakePathCalculator {

    /**
     * Builds the complete polyline track for [arrow] escaping through [board].
     * The track starts at the arrow's tail, follows every corner of the arrow's body to its head,
     * and extends straight outward in [arrow.direction] until the entire arrow has left the board.
     */
    fun buildEscapeTrack(
        arrow: Arrow,
        board: Board,
        padding: Float = 2.2f
    ): EscapeTrack {
        val points = arrow.points
        require(points.size >= 2) { "Arrow must have at least 2 points" }

        // 1. Initial arrow vertices
        val vertices = mutableListOf<EscapePoint>()
        for (p in points) {
            vertices.add(EscapePoint(p.x.toFloat(), p.y.toFloat()))
        }

        // 2. Compute initial arrow body length
        var initialLength = 0f
        for (i in 0 until vertices.size - 1) {
            val v1 = vertices[i]
            val v2 = vertices[i + 1]
            initialLength += hypot(v2.x - v1.x, v2.y - v1.y)
        }

        // 3. Compute distance from arrowhead to the outside edge of the board
        val head = arrow.head
        val boardMaxX = (board.width - 1).toFloat()
        val boardMaxY = (board.height - 1).toFloat()

        val headDistToExit = when (arrow.direction) {
            Direction.RIGHT -> (boardMaxX + padding) - head.x.toFloat()
            Direction.LEFT -> head.x.toFloat() - (-padding)
            Direction.DOWN -> (boardMaxY + padding) - head.y.toFloat()
            Direction.UP -> head.y.toFloat() - (-padding)
        }.coerceAtLeast(1.5f)

        // Total travel distance for the TAIL to also pass the exit boundary
        val totalEscapeDistance = headDistToExit + initialLength + 0.6f

        // 4. Extend the track straight off the board in arrow.direction
        val exitPoint = EscapePoint(
            x = head.x.toFloat() + arrow.direction.dx * (totalEscapeDistance + 3.0f),
            y = head.y.toFloat() + arrow.direction.dy * (totalEscapeDistance + 3.0f)
        )
        vertices.add(exitPoint)

        // 5. Compute cumulative arc-length distances for all track vertices
        val cumulativeDistances = FloatArray(vertices.size)
        cumulativeDistances[0] = 0f
        for (i in 1 until vertices.size) {
            val v1 = vertices[i - 1]
            val v2 = vertices[i]
            val segLen = hypot(v2.x - v1.x, v2.y - v1.y)
            cumulativeDistances[i] = cumulativeDistances[i - 1] + segLen
        }

        return EscapeTrack(
            vertices = vertices,
            cumulativeDistances = cumulativeDistances,
            initialArrowLength = initialLength,
            totalEscapeDistance = totalEscapeDistance,
            direction = arrow.direction
        )
    }
}
