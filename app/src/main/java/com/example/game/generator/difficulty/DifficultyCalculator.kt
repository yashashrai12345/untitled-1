package com.example.game.generator.difficulty

import com.example.game.collision.CollisionDetector
import com.example.game.model.Arrow
import com.example.game.model.Board
import com.example.game.model.Direction
import com.example.game.solver.PuzzleSolver
import kotlin.math.ln

data class DifficultyMetrics(
    val arrowCount: Int,
    val totalBendCount: Int,
    val totalPathLength: Int,
    val dependencyDepth: Int,
    val branchingFactor: Float,
    val boardDensity: Float,
    val availableFirstMoves: Int,
    val directionalEntropy: Float,
    val rawScore: Float,
    val normalizedTier: Int // 1..6
)

object DifficultyCalculator {

    fun calculateMetrics(arrows: List<Arrow>, board: Board): DifficultyMetrics {
        if (arrows.isEmpty()) {
            return DifficultyMetrics(0, 0, 0, 0, 0f, 0f, 0, 0f, 0f, 1)
        }

        val arrowCount = arrows.size
        val totalBendCount = arrows.sumOf { it.bendCount }
        val totalPathLength = arrows.sumOf { it.pathLength }

        // Board density
        val allOccupiedPoints = arrows.flatMap { it.occupiedPoints }.toSet()
        val boardArea = (board.width * board.height).coerceAtLeast(1)
        val boardDensity = allOccupiedPoints.size.toFloat() / boardArea.toFloat()

        // Available first moves
        val firstMoves = PuzzleSolver.getLegalMoves(arrows, board)
        val availableFirstMoves = firstMoves.size

        // Build dependency graph: who directly blocks whom
        // blocker -> list of blocked arrows
        val blockers = mutableMapOf<String, MutableSet<String>>()
        val blockedBy = mutableMapOf<String, MutableSet<String>>()
        for (a in arrows) {
            blockers[a.id] = mutableSetOf()
            blockedBy[a.id] = mutableSetOf()
        }

        for (a in arrows) {
            val blocker = CollisionDetector.findBlockingArrow(a, arrows, board)
            if (blocker != null) {
                blockers[blocker.id]?.add(a.id)
                blockedBy[a.id]?.add(blocker.id)
            }
        }

        // Compute dependency depth (longest path in dependency graph)
        var maxDepth = 1
        for (root in arrows.filter { blockedBy[it.id].isNullOrEmpty() }) {
            maxDepth = maxOf(maxDepth, computeDepth(root.id, blockers, mutableSetOf()))
        }
        val dependencyDepth = maxDepth

        // Branching factor: average number of arrows unlocked when a blocker leaves
        val totalBranchOut = blockers.values.sumOf { it.size }
        val activeBlockersCount = blockers.values.count { it.isNotEmpty() }.coerceAtLeast(1)
        val branchingFactor = totalBranchOut.toFloat() / activeBlockersCount.toFloat()

        // Directional entropy: Shannon entropy of directional distribution
        val dirCounts = arrows.groupingBy { it.direction }.eachCount()
        var entropy = 0.0
        val total = arrowCount.toDouble()
        for (d in Direction.values()) {
            val c = dirCounts[d] ?: 0
            if (c > 0) {
                val p = c / total
                entropy -= p * (ln(p) / ln(4.0)) // normalized by ln(4)
            }
        }
        val directionalEntropy = entropy.toFloat().coerceIn(0f, 1f)

        // Raw score composite:
        // arrowCount * 1.0 + totalBendCount * 0.8 + dependencyDepth * 2.0 + totalPathLength * 0.2 + (1.0 - availableFirstMoves / arrowCount) * 4.0
        val constraintFactor = if (arrowCount > 1) {
            (1f - (availableFirstMoves.toFloat() / arrowCount.toFloat())).coerceIn(0f, 1f)
        } else {
            0f
        }

        val rawScore = (arrowCount * 1.2f) +
                (totalBendCount * 0.9f) +
                (dependencyDepth * 2.2f) +
                (totalPathLength * 0.15f) +
                (constraintFactor * 4.5f) +
                (boardDensity * 8.0f) +
                (directionalEntropy * 2.0f)

        val normalizedTier = when {
            rawScore < 8f -> 1   // Very Easy
            rawScore < 15f -> 2  // Easy
            rawScore < 24f -> 3  // Medium
            rawScore < 35f -> 4  // Hard
            rawScore < 48f -> 5  // Very Hard
            else -> 6            // Expert
        }

        return DifficultyMetrics(
            arrowCount = arrowCount,
            totalBendCount = totalBendCount,
            totalPathLength = totalPathLength,
            dependencyDepth = dependencyDepth,
            branchingFactor = branchingFactor,
            boardDensity = boardDensity,
            availableFirstMoves = availableFirstMoves,
            directionalEntropy = directionalEntropy,
            rawScore = rawScore,
            normalizedTier = normalizedTier
        )
    }

    private fun computeDepth(
        currentId: String,
        blockers: Map<String, Set<String>>,
        visited: MutableSet<String>
    ): Int {
        if (currentId in visited) return 0
        visited.add(currentId)

        val children = blockers[currentId] ?: emptySet()
        var childMax = 0
        for (c in children) {
            childMax = maxOf(childMax, computeDepth(c, blockers, visited))
        }

        visited.remove(currentId)
        return 1 + childMax
    }
}
