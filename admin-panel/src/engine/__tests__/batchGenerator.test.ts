import { describe, it, expect } from 'vitest';
import { generate100VerifiedLevels } from '../../lib/batchLevelGenerator';
import { LevelValidator } from '../LevelValidator';
import { PuzzleSolver } from '../PuzzleSolver';

describe('Batch Level Collection Generator Tests', () => {
  it('Generates 100 levels where EVERY level is 100% valid and solvable', () => {
    const levels = generate100VerifiedLevels();

    expect(levels.length).toBe(100);

    let validCount = 0;
    let solvableCount = 0;

    levels.forEach((level) => {
      const val = LevelValidator.validateLevel(level);
      if (val.isValid) validCount++;

      const solve = PuzzleSolver.solvePuzzle(level.arrows, level.board);
      if (solve.isSolvable && solve.solutionSequence.length === level.arrows.length) {
        solvableCount++;
      }
    });

    expect(validCount).toBe(100);
    expect(solvableCount).toBe(100);
  });
});
