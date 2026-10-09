import React, { useRef, useState, useEffect } from 'react';
import { Arrow, Board, Direction, GridPoint } from '../types/game';
import { DIRECTION_DELTAS } from '../engine/CollisionDetector';

interface GridCanvasProps {
  board: Board;
  arrows: Arrow[];
  selectedArrowId: string | null;
  setSelectedArrowId: (id: string | null) => void;
  onUpdateArrow: (arrow: Arrow) => void;
  onAddArrow: (arrow: Arrow) => void;
  zoom: number;
  isPlaytesting: boolean;
  onArrowTappedInPlaytest?: (arrowId: string) => void;
  escapingArrowId?: string | null;
  wrongMoveArrowId?: string | null;
}

export const GridCanvas: React.FC<GridCanvasProps> = ({
  board,
  arrows,
  selectedArrowId,
  setSelectedArrowId,
  onUpdateArrow,
  onAddArrow,
  zoom,
  isPlaytesting,
  onArrowTappedInPlaytest,
  escapingArrowId,
  wrongMoveArrowId,
}) => {
  const containerRef = useRef<HTMLDivElement>(null);
  const [drawingPoints, setDrawingPoints] = useState<GridPoint[]>([]);
  const [isDrawing, setIsDrawing] = useState(false);
  const [hoveredCell, setHoveredCell] = useState<GridPoint | null>(null);

  const cellSize = 60 * zoom;
  const padding = 40;
  const gridWidthPx = board.width * cellSize;
  const gridHeightPx = board.height * cellSize;

  // Converts pixel coordinates within the grid to discrete grid coordinates (0..width-1, 0..height-1)
  const getGridPointFromEvent = (e: React.MouseEvent<SVGSVGElement>): GridPoint | null => {
    const svg = e.currentTarget;
    const rect = svg.getBoundingClientRect();
    const x = e.clientX - rect.left - padding;
    const y = e.clientY - rect.top - padding;

    const gx = Math.floor(x / cellSize);
    const gy = Math.floor(y / cellSize);

    if (gx >= 0 && gx < board.width && gy >= 0 && gy < board.height) {
      return { x: gx, y: gy };
    }
    return null;
  };

  const handleMouseDown = (e: React.MouseEvent<SVGSVGElement>) => {
    if (isPlaytesting) return;

    const point = getGridPointFromEvent(e);
    if (!point) return;

    // Check if clicked on an existing arrow
    const clickedArrow = arrows.find((a) =>
      a.points.some((p) => p.x === point.x && p.y === point.y)
    );

    if (clickedArrow) {
      setSelectedArrowId(clickedArrow.id);
      setIsDrawing(false);
      setDrawingPoints([]);
      return;
    }

    // Start drawing a new arrow path
    setSelectedArrowId(null);
    setIsDrawing(true);
    setDrawingPoints([point]);
  };

  const handleMouseMove = (e: React.MouseEvent<SVGSVGElement>) => {
    const point = getGridPointFromEvent(e);
    setHoveredCell(point);

    if (!isDrawing || !point || drawingPoints.length === 0) return;

    const lastPoint = drawingPoints[drawingPoints.length - 1];

    if (lastPoint.x === point.x && lastPoint.y === point.y) return;

    // Force orthogonal 90-degree moves
    if (lastPoint.x === point.x || lastPoint.y === point.y) {
      // Check if backing up
      if (
        drawingPoints.length >= 2 &&
        drawingPoints[drawingPoints.length - 2].x === point.x &&
        drawingPoints[drawingPoints.length - 2].y === point.y
      ) {
        setDrawingPoints(drawingPoints.slice(0, -1));
      } else {
        setDrawingPoints([...drawingPoints, point]);
      }
    }
  };

  const handleMouseUp = () => {
    if (!isDrawing) return;
    setIsDrawing(false);

    if (drawingPoints.length >= 2) {
      // Determine direction from last segment
      const p1 = drawingPoints[drawingPoints.length - 2];
      const p2 = drawingPoints[drawingPoints.length - 1];
      const dx = p2.x - p1.x;
      const dy = p2.y - p1.y;

      let direction: Direction = 'RIGHT';
      if (dx > 0) direction = 'RIGHT';
      else if (dx < 0) direction = 'LEFT';
      else if (dy > 0) direction = 'DOWN';
      else if (dy < 0) direction = 'UP';

      const newArrow: Arrow = {
        id: 'arrow_' + Math.random().toString(36).substring(2, 9),
        direction,
        points: drawingPoints,
      };

      onAddArrow(newArrow);
      setSelectedArrowId(newArrow.id);
    }

    setDrawingPoints([]);
  };

  return (
    <div
      ref={containerRef}
      className="flex-1 bg-slate-950 overflow-auto flex items-center justify-center p-8 select-none relative"
    >
      <div className="bg-slate-900 border border-slate-800 rounded-2xl shadow-2xl p-6 relative">
        <svg
          width={gridWidthPx + padding * 2}
          height={gridHeightPx + padding * 2}
          onMouseDown={handleMouseDown}
          onMouseMove={handleMouseMove}
          onMouseUp={handleMouseUp}
          className="cursor-crosshair overflow-visible"
        >
          {/* Grid Background & Coordinate Labels */}
          <g transform={`translate(${padding}, ${padding})`}>
            {/* Grid cell tiles */}
            {Array.from({ length: board.height }).map((_, r) =>
              Array.from({ length: board.width }).map((_, c) => {
                const isHovered = hoveredCell?.x === c && hoveredCell?.y === r;
                return (
                  <rect
                    key={`cell-${r}-${c}`}
                    x={c * cellSize}
                    y={r * cellSize}
                    width={cellSize}
                    height={cellSize}
                    fill={isHovered ? 'rgba(99, 102, 241, 0.15)' : 'rgba(15, 23, 42, 0.6)'}
                    stroke="rgba(51, 65, 85, 0.4)"
                    strokeWidth="1"
                  />
                );
              })
            )}

            {/* Grid Column Coordinates (Top) */}
            {Array.from({ length: board.width }).map((_, c) => (
              <text
                key={`col-${c}`}
                x={c * cellSize + cellSize / 2}
                y={-12}
                fill="#64748b"
                fontSize={Math.max(10, Math.min(14, 12 * zoom))}
                fontWeight="bold"
                textAnchor="middle"
              >
                {c}
              </text>
            ))}

            {/* Grid Row Coordinates (Left) */}
            {Array.from({ length: board.height }).map((_, r) => (
              <text
                key={`row-${r}`}
                x={-15}
                y={r * cellSize + cellSize / 2 + 4}
                fill="#64748b"
                fontSize={Math.max(10, Math.min(14, 12 * zoom))}
                fontWeight="bold"
                textAnchor="middle"
              >
                {r}
              </text>
            ))}

            {/* Render Saved Arrows */}
            {arrows.map((arrow) => {
              const isSelected = arrow.id === selectedArrowId;
              const isEscaping = arrow.id === escapingArrowId;
              const isWrongMove = arrow.id === wrongMoveArrowId;

              const points = arrow.points;
              const pathD = points
                .map(
                  (p, i) =>
                    `${i === 0 ? 'M' : 'L'} ${p.x * cellSize + cellSize / 2} ${
                      p.y * cellSize + cellSize / 2
                    }`
                )
                .join(' ');

              const head = points[points.length - 1];
              const headCenterX = head.x * cellSize + cellSize / 2;
              const headCenterY = head.y * cellSize + cellSize / 2;
              const delta = DIRECTION_DELTAS[arrow.direction];

              let arrowColor = '#1e293b'; // Navy default
              let strokeColor = isSelected ? '#38bdf8' : '#334155';
              let headFill = isSelected ? '#38bdf8' : '#0284c7';

              if (isEscaping) {
                strokeColor = '#10b981';
                headFill = '#10b981';
              } else if (isWrongMove) {
                strokeColor = '#f43f5e';
                headFill = '#f43f5e';
              }

              return (
                <g
                  key={arrow.id}
                  onClick={(e) => {
                    if (isPlaytesting && onArrowTappedInPlaytest) {
                      e.stopPropagation();
                      onArrowTappedInPlaytest(arrow.id);
                    }
                  }}
                  className="cursor-pointer transition-all duration-200 hover:opacity-90"
                >
                  {/* Arrow Polyline Body */}
                  <path
                    d={pathD}
                    fill="none"
                    stroke={strokeColor}
                    strokeWidth={Math.max(6, 12 * zoom)}
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  />

                  {/* Arrowhead */}
                  <g
                    transform={`translate(${headCenterX}, ${headCenterY}) rotate(${delta.degrees})`}
                  >
                    <path
                      d="M 12 0 L -8 -10 L -4 0 L -8 10 Z"
                      fill={headFill}
                      transform={`scale(${Math.max(0.7, zoom)})`}
                    />
                  </g>

                  {/* Selection Point Handles */}
                  {isSelected &&
                    points.map((p, idx) => (
                      <circle
                        key={`handle-${idx}`}
                        cx={p.x * cellSize + cellSize / 2}
                        cy={p.y * cellSize + cellSize / 2}
                        r={Math.max(4, 6 * zoom)}
                        fill="#38bdf8"
                        stroke="#0f172a"
                        strokeWidth="2"
                      />
                    ))}
                </g>
              );
            })}

            {/* Render In-Progress Drawing Polyline */}
            {isDrawing && drawingPoints.length >= 2 && (
              <g>
                <path
                  d={drawingPoints
                    .map(
                      (p, i) =>
                        `${i === 0 ? 'M' : 'L'} ${p.x * cellSize + cellSize / 2} ${
                          p.y * cellSize + cellSize / 2
                        }`
                    )
                    .join(' ')}
                  fill="none"
                  stroke="#818cf8"
                  strokeWidth={Math.max(6, 12 * zoom)}
                  strokeDasharray="6 4"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                />
              </g>
            )}
          </g>
        </svg>

        {/* Hover Coordinate Status Bar */}
        <div className="absolute bottom-3 left-6 text-xs font-mono text-slate-400 bg-slate-900/90 px-3 py-1 rounded-md border border-slate-800">
          {hoveredCell ? `Grid: (${hoveredCell.x}, ${hoveredCell.y})` : 'Hover Grid'}
        </div>
      </div>
    </div>
  );
};
