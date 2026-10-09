import { Arrow, Board, Direction, GridPoint } from '../types/game';

export const DIRECTION_DELTAS: Record<Direction, { dx: number; dy: number; degrees: number }> = {
  UP: { dx: 0, dy: -1, degrees: 270 },
  RIGHT: { dx: 1, dy: 0, degrees: 0 },
  DOWN: { dx: 0, dy: 1, degrees: 90 },
  LEFT: { dx: -1, dy: 0, degrees: 180 },
};

export class CollisionDetector {
  /**
   * Get set of discrete integer grid points occupied by an arrow's body segments.
   */
  static getOccupiedPoints(arrow: Arrow): Set<string> {
    const set = new Set<string>();
    const points = arrow.points;
    if (points.length < 2) return set;

    for (let i = 0; i < points.length - 1; i++) {
      const p1 = points[i];
      const p2 = points[i + 1];

      if (p1.x === p2.x) {
        const minY = Math.min(p1.y, p2.y);
        const maxY = Math.max(p1.y, p2.y);
        for (let y = minY; y <= maxY; y++) {
          set.add(`${p1.x},${y}`);
        }
      } else if (p1.y === p2.y) {
        const minX = Math.min(p1.x, p2.x);
        const maxX = Math.max(p1.x, p2.x);
        for (let x = minX; x <= maxX; x++) {
          set.add(`${x},${p1.y}`);
        }
      } else {
        set.add(`${p1.x},${p1.y}`);
        set.add(`${p2.x},${p2.y}`);
      }
    }
    return set;
  }

  /**
   * Check if arrow occupies a specific grid point.
   */
  static occupies(arrow: Arrow, point: GridPoint): boolean {
    const key = `${point.x},${point.y}`;
    return this.getOccupiedPoints(arrow).has(key);
  }

  /**
   * Determines whether the given arrow can legally escape off the board
   * in its movement direction without colliding with any other active arrows.
   */
  static canArrowEscape(arrow: Arrow, activeArrows: Arrow[], board: Board): boolean {
    const otherArrows = activeArrows.filter((a) => a.id !== arrow.id);
    if (otherArrows.length === 0) return true;

    const delta = DIRECTION_DELTAS[arrow.direction];
    const head = arrow.points[arrow.points.length - 1];

    // Build lookup set of obstacles
    const obstaclePoints = new Set<string>();
    for (const other of otherArrows) {
      const occupied = this.getOccupiedPoints(other);
      occupied.forEach((p) => obstaclePoints.add(p));
    }

    const maxSteps = Math.max(board.width, board.height) + 2;
    for (let k = 1; k <= maxSteps; k++) {
      const corridorX = head.x + k * delta.dx;
      const corridorY = head.y + k * delta.dy;
      const key = `${corridorX},${corridorY}`;

      if (obstaclePoints.has(key)) {
        return false;
      }
    }

    return true;
  }

  /**
   * Finds which other arrow is blocking arrow, if any.
   */
  static findBlockingArrow(arrow: Arrow, activeArrows: Arrow[], board: Board): Arrow | null {
    const otherArrows = activeArrows.filter((a) => a.id !== arrow.id);
    const delta = DIRECTION_DELTAS[arrow.direction];
    const head = arrow.points[arrow.points.length - 1];
    const maxSteps = Math.max(board.width, board.height) + 2;

    for (let k = 1; k <= maxSteps; k++) {
      const corridorPoint: GridPoint = {
        x: head.x + k * delta.dx,
        y: head.y + k * delta.dy,
      };

      for (const other of otherArrows) {
        if (this.occupies(other, corridorPoint)) {
          return other;
        }
      }
    }

    return null;
  }

  /**
   * Calculates distance in grid units from head to nearest obstacle.
   */
  static calculateCollisionDistance(arrow: Arrow, activeArrows: Arrow[], board: Board): number {
    const otherArrows = activeArrows.filter((a) => a.id !== arrow.id);
    const delta = DIRECTION_DELTAS[arrow.direction];
    const head = arrow.points[arrow.points.length - 1];
    const maxSteps = Math.max(board.width, board.height) + 2;

    for (let k = 1; k <= maxSteps; k++) {
      const corridorPoint: GridPoint = {
        x: head.x + k * delta.dx,
        y: head.y + k * delta.dy,
      };

      if (otherArrows.some((other) => this.occupies(other, corridorPoint))) {
        return Math.max(0.35, k - 0.35);
      }
    }

    return 0.5;
  }
}
