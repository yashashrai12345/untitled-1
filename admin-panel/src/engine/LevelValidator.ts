import { Arrow, Level, ValidationResult } from '../types/game';
import { CollisionDetector, DIRECTION_DELTAS } from './CollisionDetector';
import { PuzzleSolver } from './PuzzleSolver';

export const MIN_ARROW_CLEARANCE = 0.35;

function hypot(x: number, y: number): number {
  return Math.sqrt(x * x + y * y);
}

function distancePointToSegment(
  px: number,
  py: number,
  x1: number,
  y1: number,
  x2: number,
  y2: number
): number {
  const dx = x2 - x1;
  const dy = y2 - y1;
  const lenSq = dx * dx + dy * dy;
  if (lenSq === 0) {
    return hypot(px - x1, py - y1);
  }

  let t = ((px - x1) * dx + (py - y1) * dy) / lenSq;
  t = Math.max(0, Math.min(1, t));
  const projX = x1 + t * dx;
  const projY = y1 + t * dy;
  return hypot(px - projX, py - projY);
}

function ccw(ax: number, ay: number, bx: number, by: number, cx: number, cy: number): number {
  return (bx - ax) * (cy - ay) - (by - ay) * (cx - ax);
}

function segmentsIntersect(
  x1: number, y1: number, x2: number, y2: number,
  x3: number, y3: number, x4: number, y4: number
): boolean {
  const d1 = ccw(x3, y3, x4, y4, x1, y1);
  const d2 = ccw(x3, y3, x4, y4, x2, y2);
  const d3 = ccw(x1, y1, x2, y2, x3, y3);
  const d4 = ccw(x1, y1, x2, y2, x4, y4);

  if (
    ((d1 > 0.001 && d2 < -0.001) || (d1 < -0.001 && d2 > 0.001)) &&
    ((d3 > 0.001 && d4 < -0.001) || (d3 < -0.001 && d4 > 0.001))
  ) {
    return true;
  }

  if (distancePointToSegment(x1, y1, x3, y3, x4, y4) < 0.001) return true;
  if (distancePointToSegment(x2, y2, x3, y3, x4, y4) < 0.001) return true;
  if (distancePointToSegment(x3, y3, x1, y1, x2, y2) < 0.001) return true;
  if (distancePointToSegment(x4, y4, x1, y1, x2, y2) < 0.001) return true;

  return false;
}

function distanceBetweenSegments(
  x1: number, y1: number, x2: number, y2: number,
  x3: number, y3: number, x4: number, y4: number
): number {
  if (segmentsIntersect(x1, y1, x2, y2, x3, y3, x4, y4)) {
    return 0;
  }

  const d1 = distancePointToSegment(x1, y1, x3, y3, x4, y4);
  const d2 = distancePointToSegment(x2, y2, x3, y3, x4, y4);
  const d3 = distancePointToSegment(x3, y3, x1, y1, x2, y2);
  const d4 = distancePointToSegment(x4, y4, x1, y1, x2, y2);

  return Math.min(d1, d2, d3, d4);
}

export class LevelValidator {
  /**
   * Complete level validation pipeline:
   * BOUNDS -> PATH VALIDITY -> ARROW OVERLAP -> CLEARANCE -> SOLVABILITY
   */
  static validateLevel(level: Level, minClearance = MIN_ARROW_CLEARANCE): ValidationResult {
    const board = level.board;
    const arrows = level.arrows;

    if (arrows.length === 0) {
      return {
        isValid: false,
        reason: 'Level has no arrows',
        details: { boundsValid: false, overlapValid: false, solvabilityValid: false },
      };
    }

    // Check duplicate IDs
    const idSet = new Set<string>();
    for (const arrow of arrows) {
      if (idSet.has(arrow.id)) {
        return {
          isValid: false,
          reason: `Duplicate arrow ID: ${arrow.id}`,
          details: { boundsValid: false, overlapValid: false, solvabilityValid: false },
        };
      }
      idSet.add(arrow.id);
    }

    // 1. Bounds & arrow segment check
    for (const arrow of arrows) {
      if (arrow.points.length < 2) {
        return {
          isValid: false,
          reason: `Arrow ${arrow.id} has fewer than 2 points`,
          details: { boundsValid: false, overlapValid: false, solvabilityValid: false },
        };
      }

      for (const p of arrow.points) {
        if (p.x < 0 || p.x >= board.width || p.y < 0 || p.y >= board.height) {
          return {
            isValid: false,
            reason: `Arrow ${arrow.id} point (${p.x}, ${p.y}) is outside board bounds (${board.width}x${board.height})`,
            details: { boundsValid: false, overlapValid: false, solvabilityValid: false },
          };
        }
      }

      // Check orthogonal segments
      for (let i = 0; i < arrow.points.length - 1; i++) {
        const p1 = arrow.points[i];
        const p2 = arrow.points[i + 1];
        if (p1.x !== p2.x && p1.y !== p2.y) {
          return {
            isValid: false,
            reason: `Arrow ${arrow.id} segment between (${p1.x},${p1.y}) and (${p2.x},${p2.y}) is diagonal. Only orthogonal horizontal/vertical segments allowed.`,
            details: { boundsValid: true, overlapValid: false, solvabilityValid: false },
          };
        }
      }
    }

    // 2. Overlap & Clearance between pairs
    for (let i = 0; i < arrows.length; i++) {
      const a1 = arrows[i];
      for (let j = i + 1; j < arrows.length; j++) {
        const a2 = arrows[j];
        const clearResult = this.checkArrowsClearance(a1, a2, minClearance);
        if (!clearResult.isValid) {
          return {
            isValid: false,
            reason: `Overlap or clearance issue between ${a1.id} and ${a2.id}: ${clearResult.reason}`,
            details: {
              boundsValid: true,
              overlapValid: false,
              solvabilityValid: false,
              blockingArrowId: a2.id,
            },
          };
        }
      }
    }

    // 3. Solvability Check
    const solveResult = PuzzleSolver.solvePuzzle(arrows, board);
    if (!solveResult.isSolvable || solveResult.solutionSequence.length !== arrows.length) {
      return {
        isValid: false,
        reason: 'Level is not fully solvable! Some arrows cannot escape due to deadlocks or blocking corridors.',
        details: { boundsValid: true, overlapValid: true, solvabilityValid: false },
      };
    }

    return {
      isValid: true,
      reason: 'Verified Solvable & Valid Level',
      details: { boundsValid: true, overlapValid: true, solvabilityValid: true },
    };
  }

  private static checkArrowsClearance(
    arrowA: Arrow,
    arrowB: Arrow,
    minClearance: number
  ): { isValid: boolean; reason?: string } {
    if (arrowA.id === arrowB.id) return { isValid: true };

    // Shared grid points
    const pointsA = CollisionDetector.getOccupiedPoints(arrowA);
    const pointsB = CollisionDetector.getOccupiedPoints(arrowB);

    for (const pt of pointsA) {
      if (pointsB.has(pt)) {
        return { isValid: false, reason: `Shared grid point ${pt}` };
      }
    }

    // Segment geometry checks
    const segsA: Array<[number, number, number, number]> = [];
    for (let i = 0; i < arrowA.points.length - 1; i++) {
      segsA.push([
        arrowA.points[i].x,
        arrowA.points[i].y,
        arrowA.points[i + 1].x,
        arrowA.points[i + 1].y,
      ]);
    }

    const segsB: Array<[number, number, number, number]> = [];
    for (let i = 0; i < arrowB.points.length - 1; i++) {
      segsB.push([
        arrowB.points[i].x,
        arrowB.points[i].y,
        arrowB.points[i + 1].x,
        arrowB.points[i + 1].y,
      ]);
    }

    for (const sa of segsA) {
      for (const sb of segsB) {
        if (segmentsIntersect(sa[0], sa[1], sa[2], sa[3], sb[0], sb[1], sb[2], sb[3])) {
          return { isValid: false, reason: 'Segments intersect geometrically' };
        }

        const dist = distanceBetweenSegments(sa[0], sa[1], sa[2], sa[3], sb[0], sb[1], sb[2], sb[3]);
        if (dist < minClearance) {
          return { isValid: false, reason: `Distance between segments ${dist.toFixed(2)} < ${minClearance}` };
        }
      }
    }

    // Arrowhead tip clearance (tip extends forward 0.25)
    const headA = arrowA.points[arrowA.points.length - 1];
    const deltaA = DIRECTION_DELTAS[arrowA.direction];
    const tipA_X = headA.x + deltaA.dx * 0.25;
    const tipA_Y = headA.y + deltaA.dy * 0.25;

    for (const sb of segsB) {
      const dist = distancePointToSegment(tipA_X, tipA_Y, sb[0], sb[1], sb[2], sb[3]);
      if (dist < minClearance) {
        return { isValid: false, reason: `Tip of ${arrowA.id} too close to segment of ${arrowB.id}` };
      }
    }

    const headB = arrowB.points[arrowB.points.length - 1];
    const deltaB = DIRECTION_DELTAS[arrowB.direction];
    const tipB_X = headB.x + deltaB.dx * 0.25;
    const tipB_Y = headB.y + deltaB.dy * 0.25;

    for (const sa of segsA) {
      const dist = distancePointToSegment(tipB_X, tipB_Y, sa[0], sa[1], sa[2], sa[3]);
      if (dist < minClearance) {
        return { isValid: false, reason: `Tip of ${arrowB.id} too close to segment of ${arrowA.id}` };
      }
    }

    return { isValid: true };
  }
}
