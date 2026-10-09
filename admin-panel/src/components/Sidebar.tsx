import React from 'react';
import { PlusCircle, Folder, FileText, CheckCircle2, Layers } from 'lucide-react';

export type TabType = 'editor' | 'my_patterns' | 'drafts' | 'published';

interface SidebarProps {
  activeTab: TabType;
  setActiveTab: (tab: TabType) => void;
  draftCount: number;
  publishedCount: number;
  onNewPattern: () => void;
}

export const Sidebar: React.FC<SidebarProps> = ({
  activeTab,
  setActiveTab,
  draftCount,
  publishedCount,
  onNewPattern,
}) => {
  return (
    <div className="w-64 bg-slate-900 border-r border-slate-800 flex flex-col h-full text-slate-300 select-none">
      {/* App Branding Header */}
      <div className="p-5 border-b border-slate-800 flex items-center space-x-3">
        <div className="w-10 h-10 rounded-xl bg-indigo-600 flex items-center justify-center text-white font-bold text-xl shadow-lg shadow-indigo-500/30">
          <Layers className="w-6 h-6" />
        </div>
        <div>
          <h1 className="font-bold text-white text-base tracking-tight leading-none">Arrows Admin</h1>
          <p className="text-xs text-indigo-400 font-medium mt-1 uppercase tracking-wider">Pattern Designer</p>
        </div>
      </div>

      {/* Navigation Links */}
      <div className="p-3 space-y-1 flex-1">
        <button
          onClick={() => {
            onNewPattern();
            setActiveTab('editor');
          }}
          className="w-full flex items-center space-x-3 px-3.5 py-2.5 rounded-lg text-sm font-semibold text-white bg-indigo-600 hover:bg-indigo-500 transition-colors shadow-sm mb-4"
        >
          <PlusCircle className="w-4 h-4" />
          <span>New Pattern</span>
        </button>

        <button
          onClick={() => setActiveTab('editor')}
          className={`w-full flex items-center justify-between px-3.5 py-2.5 rounded-lg text-sm font-medium transition-colors ${
            activeTab === 'editor'
              ? 'bg-slate-800 text-white font-semibold'
              : 'hover:bg-slate-800/60 text-slate-400 hover:text-slate-200'
          }`}
        >
          <div className="flex items-center space-x-3">
            <Layers className="w-4 h-4" />
            <span>Active Designer</span>
          </div>
        </button>

        <button
          onClick={() => setActiveTab('my_patterns')}
          className={`w-full flex items-center justify-between px-3.5 py-2.5 rounded-lg text-sm font-medium transition-colors ${
            activeTab === 'my_patterns'
              ? 'bg-slate-800 text-white font-semibold'
              : 'hover:bg-slate-800/60 text-slate-400 hover:text-slate-200'
          }`}
        >
          <div className="flex items-center space-x-3">
            <Folder className="w-4 h-4" />
            <span>My Patterns</span>
          </div>
          <span className="text-xs bg-slate-800 px-2 py-0.5 rounded-full text-slate-400">
            {draftCount + publishedCount}
          </span>
        </button>

        <button
          onClick={() => setActiveTab('drafts')}
          className={`w-full flex items-center justify-between px-3.5 py-2.5 rounded-lg text-sm font-medium transition-colors ${
            activeTab === 'drafts'
              ? 'bg-slate-800 text-white font-semibold'
              : 'hover:bg-slate-800/60 text-slate-400 hover:text-slate-200'
          }`}
        >
          <div className="flex items-center space-x-3">
            <FileText className="w-4 h-4" />
            <span>Drafts</span>
          </div>
          <span className="text-xs bg-amber-500/20 text-amber-400 px-2 py-0.5 rounded-full font-semibold">
            {draftCount}
          </span>
        </button>

        <button
          onClick={() => setActiveTab('published')}
          className={`w-full flex items-center justify-between px-3.5 py-2.5 rounded-lg text-sm font-medium transition-colors ${
            activeTab === 'published'
              ? 'bg-slate-800 text-white font-semibold'
              : 'hover:bg-slate-800/60 text-slate-400 hover:text-slate-200'
          }`}
        >
          <div className="flex items-center space-x-3">
            <CheckCircle2 className="w-4 h-4" />
            <span>Published</span>
          </div>
          <span className="text-xs bg-emerald-500/20 text-emerald-400 px-2 py-0.5 rounded-full font-semibold">
            {publishedCount}
          </span>
        </button>
      </div>

      {/* Footer Info */}
      <div className="p-4 border-t border-slate-800 text-xs text-slate-500 space-y-1">
        <p className="font-semibold text-slate-400">Arrows Puzzle Escape Engine</p>
        <p>Compatible with Android v1.1+</p>
        <p className="text-[10px] text-slate-600 mt-2">Real-time Supabase Sync</p>
      </div>
    </div>
  );
};
