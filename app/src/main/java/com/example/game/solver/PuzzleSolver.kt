package com.example.game.solver

import com.example.game.collision.CollisionDetector
import com.example.game.model.Arrow
import com.example.game.model.Board
import java.util.ArrayDeque

object PuzzleSolver {

    data class SolveResult(
        val isSolvable: Boolean,
        val solutionSequence: List<String>,
        val movesExplored: Int,
        val depth: Int
    )

    /**
     * Returns all arrows that are currently free to escape.
     */
    fun getLegalMoves(activeArrows: List<Arrow>, board: Board): List<Arrow> {
        return activeArrows.filter { arrow ->
            CollisionDetector.canArrowEscape(arrow, activeArrows, board)
        }
    }

    /**
     * Checks if a specific arrow can currently move legally.
     */
    fun isMoveLegal(activeArrows: List<Arrow>, arrowId: String, board: Board): Boolean {
        val arrow = activeArrows.find { it.id == arrowId } ?: return false
        return CollisionDetector.canArrowEscape(arrow, activeArrows, board)
    }

    /**
     * Solves the puzzle using fast greedy topological resolution, falling back to BFS.
     * Guaranteed to terminate and returns [SolveResult.isSolvable] = true if solvable.
     */
    fun solvePuzzle(
        initialArrows: List<Arrow>,
        board: Board,
        maxQueueSize: Int = 5000
    ): SolveResult {
        if (initialArrows.isEmpty()) {
            return SolveResult(isSolvable = true, solutionSequence = emptyList(), movesExplored = 0, depth = 0)
        }

        // Fast greedy resolution: in this puzzle, arrow removal is strictly monotonic.
        // Removing a free arrow only removes obstacles and never blocks any other arrow.
        val greedyRemaining = initialArrows.toMutableList()
        val greedyPath = mutableListOf<String>()
        while (greedyRemaining.isNotEmpty()) {
            val move = greedyRemaining.firstOrNull { arrow ->
                CollisionDetector.canArrowEscape(arrow, greedyRemaining, board)
            } ?: break
            greedyRemaining.remove(move)
            greedyPath.add(move.id)
        }
        if (greedyRemaining.isEmpty()) {
            return SolveResult(
                isSolvable = true,
                solutionSequence = greedyPath,
                movesExplored = greedyPath.size,
                depth = greedyPath.size
            )
        }

        // Each BFS node holds (remainingArrows, sequenceOfMovesSoFar)
        data class Node(
            val remaining: List<Arrow>,
            val path: List<String>
        )

        val queue = ArrayDeque<Node>()
        val visited = mutableSetOf<String>()

        fun stateKey(arrows: List<Arrow>): String {
            return arrows.map { it.id }.sorted().joinToString(",")
        }

        queue.add(Node(initialArrows, emptyList()))
        visited.add(stateKey(initialArrows))

        var explored = 0

        while (queue.isNotEmpty()) {
            val current = queue.poll() ?: break
            explored++

            if (current.remaining.isEmpty()) {
                return SolveResult(
                    isSolvable = true,
                    solutionSequence = current.path,
                    movesExplored = explored,
                    depth = current.path.size
                )
            }

            if (explored >= maxQueueSize) {
                // Cutoff for memory / CPU safety
                break
            }

            val legalMoves = getLegalMoves(current.remaining, board)
            for (move in legalMoves) {
                val nextRemaining = current.remaining.filter { it.id != move.id }
                val key = stateKey(nextRemaining)
                if (key !in visited) {
                    visited.add(key)
                    queue.add(Node(nextRemaining, current.path + move.id))
                }
            }
        }

        return SolveResult(
            isSolvable = false,
            solutionSequence = emptyList(),
            movesExplored = explored,
            depth = 0
        )
    }

    /**
     * Estimates puzzle difficulty on a scale from 1 (Novice) to 5 (Master).
     */
    fun calculateDifficulty(arrows: List<Arrow>, board: Board): Int {
        val solve = solvePuzzle(arrows, board)
        if (!solve.isSolvable) return 1

        val arrowCount = arrows.size
        val solutionLength = solve.solutionSequence.size
        val density = arrows.sumOf { it.occupiedPoints.size }.toFloat() / (board.width * board.height)

        return when {
            arrowCount <= 3 -> 1
            arrowCount <= 6 -> 2
            arrowCount <= 10 && density < 0.4f -> 3
            arrowCount <= 14 -> 4
            else -> 5
        }
    }
}
