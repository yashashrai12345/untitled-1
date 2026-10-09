import React from 'react';
import { Arrow, Board, Direction } from '../types/game';
import { ArrowUp, ArrowRight, ArrowDown, ArrowLeft, Trash2, Repeat, Copy, RotateCw, FlipHorizontal, FlipVertical, Compass } from 'lucide-react';
import { PatternGenerator } from '../lib/patternGenerator';

interface PropertiesPanelProps {
  selectedArrow: Arrow | null;
  onUpdateArrow: (arrow: Arrow) => void;
  onDeleteArrow: (id: string) => void;
  onDuplicateArrow: (arrow: Arrow) => void;
  board: Board;
  arrows: Arrow[];
  setArrows: (arrows: Arrow[] | ((prev: Arrow[]) => Arrow[])) => void;
  difficulty: number;
  setDifficulty: (diff: number) => void;
  description: string;
  setDescription: (desc: string) => void;
}

export const PropertiesPanel: React.FC<PropertiesPanelProps> = ({
  selectedArrow,
  onUpdateArrow,
  onDeleteArrow,
  onDuplicateArrow,
  board,
  arrows,
  setArrows,
  difficulty,
  setDifficulty,
  description,
  setDescription,
}) => {
  const directions: Array<{ dir: Direction; label: string; icon: React.ReactNode }> = [
    { dir: 'UP', label: 'Up', icon: <ArrowUp className="w-4 h-4" /> },
    { dir: 'RIGHT', label: 'Right', icon: <ArrowRight className="w-4 h-4" /> },
    { dir: 'DOWN', label: 'Down', icon: <ArrowDown className="w-4 h-4" /> },
    { dir: 'LEFT', label: 'Left', icon: <ArrowLeft className="w-4 h-4" /> },
  ];

  const handleReversePath = () => {
    if (!selectedArrow) return;
    const revPoints = [...selectedArrow.points].reverse();
    // Re-determine direction based on last segment
    const p1 = revPoints[revPoints.length - 2];
    const p2 = revPoints[revPoints.length - 1];
    const dx = p2.x - p1.x;
    const dy = p2.y - p1.y;

    let direction: Direction = 'RIGHT';
    if (dx > 0) direction = 'RIGHT';
    else if (dx < 0) direction = 'LEFT';
    else if (dy > 0) direction = 'DOWN';
    else if (dy < 0) direction = 'UP';

    onUpdateArrow({
      ...selectedArrow,
      points: revPoints,
      direction,
    });
  };

  const bendCount = selectedArrow ? Math.max(0, selectedArrow.points.length - 2) : 0;

  let pathLength = 0;
  if (selectedArrow) {
    for (let i = 0; i < selectedArrow.points.length - 1; i++) {
      const p1 = selectedArrow.points[i];
      const p2 = selectedArrow.points[i + 1];
      pathLength += Math.abs(p2.x - p1.x) + Math.abs(p2.y - p1.y);
    }
  }

  return (
    <div className="w-80 bg-slate-900 border-l border-slate-800 flex flex-col h-full text-slate-300 select-none overflow-y-auto">
      {/* Panel Section Header */}
      <div className="p-4 border-b border-slate-800">
        <h2 className="text-xs font-bold uppercase tracking-wider text-slate-400">
          Pattern Properties
        </h2>
      </div>

      {/* Selected Arrow Inspector */}
      {selectedArrow ? (
        <div className="p-4 border-b border-slate-800 space-y-4">
          <div className="flex items-center justify-between">
            <span className="text-sm font-bold text-white flex items-center space-x-2">
              <Compass className="w-4 h-4 text-indigo-400" />
              <span>Selected Arrow</span>
            </span>
            <span className="text-xs font-mono text-slate-500 bg-slate-800 px-2 py-0.5 rounded">
              {selectedArrow.id}
            </span>
          </div>

          {/* Direction Selector */}
          <div>
            <label className="text-xs font-semibold text-slate-400 mb-1.5 block">
              Exit Direction
            </label>
            <div className="grid grid-cols-4 gap-1.5 bg-slate-800 p-1 rounded-lg">
              {directions.map((item) => (
                <button
                  key={item.dir}
                  onClick={() => onUpdateArrow({ ...selectedArrow, direction: item.dir })}
                  className={`flex flex-col items-center justify-center p-2 rounded-md transition-colors ${
                    selectedArrow.direction === item.dir
                      ? 'bg-indigo-600 text-white font-bold'
                      : 'text-slate-400 hover:text-slate-200 hover:bg-slate-700/50'
                  }`}
                  title={`Set Direction ${item.label}`}
                >
                  {item.icon}
                  <span className="text-[10px] mt-1">{item.label}</span>
                </button>
              ))}
            </div>
          </div>

          {/* Arrow Stats */}
          <div className="grid grid-cols-2 gap-2 text-xs">
            <div className="bg-slate-800/60 p-2 rounded-lg border border-slate-800">
              <span className="text-slate-500 block">Path Length</span>
              <span className="text-sm font-bold text-white">{pathLength} cells</span>
            </div>
            <div className="bg-slate-800/60 p-2 rounded-lg border border-slate-800">
              <span className="text-slate-500 block">90° Bends</span>
              <span className="text-sm font-bold text-white">{bendCount} bends</span>
            </div>
          </div>

          {/* Coordinates Table */}
          <div>
            <span className="text-xs font-semibold text-slate-400 mb-1.5 block">
              Path Points
            </span>
            <div className="bg-slate-950 p-2 rounded-lg border border-slate-800 max-h-28 overflow-y-auto font-mono text-xs space-y-1">
              {selectedArrow.points.map((p, idx) => (
                <div key={idx} className="flex justify-between text-slate-400">
                  <span>Point {idx + 1}:</span>
                  <span className="text-indigo-300">({p.x}, {p.y})</span>
                </div>
              ))}
            </div>
          </div>

          {/* Arrow Action Buttons */}
          <div className="flex items-center space-x-2 pt-1">
            <button
              onClick={handleReversePath}
              className="flex-1 py-1.5 bg-slate-800 hover:bg-slate-700 text-slate-200 rounded-lg text-xs font-semibold flex items-center justify-center space-x-1.5 border border-slate-700"
            >
              <Repeat className="w-3.5 h-3.5" />
              <span>Reverse Path</span>
            </button>

            <button
              onClick={() => onDuplicateArrow(selectedArrow)}
              className="p-1.5 bg-slate-800 hover:bg-slate-700 text-slate-200 rounded-lg border border-slate-700"
              title="Duplicate Arrow"
            >
              <Copy className="w-4 h-4" />
            </button>

            <button
              onClick={() => onDeleteArrow(selectedArrow.id)}
              className="p-1.5 bg-rose-500/10 hover:bg-rose-500/20 text-rose-400 rounded-lg border border-rose-500/30"
              title="Delete Arrow"
            >
              <Trash2 className="w-4 h-4" />
            </button>
          </div>
        </div>
      ) : (
        <div className="p-4 border-b border-slate-800 text-xs text-slate-500 text-center py-6">
          Click or draw an arrow on the grid to inspect properties.
        </div>
      )}

      {/* Advanced Pattern Construction Generators */}
      <div className="p-4 border-b border-slate-800 space-y-3">
        <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400">
          Pattern Generators & Symmetry
        </h3>

        <div className="grid grid-cols-2 gap-2 text-xs">
          <button
            onClick={() => setArrows(PatternGenerator.generateSpiral(board))}
            className="p-2 bg-slate-800 hover:bg-slate-700 border border-slate-700 rounded-lg text-slate-200 font-medium text-left flex items-center space-x-2"
          >
            <RotateCw className="w-3.5 h-3.5 text-indigo-400" />
            <span>Spiral Ring</span>
          </button>

          <button
            onClick={() => setArrows(PatternGenerator.applyHorizontalSymmetry(arrows, board))}
            className="p-2 bg-slate-800 hover:bg-slate-700 border border-slate-700 rounded-lg text-slate-200 font-medium text-left flex items-center space-x-2"
          >
            <FlipHorizontal className="w-3.5 h-3.5 text-indigo-400" />
            <span>Horizontal Mirror</span>
          </button>

          <button
            onClick={() => setArrows(PatternGenerator.applyVerticalSymmetry(arrows, board))}
            className="p-2 bg-slate-800 hover:bg-slate-700 border border-slate-700 rounded-lg text-slate-200 font-medium text-left flex items-center space-x-2"
          >
            <FlipVertical className="w-3.5 h-3.5 text-indigo-400" />
            <span>Vertical Mirror</span>
          </button>

          <button
            onClick={() => setArrows(PatternGenerator.rotate90Clockwise(arrows, board))}
            className="p-2 bg-slate-800 hover:bg-slate-700 border border-slate-700 rounded-lg text-slate-200 font-medium text-left flex items-center space-x-2"
          >
            <RotateCw className="w-3.5 h-3.5 text-indigo-400" />
            <span>Rotate 90°</span>
          </button>
        </div>
      </div>

      {/* Level Metadata Form */}
      <div className="p-4 space-y-4 text-xs">
        <h3 className="font-bold uppercase tracking-wider text-slate-400">
          Level Metadata
        </h3>

        <div>
          <label className="text-slate-400 font-semibold mb-1 block">
            Difficulty Rating (1 to 5)
          </label>
          <div className="flex items-center space-x-1 bg-slate-800 p-1 rounded-lg">
            {[1, 2, 3, 4, 5].map((lvl) => (
              <button
                key={lvl}
                onClick={() => setDifficulty(lvl)}
                className={`flex-1 py-1 rounded font-bold transition-colors ${
                  difficulty === lvl
                    ? 'bg-amber-500 text-slate-950'
                    : 'text-slate-400 hover:text-slate-200'
                }`}
              >
                {lvl}
              </button>
            ))}
          </div>
        </div>

        <div>
          <label className="text-slate-400 font-semibold mb-1 block">
            Pattern Description (Optional)
          </label>
          <textarea
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="E.g. Interlocking spiral pattern created for community challenge"
            className="w-full bg-slate-800 border border-slate-700 rounded-lg p-2.5 text-xs text-white focus:outline-none focus:border-indigo-500 h-20 resize-none"
          />
        </div>
      </div>
    </div>
  );
};
