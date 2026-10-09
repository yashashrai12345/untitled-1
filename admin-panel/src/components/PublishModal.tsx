import React, { useState } from 'react';
import { Send, Key, AlertCircle, CheckCircle, X, Hash } from 'lucide-react';
import { publishPatternViaApi } from '../lib/supabase';
import { CustomPatternRecord, Level } from '../types/game';

interface PublishModalProps {
  isOpen: boolean;
  onClose: () => void;
  level: Level;
  description: string;
  difficulty: number;
  onPublishedSuccess: () => void;
  defaultLevelNumber?: number;
}

export const PublishModal: React.FC<PublishModalProps> = ({
  isOpen,
  onClose,
  level,
  description,
  difficulty,
  onPublishedSuccess,
  defaultLevelNumber = 1,
}) => {
  const [levelNumber, setLevelNumber] = useState<number>(defaultLevelNumber);
  const [publishSecret, setPublishSecret] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);

  if (!isOpen) return null;

  const handlePublish = async () => {
    setIsSubmitting(true);
    setErrorMsg(null);
    setSuccessMsg(null);

    const payload: Partial<CustomPatternRecord> = {
      name: level.name || `Level ${levelNumber}`,
      description,
      width: level.board.width,
      height: level.board.height,
      level_data: { ...level, level_number: levelNumber },
      difficulty,
      status: 'published',
      level_number: levelNumber,
      version: 1,
    };

    const result = await publishPatternViaApi(payload, publishSecret);

    setIsSubmitting(false);

    if (result.success) {
      setSuccessMsg(`Level ${levelNumber} published successfully! Available live in Android app.`);
      setTimeout(() => {
        onPublishedSuccess();
        onClose();
      }, 1500);
    } else {
      setErrorMsg(result.message || 'Failed to publish. Please check your publish secret or network connection.');
    }
  };

  return (
    <div className="fixed inset-0 bg-slate-950/80 backdrop-blur-sm flex items-center justify-center z-50 p-4 select-none">
      <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-md w-full p-6 text-slate-200 shadow-2xl space-y-5 relative">
        <button
          onClick={onClose}
          className="absolute top-4 right-4 p-1 text-slate-500 hover:text-slate-300 rounded-lg hover:bg-slate-800"
        >
          <X className="w-5 h-5" />
        </button>

        <div className="flex items-center space-x-3">
          <div className="w-10 h-10 rounded-xl bg-emerald-500/20 border border-emerald-500/30 flex items-center justify-center text-emerald-400">
            <Send className="w-5 h-5" />
          </div>
          <div>
            <h2 className="text-base font-bold text-white">Publish Level to Android App</h2>
            <p className="text-xs text-slate-400">Make this pattern instantly playable worldwide</p>
          </div>
        </div>

        <div className="space-y-3 bg-slate-950 p-3.5 rounded-xl border border-slate-800/80 text-xs">
          <div className="flex justify-between items-center">
            <span className="text-slate-400 font-semibold flex items-center space-x-1">
              <Hash className="w-3.5 h-3.5 text-indigo-400" />
              <span>Level Order Position:</span>
            </span>
            <div className="flex items-center space-x-2">
              <span className="text-slate-400">Level</span>
              <input
                type="number"
                min={1}
                max={999}
                value={levelNumber}
                onChange={(e) => setLevelNumber(Math.max(1, parseInt(e.target.value) || 1))}
                className="w-16 bg-slate-800 border border-slate-700 text-center rounded-md text-white font-bold py-1 focus:outline-none focus:border-indigo-500"
              />
            </div>
          </div>

          <div className="flex justify-between">
            <span className="text-slate-500">Pattern Name:</span>
            <span className="font-bold text-white">{level.name}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-slate-500">Grid Dimensions:</span>
            <span className="font-mono text-indigo-300">{level.board.width} × {level.board.height}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-slate-500">Total Arrows:</span>
            <span className="font-bold text-white">{level.arrows.length}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-slate-500">Difficulty Rating:</span>
            <span className="font-bold text-amber-400">{difficulty} / 5</span>
          </div>
        </div>

        <div>
          <label className="text-xs font-semibold text-slate-400 mb-1.5 flex items-center justify-between">
            <span className="flex items-center space-x-1.5">
              <Key className="w-3.5 h-3.5 text-indigo-400" />
              <span>Admin Publishing Secret</span>
            </span>
            <span className="text-[10px] text-slate-500">(Optional if unconfigured)</span>
          </label>
          <input
            type="password"
            value={publishSecret}
            onChange={(e) => setPublishSecret(e.target.value)}
            placeholder="Enter ADMIN_PUBLISH_SECRET"
            className="w-full bg-slate-800 border border-slate-700 rounded-lg p-2.5 text-xs text-white focus:outline-none focus:border-indigo-500"
          />
        </div>

        {errorMsg && (
          <div className="flex items-center space-x-2 bg-rose-500/10 border border-rose-500/30 text-rose-400 p-3 rounded-lg text-xs">
            <AlertCircle className="w-4 h-4 flex-shrink-0" />
            <span>{errorMsg}</span>
          </div>
        )}

        {successMsg && (
          <div className="flex items-center space-x-2 bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 p-3 rounded-lg text-xs">
            <CheckCircle className="w-4 h-4 flex-shrink-0" />
            <span>{successMsg}</span>
          </div>
        )}

        <div className="flex space-x-3 pt-2">
          <button
            onClick={onClose}
            className="flex-1 py-2.5 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-xl text-xs font-semibold"
          >
            Cancel
          </button>
          <button
            onClick={handlePublish}
            disabled={isSubmitting}
            className="flex-1 py-2.5 bg-emerald-600 hover:bg-emerald-500 text-white rounded-xl text-xs font-bold shadow-lg shadow-emerald-600/20 disabled:opacity-50"
          >
            {isSubmitting ? 'Publishing...' : `Publish as Level ${levelNumber}`}
          </button>
        </div>
      </div>
    </div>
  );
};
