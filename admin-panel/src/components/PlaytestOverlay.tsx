import React, { useState } from 'react';
import { Arrow, Board } from '../types/game';
import { CollisionDetector } from '../engine/CollisionDetector';
import { Trophy, RefreshCw, XCircle } from 'lucide-react';

interface PlaytestOverlayProps {
  initialArrows: Arrow[];
  board: Board;
  onExitPlaytest: () => void;
}

export const PlaytestOverlay: React.FC<PlaytestOverlayProps> = ({
  initialArrows,
  board,
  onExitPlaytest,
}) => {
  const [activeArrows, setActiveArrows] = useState<Arrow[]>([...initialArrows]);
  const [moves, setMoves] = useState(0);
  const [message, setMessage] = useState<string | null>(null);

  const isSolved = activeArrows.length === 0 && initialArrows.length > 0;

  const handleArrowClick = (arrowId: string) => {
    if (isSolved) return;

    const arrow = activeArrows.find((a) => a.id === arrowId);
    if (!arrow) return;

    setMoves((m) => m + 1);

    const canEscape = CollisionDetector.canArrowEscape(arrow, activeArrows, board);

    if (canEscape) {
      // Remove escaping arrow
      setActiveArrows((prev) => prev.filter((a) => a.id !== arrowId));
      setMessage(`Arrow ${arrowId} escaped!`);
      setTimeout(() => setMessage(null), 1200);
    } else {
      const blocker = CollisionDetector.findBlockingArrow(arrow, activeArrows, board);
      setMessage(`Blocked! Arrow ${arrowId} hit ${blocker ? blocker.id : 'obstacle'}`);
      setTimeout(() => setMessage(null), 1500);
    }
  };

  const handleRestart = () => {
    setActiveArrows([...initialArrows]);
    setMoves(0);
    setMessage('Level restarted');
    setTimeout(() => setMessage(null), 1000);
  };

  return (
    <div className="absolute top-4 left-1/2 transform -translate-x-1/2 bg-slate-900/90 backdrop-blur border border-indigo-500/40 px-5 py-2.5 rounded-2xl shadow-xl flex items-center space-x-6 text-xs text-slate-200 select-none z-20">
      <div className="flex items-center space-x-2">
        <span className="w-2.5 h-2.5 rounded-full bg-emerald-400 animate-pulse" />
        <span className="font-bold text-white uppercase tracking-wider text-[11px]">
          Playtest Mode Active
        </span>
      </div>

      <div className="flex items-center space-x-4 border-l border-slate-800 pl-4">
        <div>
          <span className="text-slate-400">Remaining:</span>{' '}
          <span className="font-bold text-indigo-400 text-sm">{activeArrows.length}</span>
          <span className="text-slate-500"> / {initialArrows.length}</span>
        </div>

        <div>
          <span className="text-slate-400">Moves:</span>{' '}
          <span className="font-bold text-white text-sm">{moves}</span>
        </div>
      </div>

      {message && (
        <div className="text-amber-300 font-semibold bg-amber-500/10 px-2.5 py-1 rounded border border-amber-500/20">
          {message}
        </div>
      )}

      <button
        onClick={handleRestart}
        className="p-1.5 hover:bg-slate-800 rounded-lg text-slate-300 hover:text-white transition-colors"
        title="Restart Playtest"
      >
        <RefreshCw className="w-4 h-4" />
      </button>

      {/* Victory Modal Overlay */}
      {isSolved && (
        <div className="fixed inset-0 bg-slate-950/80 backdrop-blur-md flex items-center justify-center z-50">
          <div className="bg-slate-900 border border-emerald-500/40 rounded-2xl p-8 max-w-sm text-center shadow-2xl space-y-4">
            <div className="w-16 h-16 bg-emerald-500/20 text-emerald-400 rounded-full flex items-center justify-center mx-auto border border-emerald-500/30">
              <Trophy className="w-8 h-8" />
            </div>
            <h2 className="text-xl font-bold text-white">Puzzle Complete!</h2>
            <p className="text-xs text-slate-400">
              All arrows escaped successfully in {moves} moves.
            </p>
            <div className="flex space-x-3 pt-2">
              <button
                onClick={handleRestart}
                className="flex-1 py-2 bg-slate-800 hover:bg-slate-700 text-slate-200 rounded-lg text-xs font-semibold"
              >
                Play Again
              </button>
              <button
                onClick={onExitPlaytest}
                className="flex-1 py-2 bg-indigo-600 hover:bg-indigo-500 text-white rounded-lg text-xs font-semibold shadow-lg shadow-indigo-600/30"
              >
                Back to Editing
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
