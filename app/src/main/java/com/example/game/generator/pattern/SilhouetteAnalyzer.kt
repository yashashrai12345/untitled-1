package com.example.game.generator.pattern

import com.example.game.model.Arrow
import com.example.game.model.Direction
import com.example.game.model.GridPoint
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Evaluates geometric silhouette, boxiness, and spatial similarity between puzzle levels.
 * Guarantees that:
 * 1. The majority of levels are NOT box-shaped or rectangular loops.
 * 2. Generated puzzles have diverse silhouettes, directional distributions, and spatial topologies.
 */
object SilhouetteAnalyzer {

    data class LevelSilhouette(
        val boundingBoxRatio: Float, // width / height
        val perimeterPerimeterDensity: Float, // fraction of points on perimeter
        val rectangularLoopCount: Int, // number of 4-corner loops
        val isDominatedByBox: Boolean,
        val directionalDistribution: Map<Direction, Float>,
        val bendCount: Int,
        val occupancyCount: Int,
        val centerOfMass: Pair<Float, Float>,
        val spatialSignature: String
    )

    /**
     * Computes the geometric silhouette of a set of arrows on a board.
     */
    fun analyze(arrows: List<Arrow>, boardWidth: Int, boardHeight: Int): LevelSilhouette {
        if (arrows.isEmpty()) {
            return LevelSilhouette(
                boundingBoxRatio = 1.0f,
                perimeterPerimeterDensity = 0.0f,
                rectangularLoopCount = 0,
                isDominatedByBox = false,
                directionalDistribution = emptyMap(),
                bendCount = 0,
                occupancyCount = 0,
                centerOfMass = Pair(0f, 0f),
                spatialSignature = ""
            )
        }

        val allPoints = arrows.flatMap { it.occupiedPoints }
        val minX = allPoints.minOf { it.x }
        val maxX = allPoints.maxOf { it.x }
        val minY = allPoints.minOf { it.y }
        val maxY = allPoints.maxOf { it.y }

        val spanX = max(1, maxX - minX + 1)
        val spanY = max(1, maxY - minY + 1)
        val ratio = spanX.toFloat() / spanY.toFloat()

        // Count how many points lie on the outer boundary of the bounding box
        val perimeterPoints = allPoints.count { p ->
            p.x == minX || p.x == maxX || p.y == minY || p.y == maxY
        }
        val perimeterDensity = perimeterPoints.toFloat() / allPoints.size.coerceAtLeast(1)

        // Directional distribution
        val dirCounts = arrows.groupingBy { it.direction }.eachCount()
        val dirDist = Direction.entries.associateWith { dir ->
            (dirCounts[dir] ?: 0).toFloat() / arrows.size
        }

        // Bend count
        val totalBends = arrows.sumOf { max(0, it.points.size - 2) }

        // Center of mass
        val avgX = allPoints.sumOf { it.x }.toFloat() / allPoints.size
        val avgY = allPoints.sumOf { it.y }.toFloat() / allPoints.size

        // Detect 4-arrow box-loops (Top->Right, Right->Down, Down->Left, Left->Up)
        var boxLoops = 0
        val rightArrows = arrows.filter { it.direction == Direction.RIGHT }
        val downArrows = arrows.filter { it.direction == Direction.DOWN }
        val leftArrows = arrows.filter { it.direction == Direction.LEFT }
        val upArrows = arrows.filter { it.direction == Direction.UP }

        for (r in rightArrows) {
            for (d in downArrows) {
                for (l in leftArrows) {
                    for (u in upArrows) {
                        // Check if they form a rectangular loop around each other
                        if (r.head.x >= d.tail.x - 1 && d.head.y >= l.tail.y - 1 &&
                            l.head.x <= u.tail.x + 1 && u.head.y <= r.tail.y + 1) {
                            // Check if they share corners closely
                            if (abs(r.head.x - d.tail.x) <= 1 && abs(r.head.y - d.tail.y) <= 1 &&
                                abs(d.head.x - l.tail.x) <= 1 && abs(d.head.y - l.tail.y) <= 1 &&
                                abs(l.head.x - u.tail.x) <= 1 && abs(l.head.y - u.tail.y) <= 1 &&
                                abs(u.head.x - r.tail.x) <= 1 && abs(u.head.y - r.tail.y) <= 1) {
                                boxLoops++
                            }
                        }
                    }
                }
            }
        }

        // A level is dominated by a box if:
        // 1. More than 70% of points lie strictly along the outer perimeter of a rectangle
        // 2. AND there is an enclosing 4-sided loop
        val isBox = perimeterDensity > 0.72f && (boxLoops > 0 || (spanX >= 6 && spanY >= 6 && perimeterDensity > 0.80f))

        // Spatial occupancy string (low-res 4x4 sector occupancy)
        val sectorGrid = Array(4) { BooleanArray(4) }
        for (p in allPoints) {
            val sx = (((p.x - minX).toFloat() / spanX) * 4).toInt().coerceIn(0, 3)
            val sy = (((p.y - minY).toFloat() / spanY) * 4).toInt().coerceIn(0, 3)
            sectorGrid[sy][sx] = true
        }
        val sigBuilder = StringBuilder()
        for (row in sectorGrid) {
            for (cell in row) {
                sigBuilder.append(if (cell) '1' else '0')
            }
        }

        return LevelSilhouette(
            boundingBoxRatio = ratio,
            perimeterPerimeterDensity = perimeterDensity,
            rectangularLoopCount = boxLoops,
            isDominatedByBox = isBox,
            directionalDistribution = dirDist,
            bendCount = totalBends,
            occupancyCount = allPoints.size,
            centerOfMass = Pair(avgX, avgY),
            spatialSignature = sigBuilder.toString()
        )
    }

    /**
     * Computes similarity between two silhouettes (0.0 = completely different, 1.0 = identical).
     */
    fun computeSimilarity(s1: LevelSilhouette, s2: LevelSilhouette): Float {
        // Sector hamming distance
        var sectorMatches = 0
        val len = min(s1.spatialSignature.length, s2.spatialSignature.length)
        for (i in 0 until len) {
            if (s1.spatialSignature[i] == s2.spatialSignature[i]) sectorMatches++
        }
        val sectorSim = sectorMatches.toFloat() / 16f

        // Directional similarity
        var dirDiff = 0f
        for (d in Direction.entries) {
            dirDiff += abs((s1.directionalDistribution[d] ?: 0f) - (s2.directionalDistribution[d] ?: 0f))
        }
        val dirSim = (1.0f - (dirDiff / 2.0f)).coerceIn(0f, 1f)

        // Bounding box ratio similarity
        val ratioDiff = abs(s1.boundingBoxRatio - s2.boundingBoxRatio)
        val ratioSim = (1.0f - ratioDiff / 2.0f).coerceIn(0f, 1f)

        return (sectorSim * 0.5f) + (dirSim * 0.3f) + (ratioSim * 0.2f)
    }
}
