package com.example.game.validation

import com.example.game.model.Arrow
import com.example.game.model.Board
import com.example.game.model.GridPoint
import com.example.game.model.Level
import com.example.game.solver.PuzzleSolver
import kotlin.math.hypot

/**
 * Validates that puzzle levels contain ABSOLUTELY NO OVERLAPPING ARROWS,
 * no path crossings, no arrowhead collisions, and maintain strict visual clearance.
 */
object LevelOverlapValidator {

    /**
     * Minimum clearance in grid cells required between distinct arrows
     * to prevent visual touching or ambiguous proximity.
     */
    const val MIN_ARROW_CLEARANCE = 0.35f

    data class ValidationResult(
        val isValid: Boolean,
        val reason: String? = null
    )

    /**
     * Complete level validation pipeline:
     * BOUNDS -> PATH VALIDITY -> ARROW OVERLAP -> ARROWHEAD OVERLAP -> CLEARANCE -> SOLVABILITY
     */
    fun validateLevel(level: Level, minClearance: Float = MIN_ARROW_CLEARANCE): ValidationResult {
        val board = level.board
        val arrows = level.arrows

        if (arrows.isEmpty()) {
            return ValidationResult(false, "Level has no arrows")
        }

        // 1. Check bounds and individual arrow validity
        for (arrow in arrows) {
            if (arrow.points.size < 2) {
                return ValidationResult(false, "Arrow ${arrow.id} has fewer than 2 points")
            }
            for (p in arrow.points) {
                if (p.x < 0 || p.x >= board.width || p.y < 0 || p.y >= board.height) {
                    return ValidationResult(false, "Arrow ${arrow.id} point $p is outside board bounds (${board.width}x${board.height})")
                }
            }
            // Check self-intersection within arrow
            for (i in 0 until arrow.segments.size) {
                for (j in (i + 2) until arrow.segments.size) {
                    val s1 = arrow.segments[i]
                    val s2 = arrow.segments[j]
                    if (distanceBetweenSegments(
                            s1.first.x.toFloat(), s1.first.y.toFloat(), s1.second.x.toFloat(), s1.second.y.toFloat(),
                            s2.first.x.toFloat(), s2.first.y.toFloat(), s2.second.x.toFloat(), s2.second.y.toFloat()
                        ) < minClearance
                    ) {
                        return ValidationResult(false, "Arrow ${arrow.id} self-intersects or violates clearance with itself")
                    }
                }
            }
        }

        // 2. Check pairs of distinct arrows for overlap, crossing, or clearance violations
        for (i in 0 until arrows.size) {
            val a1 = arrows[i]
            for (j in (i + 1) until arrows.size) {
                val a2 = arrows[j]
                val clearResult = checkArrowsClearance(a1, a2, minClearance)
                if (!clearResult.isValid) {
                    return ValidationResult(false, "Overlap detected between ${a1.id} and ${a2.id}: ${clearResult.reason}")
                }
            }
        }

        // 3. Solvability check
        val solveResult = PuzzleSolver.solvePuzzle(arrows, board)
        if (!solveResult.isSolvable || solveResult.solutionSequence.size != arrows.size) {
            return ValidationResult(false, "Level is not fully solvable without collisions")
        }

        return ValidationResult(true)
    }

    /**
     * Checks if two arrows are visually and geometrically separate with sufficient clearance.
     */
    fun areArrowsClear(arrowA: Arrow, arrowB: Arrow, minClearance: Float = MIN_ARROW_CLEARANCE): Boolean {
        return checkArrowsClearance(arrowA, arrowB, minClearance).isValid
    }

    private fun checkArrowsClearance(
        arrowA: Arrow,
        arrowB: Arrow,
        minClearance: Float
    ): ValidationResult {
        if (arrowA.id == arrowB.id) return ValidationResult(true)

        // 1. Same-grid-cell occupation check
        val shared = arrowA.occupiedPoints.intersect(arrowB.occupiedPoints)
        if (shared.isNotEmpty()) {
            return ValidationResult(false, "Shared grid points: $shared")
        }

        // 2. Check every segment of A against every segment of B
        for (segA in arrowA.segments) {
            val ax1 = segA.first.x.toFloat()
            val ay1 = segA.first.y.toFloat()
            val ax2 = segA.second.x.toFloat()
            val ay2 = segA.second.y.toFloat()

            for (segB in arrowB.segments) {
                val bx1 = segB.first.x.toFloat()
                val by1 = segB.first.y.toFloat()
                val bx2 = segB.second.x.toFloat()
                val by2 = segB.second.y.toFloat()

                // Check for direct geometric intersection / crossing
                if (segmentsIntersect(ax1, ay1, ax2, ay2, bx1, by1, bx2, by2)) {
                    return ValidationResult(false, "Segments intersect: $segA and $segB")
                }

                // Check minimum Euclidean clearance
                val dist = distanceBetweenSegments(ax1, ay1, ax2, ay2, bx1, by1, bx2, by2)
                if (dist < minClearance) {
                    return ValidationResult(false, "Clearance between $segA and $segB is $dist < $minClearance")
                }
            }
        }

        // 3. Check arrowhead tip clearances (arrowhead tip extends forward by 0.25 grid units)
        val tipA_X = arrowA.head.x.toFloat() + arrowA.direction.dx * 0.25f
        val tipA_Y = arrowA.head.y.toFloat() + arrowA.direction.dy * 0.25f
        for (segB in arrowB.segments) {
            val dist = distancePointToSegment(
                tipA_X, tipA_Y,
                segB.first.x.toFloat(), segB.first.y.toFloat(),
                segB.second.x.toFloat(), segB.second.y.toFloat()
            )
            if (dist < minClearance) {
                return ValidationResult(false, "Arrowhead tip of ${arrowA.id} too close to segment $segB of ${arrowB.id}")
            }
        }

        val tipB_X = arrowB.head.x.toFloat() + arrowB.direction.dx * 0.25f
        val tipB_Y = arrowB.head.y.toFloat() + arrowB.direction.dy * 0.25f
        for (segA in arrowA.segments) {
            val dist = distancePointToSegment(
                tipB_X, tipB_Y,
                segA.first.x.toFloat(), segA.first.y.toFloat(),
                segA.second.x.toFloat(), segA.second.y.toFloat()
            )
            if (dist < minClearance) {
                return ValidationResult(false, "Arrowhead tip of ${arrowB.id} too close to segment $segA of ${arrowA.id}")
            }
        }

        return ValidationResult(true)
    }

    /**
     * Determines whether two 2D line segments cross or touch.
     */
    fun segmentsIntersect(
        x1: Float, y1: Float, x2: Float, y2: Float,
        x3: Float, y3: Float, x4: Float, y4: Float
    ): Boolean {
        val d1 = ccw(x3, y3, x4, y4, x1, y1)
        val d2 = ccw(x3, y3, x4, y4, x2, y2)
        val d3 = ccw(x1, y1, x2, y2, x3, y3)
        val d4 = ccw(x1, y1, x2, y2, x4, y4)

        if (((d1 > 0.001f && d2 < -0.001f) || (d1 < -0.001f && d2 > 0.001f)) &&
            ((d3 > 0.001f && d4 < -0.001f) || (d3 < -0.001f && d4 > 0.001f))
        ) {
            return true
        }

        // Collinear or endpoint touching checks
        if (distancePointToSegment(x1, y1, x3, y3, x4, y4) < 0.001f) return true
        if (distancePointToSegment(x2, y2, x3, y3, x4, y4) < 0.001f) return true
        if (distancePointToSegment(x3, y3, x1, y1, x2, y2) < 0.001f) return true
        if (distancePointToSegment(x4, y4, x1, y1, x2, y2) < 0.001f) return true

        return false
    }

    private fun ccw(ax: Float, ay: Float, bx: Float, by: Float, cx: Float, cy: Float): Float {
        return (bx - ax) * (cy - ay) - (by - ay) * (cx - ax)
    }

    /**
     * Shortest Euclidean distance between two 2D line segments.
     */
    fun distanceBetweenSegments(
        x1: Float, y1: Float, x2: Float, y2: Float,
        x3: Float, y3: Float, x4: Float, y4: Float
    ): Float {
        if (segmentsIntersect(x1, y1, x2, y2, x3, y3, x4, y4)) {
            return 0f
        }

        val d1 = distancePointToSegment(x1, y1, x3, y3, x4, y4)
        val d2 = distancePointToSegment(x2, y2, x3, y3, x4, y4)
        val d3 = distancePointToSegment(x3, y3, x1, y1, x2, y2)
        val d4 = distancePointToSegment(x4, y4, x1, y1, x2, y2)

        return minOf(d1, d2, d3, d4)
    }

    /**
     * Distance from point (px, py) to line segment (x1, y1)-(x2, y2).
     */
    fun distancePointToSegment(
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
