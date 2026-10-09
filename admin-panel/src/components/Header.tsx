import React from 'react';
import {
  ZoomIn,
  ZoomOut,
  Maximize2,
  Undo2,
  Redo2,
  Trash2,
  Save,
  Send,
  Play,
  Edit3,
  Download,
  Upload,
  Grid,
} from 'lucide-react';

interface HeaderProps {
  patternName: string;
  setPatternName: (name: string) => void;
  width: number;
  height: number;
  setWidth: (w: number) => void;
  setHeight: (h: number) => void;
  zoom: number;
  setZoom: (z: number | ((prev: number) => number)) => void;
  fitToScreen: () => void;
  canUndo: boolean;
  canRedo: boolean;
  onUndo: () => void;
  onRedo: () => void;
  onClear: () => void;
  onSaveDraft: () => void;
  onPublish: () => void;
  isPlaytesting: boolean;
  setIsPlaytesting: (val: boolean) => void;
  snapToGrid: boolean;
  setSnapToGrid: (val: boolean) => void;
  onExportJSON: () => void;
  onImportJSON: () => void;
}

export const Header: React.FC<HeaderProps> = ({
  patternName,
  setPatternName,
  width,
  height,
  setWidth,
  setHeight,
  zoom,
  setZoom,
  fitToScreen,
  canUndo,
  canRedo,
  onUndo,
  onRedo,
  onClear,
  onSaveDraft,
  onPublish,
  isPlaytesting,
  setIsPlaytesting,
  snapToGrid,
  setSnapToGrid,
  onExportJSON,
  onImportJSON,
}) => {
  return (
    <header className="h-16 bg-slate-900 border-b border-slate-800 px-5 flex items-center justify-between text-slate-200 select-none">
      {/* Pattern Name & Dimensions */}
      <div className="flex items-center space-x-4">
        <input
          type="text"
          value={patternName}
          onChange={(e) => setPatternName(e.target.value)}
          placeholder="Pattern Name"
          className="bg-slate-800 border border-slate-700 rounded-lg px-3 py-1.5 text-sm font-semibold text-white focus:outline-none focus:border-indigo-500 w-48"
        />

        {/* Grid dimensions picker */}
        <div className="flex items-center space-x-2 text-xs bg-slate-800/80 px-3 py-1.5 rounded-lg border border-slate-700/80">
          <Grid className="w-3.5 h-3.5 text-indigo-400" />
          <span className="text-slate-400 font-medium">Grid:</span>
          <input
            type="number"
            min={3}
            max={30}
            value={width}
            onChange={(e) => setWidth(Math.max(3, Math.min(30, parseInt(e.target.value) || 5)))}
            className="w-10 bg-slate-900 border border-slate-700 text-center rounded text-white text-xs py-0.5"
          />
          <span className="text-slate-500">×</span>
          <input
            type="number"
            min={3}
            max={30}
            value={height}
            onChange={(e) => setHeight(Math.max(3, Math.min(30, parseInt(e.target.value) || 5)))}
            className="w-10 bg-slate-900 border border-slate-700 text-center rounded text-white text-xs py-0.5"
          />
        </div>
      </div>

      {/* Editor Controls: Zoom, History, Grid Snap */}
      <div className="flex items-center space-x-2">
        <div className="flex items-center bg-slate-800 border border-slate-700 rounded-lg p-0.5">
          <button
            onClick={() => setZoom((z) => Math.max(0.4, z - 0.15))}
            className="p-1.5 hover:bg-slate-700 rounded text-slate-300 hover:text-white"
            title="Zoom Out"
          >
            <ZoomOut className="w-4 h-4" />
          </button>

          <span className="px-2 text-xs font-mono font-medium text-slate-400">
            {Math.round(zoom * 100)}%
          </span>

          <button
            onClick={() => setZoom((z) => Math.min(2.5, z + 0.15))}
            className="p-1.5 hover:bg-slate-700 rounded text-slate-300 hover:text-white"
            title="Zoom In"
          >
            <ZoomIn className="w-4 h-4" />
          </button>

          <button
            onClick={fitToScreen}
            className="p-1.5 hover:bg-slate-700 rounded text-slate-300 hover:text-white border-l border-slate-700 ml-0.5"
            title="Fit to Screen"
          >
            <Maximize2 className="w-3.5 h-3.5" />
          </button>
        </div>

        <div className="flex items-center bg-slate-800 border border-slate-700 rounded-lg p-0.5">
          <button
            onClick={onUndo}
            disabled={!canUndo}
            className="p-1.5 hover:bg-slate-700 disabled:opacity-30 disabled:hover:bg-transparent rounded text-slate-300"
            title="Undo (Ctrl+Z)"
          >
            <Undo2 className="w-4 h-4" />
          </button>

          <button
            onClick={onRedo}
            disabled={!canRedo}
            className="p-1.5 hover:bg-slate-700 disabled:opacity-30 disabled:hover:bg-transparent rounded text-slate-300"
            title="Redo (Ctrl+Y)"
          >
            <Redo2 className="w-4 h-4" />
          </button>
        </div>

        <button
          onClick={() => setSnapToGrid(!snapToGrid)}
          className={`px-2.5 py-1.5 text-xs font-medium rounded-lg border transition-colors ${
            snapToGrid
              ? 'bg-indigo-600/20 text-indigo-300 border-indigo-500/40'
              : 'bg-slate-800 text-slate-400 border-slate-700 hover:text-slate-200'
          }`}
        >
          Snap Grid
        </button>

        <button
          onClick={onClear}
          className="p-2 hover:bg-rose-500/10 text-slate-400 hover:text-rose-400 rounded-lg transition-colors border border-transparent hover:border-rose-500/30"
          title="Clear Board"
        >
          <Trash2 className="w-4 h-4" />
        </button>

        <div className="h-6 w-px bg-slate-800 mx-1" />

        <button
          onClick={onExportJSON}
          className="p-2 bg-slate-800 hover:bg-slate-700 border border-slate-700 rounded-lg text-slate-300 hover:text-white transition-colors text-xs flex items-center space-x-1"
          title="Export Pattern JSON"
        >
          <Download className="w-3.5 h-3.5" />
        </button>

        <button
          onClick={onImportJSON}
          className="p-2 bg-slate-800 hover:bg-slate-700 border border-slate-700 rounded-lg text-slate-300 hover:text-white transition-colors text-xs flex items-center space-x-1"
          title="Import Pattern JSON"
        >
          <Upload className="w-3.5 h-3.5" />
        </button>
      </div>

      {/* Primary Actions: Playtest, Save Draft, Publish */}
      <div className="flex items-center space-x-3">
        <button
          onClick={() => setIsPlaytesting(!isPlaytesting)}
          className={`px-3.5 py-1.5 rounded-lg text-xs font-semibold flex items-center space-x-2 border transition-all ${
            isPlaytesting
              ? 'bg-amber-500/20 text-amber-300 border-amber-500/40 shadow-lg shadow-amber-500/10'
              : 'bg-slate-800 hover:bg-slate-700 text-slate-200 border-slate-700'
          }`}
        >
          {isPlaytesting ? (
            <>
              <Edit3 className="w-3.5 h-3.5" />
              <span>Edit Mode</span>
            </>
          ) : (
            <>
              <Play className="w-3.5 h-3.5 fill-current text-indigo-400" />
              <span>Test Play</span>
            </>
          )}
        </button>

        <button
          onClick={onSaveDraft}
          className="px-3.5 py-1.5 bg-slate-800 hover:bg-slate-700 border border-slate-700 rounded-lg text-slate-200 text-xs font-semibold flex items-center space-x-2 transition-colors"
        >
          <Save className="w-3.5 h-3.5 text-slate-400" />
          <span>Save Draft</span>
        </button>

        <button
          onClick={onPublish}
          className="px-4 py-1.5 bg-emerald-600 hover:bg-emerald-500 text-white rounded-lg text-xs font-bold flex items-center space-x-2 shadow-lg shadow-emerald-600/20 transition-all hover:scale-[1.02] active:scale-[0.98]"
        >
          <Send className="w-3.5 h-3.5" />
          <span>Publish Level</span>
        </button>
      </div>
    </header>
  );
};
