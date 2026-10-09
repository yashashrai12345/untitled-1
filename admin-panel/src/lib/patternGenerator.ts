import { Arrow, Board, Direction, GridPoint } from '../types/game';

function generateId(): string {
  return 'arrow_' + Math.random().toString(36).substring(2, 9);
}

export class PatternGenerator {
  /**
   * Generates a spiral pattern within board dimensions.
   */
  static generateSpiral(board: Board): Arrow[] {
    const arrows: Arrow[] = [];
    const minDim = Math.min(board.width, board.height);
    const layers = Math.floor(minDim / 2) - 1;

    for (let l = 0; l < Math.max(1, layers); l++) {
      const top = 1 + l;
      const left = 1 + l;
      const right = board.width - 2 - l;
      const bottom = board.height - 2 - l;

      if (right <= left || bottom <= top) break;

      // Top edge pointing RIGHT
      arrows.push({
        id: generateId(),
        direction: 'RIGHT',
        points: [{ x: left, y: top }, { x: right, y: top }],
      });

      // Right edge pointing DOWN
      arrows.push({
        id: generateId(),
        direction: 'DOWN',
        points: [{ x: right, y: top + 1 }, { x: right, y: bottom }],
      });

      // Bottom edge pointing LEFT
      arrows.push({
        id: generateId(),
        direction: 'LEFT',
        points: [{ x: right - 1, y: bottom }, { x: left, y: bottom }],
      });

      // Left edge pointing UP
      arrows.push({
        id: generateId(),
        direction: 'UP',
        points: [{ x: left, y: bottom - 1 }, { x: left, y: top + 1 }],
      });
    }

    return arrows;
  }

  /**
   * Generates concentric square rings.
   */
  static generateConcentricSquares(board: Board): Arrow[] {
    return this.generateSpiral(board);
  }

  /**
   * Horizontal symmetry: flips arrows along vertical center line (x' = width - 1 - x).
   */
  static applyHorizontalSymmetry(arrows: Arrow[], board: Board): Arrow[] {
    const newArrows: Arrow[] = [...arrows];

    for (const arrow of arrows) {
      const flippedPoints: GridPoint[] = arrow.points.map((p) => ({
        x: board.width - 1 - p.x,
        y: p.y,
      }));

      let flippedDir: Direction = arrow.direction;
      if (arrow.direction === 'LEFT') flippedDir = 'RIGHT';
      else if (arrow.direction === 'RIGHT') flippedDir = 'LEFT';

      newArrows.push({
        id: generateId(),
        direction: flippedDir,
        points: flippedPoints,
      });
    }

    return newArrows;
  }

  /**
   * Vertical symmetry: flips arrows along horizontal center line (y' = height - 1 - y).
   */
  static applyVerticalSymmetry(arrows: Arrow[], board: Board): Arrow[] {
    const newArrows: Arrow[] = [...arrows];

    for (const arrow of arrows) {
      const flippedPoints: GridPoint[] = arrow.points.map((p) => ({
        x: p.x,
        y: board.height - 1 - p.y,
      }));

      let flippedDir: Direction = arrow.direction;
      if (arrow.direction === 'UP') flippedDir = 'DOWN';
      else if (arrow.direction === 'DOWN') flippedDir = 'UP';

      newArrows.push({
        id: generateId(),
        direction: flippedDir,
        points: flippedPoints,
      });
    }

    return newArrows;
  }

  /**
   * Rotates pattern 90 degrees clockwise.
   */
  static rotate90Clockwise(arrows: Arrow[], board: Board): Arrow[] {
    return arrows.map((arrow) => {
      const rotPoints = arrow.points.map((p) => ({
        x: board.height - 1 - p.y,
        y: p.x,
      }));

      const dirMap: Record<Direction, Direction> = {
        UP: 'RIGHT',
        RIGHT: 'DOWN',
        DOWN: 'LEFT',
        LEFT: 'UP',
      };

      return {
        id: arrow.id,
        direction: dirMap[arrow.direction],
        points: rotPoints,
      };
    });
  }
}
