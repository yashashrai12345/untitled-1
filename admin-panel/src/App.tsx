import React, { useState, useEffect, useMemo } from 'react';
import { Arrow, Board, CustomPatternRecord, Level, ValidationResult, SolveResult } from './types/game';
import { LevelValidator } from './engine/LevelValidator';
import { PuzzleSolver } from './engine/PuzzleSolver';
import { Sidebar, TabType } from './components/Sidebar';
import { Header } from './components/Header';
import { GridCanvas } from './components/GridCanvas';
import { PropertiesPanel } from './components/PropertiesPanel';
import { ValidationPanel } from './components/ValidationPanel';
import { PlaytestOverlay } from './components/PlaytestOverlay';
import { PublishModal } from './components/PublishModal';
import { PatternListModal } from './components/PatternListModal';
import { fetchPublishedPatterns, subscribeToRealtimePatterns } from './lib/supabase';

const INITIAL_BOARD: Board = { width: 8, height: 8 };

export const App: React.FC = () => {
  const [activeTab, setActiveTab] = useState<TabType>('editor');
  const [patternName, setPatternName] = useState('Level 1');
  const [description, setDescription] = useState('');
  const [difficulty, setDifficulty] = useState(2);
  const [board, setBoard] = useState<Board>(INITIAL_BOARD);
  const [arrows, setArrows] = useState<Arrow[]>([]);

  const [selectedArrowId, setSelectedArrowId] = useState<string | null>(null);
  const [zoom, setZoom] = useState(1.0);
  const [snapToGrid, setSnapToGrid] = useState(true);
  const [isPlaytesting, setIsPlaytesting] = useState(false);
  const [isPublishModalOpen, setIsPublishModalOpen] = useState(false);

  // Undo/Redo History
  const [history, setHistory] = useState<Arrow[][]>([]);
  const [redoStack, setRedoStack] = useState<Arrow[][]>([]);

  // Local Drafts (Starts completely clean with 0 drafts)
  const [savedDrafts, setSavedDrafts] = useState<CustomPatternRecord[]>(() => {
    const local = localStorage.getItem('arrows_admin_drafts');
    return local ? JSON.parse(local) : [];
  });

  const [publishedPatterns, setPublishedPatterns] = useState<CustomPatternRecord[]>([]);

  // Fetch live published patterns from Supabase on load & subscribe to Realtime updates!
  useEffect(() => {
    fetchPublishedPatterns().then((data) => {
      if (data) {
        setPublishedPatterns(data);
      }
    });

    const channel = subscribeToRealtimePatterns((latestPatterns) => {
      setPublishedPatterns(latestPatterns);
    });

    return () => {
      if (channel) {
        channel.unsubscribe();
      }
    };
  }, []);

  // Save drafts to LocalStorage
  useEffect(() => {
    localStorage.setItem('arrows_admin_drafts', JSON.stringify(savedDrafts));
  }, [savedDrafts]);

  // Current Level Object
  const currentLevel: Level = useMemo(
    () => ({
      id: Date.now(),
      name: patternName,
      board,
      arrows,
      difficulty,
      parMoves: arrows.length,
      patternType: 'CUSTOM',
      seed: Date.now(),
      difficultyScore: difficulty * 1.0,
      level_number: publishedPatterns.length + 1,
    }),
    [patternName, board, arrows, difficulty, publishedPatterns.length]
  );

  // Live Validation & Solvability Engine
  const validation: ValidationResult = useMemo(
    () => LevelValidator.validateLevel(currentLevel),
    [currentLevel]
  );

  const solveResult: SolveResult = useMemo(
    () => PuzzleSolver.solvePuzzle(arrows, board),
    [arrows, board]
  );

  // Push state to history before changing
  const pushHistory = (newArrows: Arrow[]) => {
    setHistory((prev) => [...prev, arrows]);
    setRedoStack([]);
    setArrows(newArrows);
  };

  const handleUndo = () => {
    if (history.length === 0) return;
    const previous = history[history.length - 1];
    setRedoStack((prev) => [...prev, arrows]);
    setHistory((prev) => prev.slice(0, -1));
    setArrows(previous);
  };

  const handleRedo = () => {
    if (redoStack.length === 0) return;
    const next = redoStack[redoStack.length - 1];
    setHistory((prev) => [...prev, arrows]);
    setRedoStack((prev) => prev.slice(0, -1));
    setArrows(next);
  };

  const handleAddArrow = (arrow: Arrow) => {
    pushHistory([...arrows, arrow]);
  };

  const handleUpdateArrow = (updated: Arrow) => {
    pushHistory(arrows.map((a) => (a.id === updated.id ? updated : a)));
  };

  const handleDeleteArrow = (id: string) => {
    pushHistory(arrows.filter((a) => a.id !== id));
    if (selectedArrowId === id) setSelectedArrowId(null);
  };

  const handleDuplicateArrow = (arrow: Arrow) => {
    const dup: Arrow = {
      ...arrow,
      id: 'arrow_' + Math.random().toString(36).substring(2, 9),
      points: arrow.points.map((p) => ({ x: Math.min(board.width - 1, p.x + 1), y: p.y })),
    };
    pushHistory([...arrows, dup]);
    setSelectedArrowId(dup.id);
  };

  const handleNewPattern = () => {
    const nextLvlNum = publishedPatterns.length + 1;
    setPatternName(`Level ${nextLvlNum}`);
    setDescription('');
    setDifficulty(2);
    setBoard(INITIAL_BOARD);
    setArrows([]);
    setSelectedArrowId(null);
    setHistory([]);
    setRedoStack([]);
  };

  const handleSaveDraft = () => {
    const draftRecord: CustomPatternRecord = {
      id: 'draft_' + Date.now(),
      name: patternName,
      description,
      width: board.width,
      height: board.height,
      level_data: currentLevel,
      difficulty,
      status: 'draft',
      version: 1,
      updated_at: new Date().toISOString(),
    };

    setSavedDrafts((prev) => [draftRecord, ...prev.filter((d) => d.name !== patternName)]);
    alert(`Draft "${patternName}" saved successfully!`);
  };

  const handleExportJSON = () => {
    const jsonStr = JSON.stringify(currentLevel, null, 2);
    const blob = new Blob([jsonStr], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${patternName.toLowerCase().replace(/\s+/g, '_')}_level.json`;
    a.click();
    URL.revokeObjectURL(url);
  };

  const handleImportJSON = () => {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = 'application/json';
    input.onchange = (e) => {
      const file = (e.target as HTMLInputElement).files?.[0];
      if (!file) return;

      const reader = new FileReader();
      reader.onload = (event) => {
        try {
          const parsed = JSON.parse(event.target?.result as string);
          if (parsed.board && parsed.arrows) {
            setPatternName(parsed.name || 'Imported Pattern');
            setBoard({ width: parsed.board.width, height: parsed.board.height });
            setArrows(parsed.arrows);
            setDifficulty(parsed.difficulty || 2);
            pushHistory(parsed.arrows);
            alert('Pattern imported successfully!');
          } else {
            alert('Invalid level JSON file structure.');
          }
        } catch (err) {
          alert('Failed to parse level JSON.');
        }
      };
      reader.readAsText(file);
    };
    input.click();
  };

  const selectedArrow = useMemo(
    () => arrows.find((a) => a.id === selectedArrowId) || null,
    [arrows, selectedArrowId]
  );

  return (
    <div className="flex h-screen bg-slate-950 font-sans text-slate-100 overflow-hidden">
      {/* Sidebar Navigation */}
      <Sidebar
        activeTab={activeTab}
        setActiveTab={setActiveTab}
        draftCount={savedDrafts.length}
        publishedCount={publishedPatterns.length}
        onNewPattern={handleNewPattern}
      />

      {/* Main Content Workspace */}
      <div className="flex-1 flex flex-col h-full overflow-hidden relative">
        {activeTab === 'editor' ? (
          <>
            <Header
              patternName={patternName}
              setPatternName={setPatternName}
              width={board.width}
              height={board.height}
              setWidth={(w) => setBoard((b) => ({ ...b, width: w }))}
              setHeight={(h) => setBoard((b) => ({ ...b, height: h }))}
              zoom={zoom}
              setZoom={setZoom}
              fitToScreen={() => setZoom(1.0)}
              canUndo={history.length > 0}
              canRedo={redoStack.length > 0}
              onUndo={handleUndo}
              onRedo={handleRedo}
              onClear={() => pushHistory([])}
              onSaveDraft={handleSaveDraft}
              onPublish={() => setIsPublishModalOpen(true)}
              isPlaytesting={isPlaytesting}
              setIsPlaytesting={setIsPlaytesting}
              snapToGrid={snapToGrid}
              setSnapToGrid={setSnapToGrid}
              onExportJSON={handleExportJSON}
              onImportJSON={handleImportJSON}
            />

            {/* Canvas Interactive Grid Workspace */}
            <div className="flex-1 flex overflow-hidden relative">
              <GridCanvas
                board={board}
                arrows={arrows}
                selectedArrowId={selectedArrowId}
                setSelectedArrowId={setSelectedArrowId}
                onUpdateArrow={handleUpdateArrow}
                onAddArrow={handleAddArrow}
                zoom={zoom}
                isPlaytesting={isPlaytesting}
              />

              {/* Right Properties Panel */}
              <PropertiesPanel
                selectedArrow={selectedArrow}
                onUpdateArrow={handleUpdateArrow}
                onDeleteArrow={handleDeleteArrow}
                onDuplicateArrow={handleDuplicateArrow}
                board={board}
                arrows={arrows}
                setArrows={setArrows}
                difficulty={difficulty}
                setDifficulty={setDifficulty}
                description={description}
                setDescription={setDescription}
              />
            </div>

            {/* Validation Results Footer Bar */}
            <ValidationPanel
              validation={validation}
              solveResult={solveResult}
              arrows={arrows}
              board={board}
            />

            {/* Playtest Overlay Banner */}
            {isPlaytesting && (
              <PlaytestOverlay
                initialArrows={arrows}
                board={board}
                onExitPlaytest={() => setIsPlaytesting(false)}
              />
            )}
          </>
        ) : (
          <PatternListModal
            patterns={activeTab === 'published' ? publishedPatterns : savedDrafts}
            filterStatus={activeTab === 'published' ? 'published' : 'draft'}
            onSelectPattern={(pattern) => {
              setPatternName(pattern.name);
              setDescription(pattern.description || '');
              setBoard({ width: pattern.width, height: pattern.height });
              setArrows(pattern.level_data.arrows || []);
              setDifficulty(pattern.difficulty);
              setActiveTab('editor');
            }}
            onDeletePattern={(id) => {
              setSavedDrafts((prev) => prev.filter((d) => d.id !== id));
              setPublishedPatterns((prev) => prev.filter((p) => p.id !== id));
            }}
            onPublishPattern={(pattern) => {
              setIsPublishModalOpen(true);
            }}
          />
        )}
      </div>

      {/* Publishing Modal */}
      <PublishModal
        isOpen={isPublishModalOpen}
        onClose={() => setIsPublishModalOpen(false)}
        level={currentLevel}
        description={description}
        difficulty={difficulty}
        defaultLevelNumber={publishedPatterns.length + 1}
        onPublishedSuccess={() => {
          fetchPublishedPatterns().then((data) => {
            if (data) setPublishedPatterns(data);
          });
        }}
      />
    </div>
  );
};
