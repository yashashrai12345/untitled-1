package com.example.game.solver

import com.example.game.model.Arrow
import com.example.game.model.Board

object HintEngine {

    data class Hint(
        val arrowId: String?,
        val message: String
    )

    /**
     * Determines the most strategic next move for the player.
     */
    fun getNextHint(activeArrows: List<Arrow>, board: Board): Hint {
        if (activeArrows.isEmpty()) {
            return Hint(null, "Level is already completed!")
        }

        // Run the solver to get optimal solution from current state
        val solveResult = PuzzleSolver.solvePuzzle(activeArrows, board)

        if (solveResult.isSolvable && solveResult.solutionSequence.isNotEmpty()) {
            val bestMoveId = solveResult.solutionSequence.first()
            return Hint(bestMoveId, "Tap the highlighted arrow to escape!")
        }

        // Fallback: check any legal move
        val legalMoves = PuzzleSolver.getLegalMoves(activeArrows, board)
        if (legalMoves.isNotEmpty()) {
            return Hint(legalMoves.first().id, "This arrow has a clear path.")
        }

        return Hint(null, "No clear paths remaining. Try restarting the level!")
    }
}
