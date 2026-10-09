import React from 'react';
import { CustomPatternRecord } from '../types/game';
import { Edit3, Trash2, CheckCircle2, FileText, Send, Layers } from 'lucide-react';

interface PatternListModalProps {
  patterns: CustomPatternRecord[];
  filterStatus?: 'draft' | 'published' | 'all';
  onSelectPattern: (pattern: CustomPatternRecord) => void;
  onDeletePattern: (id: string) => void;
  onPublishPattern: (pattern: CustomPatternRecord) => void;
}

export const PatternListModal: React.FC<PatternListModalProps> = ({
  patterns,
  filterStatus = 'all',
  onSelectPattern,
  onDeletePattern,
  onPublishPattern,
}) => {
  const filtered = patterns.filter((p) => {
    if (filterStatus === 'draft') return p.status === 'draft';
    if (filterStatus === 'published') return p.status === 'published';
    return true;
  });

  return (
    <div className="flex-1 bg-slate-950 p-8 overflow-y-auto select-none">
      <div className="max-w-5xl mx-auto space-y-6">
        <div className="flex items-center justify-between border-b border-slate-800 pb-4">
          <div>
            <h2 className="text-xl font-bold text-white flex items-center space-x-2">
              <Layers className="w-5 h-5 text-indigo-400" />
              <span>Pattern Library</span>
            </h2>
            <p className="text-xs text-slate-400 mt-1">
              Manage custom puzzle patterns, drafts, and published levels.
            </p>
          </div>
          <span className="text-xs bg-slate-800 text-slate-300 font-mono px-3 py-1 rounded-full border border-slate-700">
            {filtered.length} {filterStatus} patterns
          </span>
        </div>

        {filtered.length === 0 ? (
          <div className="bg-slate-900 border border-slate-800 rounded-2xl p-12 text-center text-slate-500 space-y-3">
            <p className="text-sm font-semibold text-slate-400">No patterns found in this view</p>
            <p className="text-xs">Create a new pattern using the sidebar to get started!</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {filtered.map((item) => (
              <div
                key={item.id}
                className="bg-slate-900 border border-slate-800 hover:border-slate-700 rounded-xl p-5 space-y-4 transition-all hover:shadow-xl relative flex flex-col justify-between"
              >
                <div className="space-y-2">
                  <div className="flex items-start justify-between">
                    <h3 className="font-bold text-white text-base truncate pr-2">{item.name}</h3>
                    {item.status === 'published' ? (
                      <span className="bg-emerald-500/20 text-emerald-400 text-[10px] font-bold px-2 py-0.5 rounded-full flex items-center space-x-1 border border-emerald-500/30">
                        <CheckCircle2 className="w-3 h-3" />
                        <span>Published</span>
                      </span>
                    ) : (
                      <span className="bg-amber-500/20 text-amber-400 text-[10px] font-bold px-2 py-0.5 rounded-full flex items-center space-x-1 border border-amber-500/30">
                        <FileText className="w-3 h-3" />
                        <span>Draft</span>
                      </span>
                    )}
                  </div>

                  <p className="text-xs text-slate-400 line-clamp-2 min-h-[2rem]">
                    {item.description || 'Handcrafted custom puzzle pattern'}
                  </p>
                </div>

                <div className="space-y-3 pt-2">
                  <div className="grid grid-cols-3 gap-2 bg-slate-950 p-2 rounded-lg text-[11px] font-mono border border-slate-800 text-center">
                    <div>
                      <span className="text-slate-500 block text-[9px]">GRID</span>
                      <span className="font-bold text-indigo-300">{item.width}×{item.height}</span>
                    </div>
                    <div>
                      <span className="text-slate-500 block text-[9px]">ARROWS</span>
                      <span className="font-bold text-white">{item.level_data.arrows?.length || 0}</span>
                    </div>
                    <div>
                      <span className="text-slate-500 block text-[9px]">DIFF</span>
                      <span className="font-bold text-amber-400">{item.difficulty}/5</span>
                    </div>
                  </div>

                  <div className="flex items-center space-x-2">
                    <button
                      onClick={() => onSelectPattern(item)}
                      className="flex-1 py-1.5 bg-indigo-600 hover:bg-indigo-500 text-white rounded-lg text-xs font-semibold flex items-center justify-center space-x-1.5 shadow-md shadow-indigo-600/20"
                    >
                      <Edit3 className="w-3.5 h-3.5" />
                      <span>Edit</span>
                    </button>

                    {item.status === 'draft' && (
                      <button
                        onClick={() => onPublishPattern(item)}
                        className="p-1.5 bg-emerald-600/20 hover:bg-emerald-600/30 text-emerald-400 rounded-lg border border-emerald-500/30"
                        title="Publish Pattern"
                      >
                        <Send className="w-4 h-4" />
                      </button>
                    )}

                    <button
                      onClick={() => onDeletePattern(item.id)}
                      className="p-1.5 bg-rose-500/10 hover:bg-rose-500/20 text-rose-400 rounded-lg border border-rose-500/30"
                      title="Delete Pattern"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};
