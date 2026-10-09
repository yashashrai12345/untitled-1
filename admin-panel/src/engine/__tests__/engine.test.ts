import { describe, it, expect } from 'vitest';
import { Arrow, Board, Level } from '../../types/game';
import { CollisionDetector } from '../CollisionDetector';
import { PuzzleSolver } from '../PuzzleSolver';
import { LevelValidator } from '../LevelValidator';

describe('Admin Designer Engine Unit Tests', () => {
  const board: Board = { width: 5, height: 5 };

  it('CollisionDetector - Solo arrow escape corridor is free', () => {
    const arrow: Arrow = {
      id: 'a1',
      direction: 'RIGHT',
      points: [{ x: 1, y: 1 }, { x: 3, y: 1 }],
    };

    const canEscape = CollisionDetector.canArrowEscape(arrow, [arrow], board);
    expect(canEscape).toBe(true);
  });

  it('CollisionDetector - Blocker in corridor prevents escape', () => {
    const a1: Arrow = {
      id: 'a1',
      direction: 'RIGHT',
      points: [{ x: 1, y: 1 }, { x: 3, y: 1 }],
    };

    const a2: Arrow = {
      id: 'a2',
      direction: 'UP',
      points: [{ x: 2, y: 3 }, { x: 2, y: 2 }],
    };

    const active = [a1, a2];
    expect(CollisionDetector.canArrowEscape(a1, active, board)).toBe(true);
    expect(CollisionDetector.canArrowEscape(a2, active, board)).toBe(false);

    // After removing a1
    expect(CollisionDetector.canArrowEscape(a2, [a2], board)).toBe(true);
  });

  it('PuzzleSolver - Solves sequential levels correctly', () => {
    const a1: Arrow = {
      id: 'a1',
      direction: 'RIGHT',
      points: [{ x: 1, y: 1 }, { x: 3, y: 1 }],
    };

    const a2: Arrow = {
      id: 'a2',
      direction: 'UP',
      points: [{ x: 2, y: 3 }, { x: 2, y: 2 }],
    };

    const result = PuzzleSolver.solvePuzzle([a1, a2], board);
    expect(result.isSolvable).toBe(true);
    expect(result.solutionSequence).toEqual(['a1', 'a2']);
  });

  it('LevelValidator - Rejects levels with overlapping grid points', () => {
    const a1: Arrow = {
      id: 'a1',
      direction: 'RIGHT',
      points: [{ x: 1, y: 2 }, { x: 3, y: 2 }],
    };

    const a2: Arrow = {
      id: 'a2',
      direction: 'DOWN',
      points: [{ x: 2, y: 1 }, { x: 2, y: 3 }],
    };

    const level: Level = {
      id: 101,
      name: 'Invalid Crossing',
      board,
      arrows: [a1, a2],
      difficulty: 1,
      parMoves: 2,
      patternType: 'CUSTOM',
      seed: 0,
      difficultyScore: 1.0,
    };

    const validation = LevelValidator.validateLevel(level);
    expect(validation.isValid).toBe(false);
  });
});
