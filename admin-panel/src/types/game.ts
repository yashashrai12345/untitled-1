export type Direction = 'UP' | 'RIGHT' | 'DOWN' | 'LEFT';

export interface GridPoint {
  x: number;
  y: number;
}

export interface Arrow {
  id: string;
  direction: Direction;
  points: GridPoint[];
}

export interface Board {
  width: number;
  height: number;
}

export interface Level {
  id: number | string;
  name: string;
  board: Board;
  arrows: Arrow[];
  difficulty: number;
  parMoves: number;
  patternType: string;
  seed: number;
  difficultyScore: number;
  level_number?: number;
}

export type PatternStatus = 'draft' | 'published';

export interface CustomPatternRecord {
  id: string;
  numeric_id?: number;
  name: string;
  description: string;
  width: number;
  height: number;
  level_data: Level;
  difficulty: number;
  status: PatternStatus;
  version: number;
  level_number?: number;
  created_at?: string;
  updated_at?: string;
  published_at?: string | null;
}

export interface ValidationResult {
  isValid: boolean;
  reason?: string;
  details?: {
    boundsValid: boolean;
    overlapValid: boolean;
    solvabilityValid: boolean;
    blockingArrowId?: string;
  };
}

export interface SolveResult {
  isSolvable: boolean;
  solutionSequence: string[];
  movesExplored: number;
  depth: number;
}
