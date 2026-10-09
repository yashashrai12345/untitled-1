import React, { useState, useEffect } from 'react';
import { CustomPatternRecord } from '../types/game';
import { Edit3, Trash2, CheckCircle2, FileText, Send, Layers, Hash, ArrowUp, ArrowDown, Save, Move } from 'lucide-react';
import { deletePatternViaApi, reorderPatternsViaApi } from '../lib/supabase';

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
  const [deletingId, setDeletingId] = useState<string | null>(null);
  const [orderedItems, setOrderedItems] = useState<CustomPatternRecord[]>([]);
  const [isReordering, setIsReordering] = useState(false);
  const [isSavingOrder, setIsSavingOrder] = useState(false);

  useEffect(() => {
    const list = [...patterns]
      .filter((p) => {
        if (filterStatus === 'draft') return p.status === 'draft';
        if (filterStatus === 'published') return p.status === 'published';
        return true;
      })
      .sort((a, b) => (a.level_number || 1) - (b.level_number || 1));

    setOrderedItems(list);
  }, [patterns, filterStatus]);

  const handleMove = (index: number, direction: 'up' | 'down') => {
    const newIdx = direction === 'up' ? index - 1 : index + 1;
    if (newIdx < 0 || newIdx >= orderedItems.length) return;

    const list = [...orderedItems];
    const temp = list[index];
    list[index] = list[newIdx];
    list[newIdx] = temp;

    // Re-assign level numbers sequentially (Level 1, Level 2, Level 3...)
    const updatedList = list.map((item, idx) => ({
      ...item,
      level_number: idx + 1,
    }));

    setOrderedItems(updatedList);
    setIsReordering(true);
  };

  const handleSaveOrder = async () => {
    setIsSavingOrder(true);
    const secret = prompt('Enter ADMIN_PUBLISH_SECRET to confirm level order (leave empty if unconfigured):') || '';

    const payload = orderedItems.map((item, idx) => ({
      id: item.id,
      level_number: idx + 1,
    }));

    const result = await reorderPatternsViaApi(payload, secret);
    setIsSavingOrder(false);

    if (result.success) {
      alert('Level progression order saved successfully! App will load levels in this exact sequence.');
      setIsReordering(false);
    } else {
      alert(result.message || 'Failed to save level order.');
    }
  };

  const handleDelete = async (item: CustomPatternRecord) => {
    if (!confirm(`Are you sure you want to delete "${item.name}"?`)) return;

    setDeletingId(item.id);

    if (item.status === 'published') {
      const secret = prompt('Enter ADMIN_PUBLISH_SECRET to confirm deletion (leave empty if unconfigured):') || '';
      const result = await deletePatternViaApi(item.id, secret);
      if (result.success) {
        onDeletePattern(item.id);
      } else {
        alert(result.message || 'Failed to delete pattern.');
      }
    } else {
      onDeletePattern(item.id);
    }

    setDeletingId(null);
  };

  return (
    <div className="flex-1 bg-slate-950 p-8 overflow-y-auto select-none">
      <div className="max-w-5xl mx-auto space-y-6">
        <div className="flex items-center justify-between border-b border-slate-800 pb-4">
          <div>
            <h2 className="text-xl font-bold text-white flex items-center space-x-2">
              <Layers className="w-5 h-5 text-indigo-400" />
              <span>Pattern Library & Level Progression</span>
            </h2>
            <p className="text-xs text-slate-400 mt-1">
              Arrange your custom puzzle patterns level-wise (Level 1, Level 2, Level 3...).
            </p>
          </div>

          <div className="flex items-center space-x-3">
            {isReordering && (
              <button
                onClick={handleSaveOrder}
                disabled={isSavingOrder}
                className="px-4 py-2 bg-emerald-600 hover:bg-emerald-500 text-white rounded-xl text-xs font-bold flex items-center space-x-2 shadow-lg shadow-emerald-600/20"
              >
                <Save className="w-4 h-4" />
                <span>{isSavingOrder ? 'Saving Order...' : 'Save Level Order'}</span>
              </button>
            )}

            <span className="text-xs bg-slate-800 text-slate-300 font-mono px-3 py-1.5 rounded-full border border-slate-700">
              {orderedItems.length} {filterStatus} patterns
            </span>
          </div>
        </div>

        {orderedItems.length === 0 ? (
          <div className="bg-slate-900 border border-slate-800 rounded-2xl p-12 text-center text-slate-500 space-y-3">
            <p className="text-sm font-semibold text-slate-400">No patterns found in this view</p>
            <p className="text-xs">Create a new pattern using the sidebar to get started!</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {orderedItems.map((item, index) => (
              <div
                key={item.id}
                className="bg-slate-900 border border-slate-800 hover:border-slate-700 rounded-xl p-5 space-y-4 transition-all hover:shadow-xl relative flex flex-col justify-between"
              >
                <div className="space-y-2">
                  <div className="flex items-start justify-between">
                    <div className="flex items-center space-x-2">
                      <span className="bg-indigo-600/30 text-indigo-300 text-xs font-mono font-bold px-2 py-0.5 rounded border border-indigo-500/30 flex items-center space-x-1">
                        <Hash className="w-3 h-3" />
                        <span>Level {item.level_number || index + 1}</span>
                      </span>
                      <h3 className="font-bold text-white text-base truncate max-w-[130px]">{item.name}</h3>
                    </div>

                    {/* Order Move Up / Move Down Buttons */}
                    <div className="flex items-center space-x-1 bg-slate-950 p-1 rounded-lg border border-slate-800">
                      <button
                        onClick={() => handleMove(index, 'up')}
                        disabled={index === 0}
                        className="p-1 hover:bg-slate-800 disabled:opacity-20 rounded text-slate-400 hover:text-white"
                        title="Move Level Up"
                      >
                        <ArrowUp className="w-3.5 h-3.5" />
                      </button>
                      <button
                        onClick={() => handleMove(index, 'down')}
                        disabled={index === orderedItems.length - 1}
                        className="p-1 hover:bg-slate-800 disabled:opacity-20 rounded text-slate-400 hover:text-white"
                        title="Move Level Down"
                      >
                        <ArrowDown className="w-3.5 h-3.5" />
                      </button>
                    </div>
                  </div>

                  <p className="text-xs text-slate-400 line-clamp-2 min-h-[2rem]">
                    {item.description || `Handcrafted Level ${item.level_number || index + 1}`}
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
                      <span className="font-bold text-white">{item.level_data?.arrows?.length || 0}</span>
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
                      onClick={() => handleDelete(item)}
                      disabled={deletingId === item.id}
                      className="p-1.5 bg-rose-500/10 hover:bg-rose-500/20 text-rose-400 rounded-lg border border-rose-500/30 disabled:opacity-40"
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
