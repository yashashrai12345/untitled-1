import React from 'react';
import { Arrow, Board, ValidationResult, SolveResult } from '../types/game';
import { CheckCircle2, AlertTriangle, XCircle, ShieldCheck, Cpu } from 'lucide-react';
import { CollisionDetector } from '../engine/CollisionDetector';

interface ValidationPanelProps {
  validation: ValidationResult;
  solveResult: SolveResult;
  arrows: Arrow[];
  board: Board;
}

export const ValidationPanel: React.FC<ValidationPanelProps> = ({
  validation,
  solveResult,
  arrows,
  board,
}) => {
  let occupiedCount = 0;
  arrows.forEach((a) => {
    occupiedCount += CollisionDetector.getOccupiedPoints(a).size;
  });

  const totalCells = board.width * board.height;
  const densityPercent = Math.round((occupiedCount / totalCells) * 100);

  return (
    <div className="bg-slate-900 border-t border-slate-800 p-4 px-6 flex items-center justify-between text-slate-300 text-xs select-none">
      {/* Solvability Status Badge */}
      <div className="flex items-center space-x-3">
        {validation.isValid ? (
          <div className="flex items-center space-x-2 bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 px-3 py-1.5 rounded-lg font-bold">
            <ShieldCheck className="w-4 h-4" />
            <span>VERIFIED SOLVABLE</span>
          </div>
        ) : (
          <div className="flex items-center space-x-2 bg-rose-500/10 border border-rose-500/30 text-rose-400 px-3 py-1.5 rounded-lg font-bold">
            <XCircle className="w-4 h-4" />
            <span>INVALID PATTERN</span>
          </div>
        )}

        <div className="text-slate-400 max-w-md truncate">
          {validation.reason}
        </div>
      </div>

      {/* Solver & Density Details */}
      <div className="flex items-center space-x-6">
        <div className="flex items-center space-x-1.5">
          <Cpu className="w-4 h-4 text-indigo-400" />
          <span className="text-slate-400">Moves Explored:</span>
          <span className="font-bold text-white font-mono">{solveResult.movesExplored}</span>
        </div>

        <div>
          <span className="text-slate-400">Total Arrows:</span>{' '}
          <span className="font-bold text-white">{arrows.length}</span>
        </div>

        <div>
          <span className="text-slate-400">Grid Density:</span>{' '}
          <span className="font-bold text-white">{densityPercent}%</span>
        </div>

        {solveResult.isSolvable && solveResult.solutionSequence.length > 0 && (
          <div className="flex items-center space-x-1 max-w-xs overflow-hidden text-ellipsis whitespace-nowrap bg-slate-950 px-2.5 py-1 rounded border border-slate-800 text-[11px] font-mono text-indigo-300">
            <span className="text-slate-500">Order:</span>
            <span>{solveResult.solutionSequence.slice(0, 4).join(' → ')}</span>
            {solveResult.solutionSequence.length > 4 && <span>...</span>}
          </div>
        )}
      </div>
    </div>
  );
};
