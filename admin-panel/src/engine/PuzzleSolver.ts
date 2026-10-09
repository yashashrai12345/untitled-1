import { Arrow, Board, SolveResult } from '../types/game';
import { CollisionDetector } from './CollisionDetector';

export class PuzzleSolver {
  /**
   * Returns all arrows currently free to escape.
   */
  static getLegalMoves(activeArrows: Arrow[], board: Board): Arrow[] {
    return activeArrows.filter((arrow) => CollisionDetector.canArrowEscape(arrow, activeArrows, board));
  }

  /**
   * Solves puzzle using fast greedy topological resolution, falling back to BFS.
   */
  static solvePuzzle(initialArrows: Arrow[], board: Board, maxQueueSize = 5000): SolveResult {
    if (initialArrows.length === 0) {
      return { isSolvable: true, solutionSequence: [], movesExplored: 0, depth: 0 };
    }

    // 1. Fast greedy resolution
    const greedyRemaining = [...initialArrows];
    const greedyPath: string[] = [];

    while (greedyRemaining.length > 0) {
      const move = greedyRemaining.find((arrow) =>
        CollisionDetector.canArrowEscape(arrow, greedyRemaining, board)
      );
      if (!move) break;

      const idx = greedyRemaining.findIndex((a) => a.id === move.id);
      greedyRemaining.splice(idx, 1);
      greedyPath.push(move.id);
    }

    if (greedyRemaining.length === 0) {
      return {
        isSolvable: true,
        solutionSequence: greedyPath,
        movesExplored: greedyPath.length,
        depth: greedyPath.length,
      };
    }

    // 2. BFS Fallback
    interface Node {
      remaining: Arrow[];
      path: string[];
    }

    const stateKey = (arrows: Arrow[]) => arrows.map((a) => a.id).sort().join(',');

    const queue: Node[] = [{ remaining: initialArrows, path: [] }];
    const visited = new Set<string>();
    visited.add(stateKey(initialArrows));

    let explored = 0;

    while (queue.length > 0) {
      const current = queue.shift()!;
      explored++;

      if (current.remaining.length === 0) {
        return {
          isSolvable: true,
          solutionSequence: current.path,
          movesExplored: explored,
          depth: current.path.length,
        };
      }

      if (explored >= maxQueueSize) {
        break;
      }

      const legalMoves = this.getLegalMoves(current.remaining, board);
      for (const move of legalMoves) {
        const nextRemaining = current.remaining.filter((a) => a.id !== move.id);
        const key = stateKey(nextRemaining);
        if (!visited.has(key)) {
          visited.add(key);
          queue.push({ remaining: nextRemaining, path: [...current.path, move.id] });
        }
      }
    }

    return {
      isSolvable: false,
      solutionSequence: [],
      movesExplored: explored,
      depth: 0,
    };
  }

  /**
   * Calculates difficulty score on scale 1-5.
   */
  static calculateDifficulty(arrows: Arrow[], board: Board): number {
    const solve = this.solvePuzzle(arrows, board);
    if (!solve.isSolvable) return 1;

    const arrowCount = arrows.length;
    let occupiedCount = 0;
    arrows.forEach((a) => {
      occupiedCount += CollisionDetector.getOccupiedPoints(a).size;
    });

    const density = occupiedCount / (board.width * board.height);

    if (arrowCount <= 3) return 1;
    if (arrowCount <= 6) return 2;
    if (arrowCount <= 10 && density < 0.4) return 3;
    if (arrowCount <= 14) return 4;
    return 5;
  }
}
