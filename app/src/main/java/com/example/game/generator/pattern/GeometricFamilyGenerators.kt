package com.example.game.generator.pattern

import com.example.game.model.Arrow
import com.example.game.model.Board
import com.example.game.model.Direction
import com.example.game.model.GridPoint
import kotlin.random.Random

/**
 * Procedural building blocks for the 20 required geometric pattern families.
 * Every generator creates genuine non-rectangular, non-box path layouts.
 */
object GeometricFamilyGenerators {

    // Helper to generate a unique arrow
    private fun arrow(id: String, dir: Direction, vararg points: GridPoint): Arrow {
        return Arrow(id = id, direction = dir, points = points.toList())
    }

    private fun arrow(id: String, dir: Direction, points: List<GridPoint>): Arrow {
        return Arrow(id = id, direction = dir, points = points)
    }

    // -------------------------------------------------------------
    // FAMILY 1: WAVES
    // Flowing wave layouts: alternating horizontal/vertical transitions
    // ~~~~~~→
    //    ↘
    // ←~~~~~~
    //       ↘
    // ~~~~~~→
    // -------------------------------------------------------------
    fun generateWavePattern(prefix: String, offsetX: Int, offsetY: Int, width: Int = 8, random: Random): List<Arrow> {
        val arrows = mutableListOf<Arrow>()
        val row0 = offsetY
        val row1 = offsetY + 2
        val row2 = offsetY + 4

        // Wave crest 1: flows right with dipping tail
        arrows.add(arrow("${prefix}_w1", Direction.RIGHT,
            GridPoint(offsetX, row0 + 1),
            GridPoint(offsetX + 1, row0),
            GridPoint(offsetX + minOf(6, width - 1), row0)
        ))

        // Interlocking transition diagonal-step downward
        arrows.add(arrow("${prefix}_w_t1", Direction.DOWN,
            GridPoint(offsetX + 4, row0 + 1),
            GridPoint(offsetX + 4, row1 - 1)
        ))

        // Wave trough: flows left with ascending crest
        arrows.add(arrow("${prefix}_w2", Direction.LEFT,
            GridPoint(offsetX + minOf(7, width - 1), row1 - 1),
            GridPoint(offsetX + minOf(6, width - 1), row1),
            GridPoint(offsetX + 1, row1)
        ))

        // Interlocking transition downward
        arrows.add(arrow("${prefix}_w_t2", Direction.DOWN,
            GridPoint(offsetX + 2, row1 + 1),
            GridPoint(offsetX + 2, row2 - 1)
        ))

        // Wave crest 2: flows right
        arrows.add(arrow("${prefix}_w3", Direction.RIGHT,
            GridPoint(offsetX, row2 + 1),
            GridPoint(offsetX + 1, row2),
            GridPoint(offsetX + minOf(7, width - 1), row2)
        ))

        return arrows
    }

    // -------------------------------------------------------------
    // FAMILY 2: ZIGZAG / SERPENTINE
    // Long continuous-looking zigzag arrangements:
    // →────┐
    //      ↓
    // ←────┘
    //      ↓
    // →────┐
    // -------------------------------------------------------------
    fun generateZigzagPattern(prefix: String, offsetX: Int, offsetY: Int, width: Int = 7, random: Random): List<Arrow> {
        val arrows = mutableListOf<Arrow>()
        val w = maxOf(4, minOf(width, 7))

        // Turn 1: Top rightward into downward bend
        arrows.add(arrow("${prefix}_zz1", Direction.RIGHT,
            GridPoint(offsetX, offsetY),
            GridPoint(offsetX + w - 1, offsetY)
        ))

        // Vertical drop connecting to turn 2
        arrows.add(arrow("${prefix}_zz_v1", Direction.DOWN,
            GridPoint(offsetX + w, offsetY),
            GridPoint(offsetX + w, offsetY + 2)
        ))

        // Turn 2: Leftward traverse
        arrows.add(arrow("${prefix}_zz2", Direction.LEFT,
            GridPoint(offsetX + w - 1, offsetY + 2),
            GridPoint(offsetX + 1, offsetY + 2)
        ))

        // Vertical drop on left side
        arrows.add(arrow("${prefix}_zz_v2", Direction.DOWN,
            GridPoint(offsetX, offsetY + 2),
            GridPoint(offsetX, offsetY + 4)
        ))

        // Turn 3: Rightward traverse
        arrows.add(arrow("${prefix}_zz3", Direction.RIGHT,
            GridPoint(offsetX + 1, offsetY + 4),
            GridPoint(offsetX + w - 1, offsetY + 4)
        ))

        // Drop 3
        arrows.add(arrow("${prefix}_zz_v3", Direction.DOWN,
            GridPoint(offsetX + w, offsetY + 4),
            GridPoint(offsetX + w, offsetY + 6)
        ))

        // Turn 4: Leftward escape
        arrows.add(arrow("${prefix}_zz4", Direction.LEFT,
            GridPoint(offsetX + w - 1, offsetY + 6),
            GridPoint(offsetX, offsetY + 6)
        ))

        return arrows
    }

    // -------------------------------------------------------------
    // FAMILY 3: LADDER / RUNG STRUCTURES
    // Multiple horizontal paths connected by offset vertical rungs.
    // No outer rectangle!
    // →────────
    //     │
    // ←────────
    //  │
    // →────────
    // -------------------------------------------------------------
    fun generateLadderPattern(prefix: String, offsetX: Int, offsetY: Int, random: Random): List<Arrow> {
        val arrows = mutableListOf<Arrow>()
        // Rung 0
        arrows.add(arrow("${prefix}_r0", Direction.RIGHT,
            GridPoint(offsetX, offsetY),
            GridPoint(offsetX + 6, offsetY)
        ))
        // Vertical connector 1 (offset at x=4)
        arrows.add(arrow("${prefix}_v1", Direction.DOWN,
            GridPoint(offsetX + 4, offsetY + 1),
            GridPoint(offsetX + 4, offsetY + 2)
        ))
        // Rung 1
        arrows.add(arrow("${prefix}_r1", Direction.LEFT,
            GridPoint(offsetX + 7, offsetY + 3),
            GridPoint(offsetX + 1, offsetY + 3)
        ))
        // Vertical connector 2 (offset at x=1)
        arrows.add(arrow("${prefix}_v2", Direction.DOWN,
            GridPoint(offsetX + 1, offsetY + 4),
            GridPoint(offsetX + 1, offsetY + 5)
        ))
        // Rung 2
        arrows.add(arrow("${prefix}_r2", Direction.RIGHT,
            GridPoint(offsetX + 2, offsetY + 6),
            GridPoint(offsetX + 8, offsetY + 6)
        ))
        // Vertical connector 3 (offset at x=6)
        arrows.add(arrow("${prefix}_v3", Direction.UP,
            GridPoint(offsetX + 6, offsetY + 5),
            GridPoint(offsetX + 6, offsetY + 4)
        ))
        return arrows
    }

    // -------------------------------------------------------------
    // FAMILY 4: STAIRCASE
    // Stepping ascending / descending diagonal steps:
    //       ┌──→
    //    ┌──┘
    //    ↑
    //  ┌─┘
    // -------------------------------------------------------------
    fun generateStaircasePattern(prefix: String, offsetX: Int, offsetY: Int, ascending: Boolean = true, random: Random): List<Arrow> {
        val arrows = mutableListOf<Arrow>()
        val steps = 4
        for (i in 0 until steps) {
            val sx = offsetX + i * 2
            val sy = if (ascending) offsetY + (steps - 1 - i) * 2 else offsetY + i * 2

            // Horizontal tread
            arrows.add(arrow("${prefix}_st_h$i", if (i % 2 == 0) Direction.RIGHT else Direction.LEFT,
                if (i % 2 == 0) listOf(GridPoint(sx, sy), GridPoint(sx + 1, sy))
                else listOf(GridPoint(sx + 1, sy), GridPoint(sx, sy))
            ))

            // Vertical riser
            val riserDir = if (ascending) Direction.UP else Direction.DOWN
            val ry1 = if (ascending) sy - 1 else sy + 1
            val ry2 = if (ascending) sy - 2 else sy + 2
            arrows.add(arrow("${prefix}_st_v$i", riserDir,
                GridPoint(sx + 1, ry1),
                GridPoint(sx + 1, ry2)
            ))
        }
        return arrows
    }

    // -------------------------------------------------------------
    // FAMILY 5: SNAKE / MEANDER
    // True flowing snake-like structure (open ends, not a closed box):
    // →───────┐
    //         ↓
    // ←───────┘
    // ┌───────→
    // ↑
    // -------------------------------------------------------------
    fun generateSnakePattern(prefix: String, offsetX: Int, offsetY: Int, random: Random): List<Arrow> {
        val arrows = mutableListOf<Arrow>()
        // Head segment curling in
        arrows.add(arrow("${prefix}_sn1", Direction.RIGHT,
            GridPoint(offsetX, offsetY),
            GridPoint(offsetX + 5, offsetY),
            GridPoint(offsetX + 5, offsetY + 1)
        ))
        // Middle body sweeping left
        arrows.add(arrow("${prefix}_sn2", Direction.LEFT,
            GridPoint(offsetX + 6, offsetY + 2),
            GridPoint(offsetX + 1, offsetY + 2)
        ))
        // Turn connecting to lower body
        arrows.add(arrow("${prefix}_sn3", Direction.DOWN,
            GridPoint(offsetX, offsetY + 2),
            GridPoint(offsetX, offsetY + 4)
        ))
        // Lower body sweeping right
        arrows.add(arrow("${prefix}_sn4", Direction.RIGHT,
            GridPoint(offsetX + 1, offsetY + 4),
            GridPoint(offsetX + 6, offsetY + 4),
            GridPoint(offsetX + 6, offsetY + 5)
        ))
        // Tail segment
        arrows.add(arrow("${prefix}_sn5", Direction.LEFT,
            GridPoint(offsetX + 5, offsetY + 6),
            GridPoint(offsetX, offsetY + 6)
        ))
        return arrows
    }

    // -------------------------------------------------------------
    // FAMILY 6: SPIRAL-LIKE
    // Curvature/turn progression producing an Archimedean open spiral:
    // Outer arm -> sweeping inward -> inner core (NO rectangular outer frame!)
    // -------------------------------------------------------------
    fun generateSpiralPattern(prefix: String, offsetX: Int, offsetY: Int, random: Random): List<Arrow> {
        val arrows = mutableListOf<Arrow>()
        // Outer sweep top
        arrows.add(arrow("${prefix}_sp1", Direction.RIGHT,
            GridPoint(offsetX, offsetY),
            GridPoint(offsetX + 6, offsetY)
        ))
        // Outer sweep right
        arrows.add(arrow("${prefix}_sp2", Direction.DOWN,
            GridPoint(offsetX + 7, offsetY + 1),
            GridPoint(offsetX + 7, offsetY + 6)
        ))
        // Outer sweep bottom
        arrows.add(arrow("${prefix}_sp3", Direction.LEFT,
            GridPoint(offsetX + 6, offsetY + 7),
            GridPoint(offsetX + 1, offsetY + 7)
        ))
        // Middle sweep left
        arrows.add(arrow("${prefix}_sp4", Direction.UP,
            GridPoint(offsetX, offsetY + 6),
            GridPoint(offsetX, offsetY + 2)
        ))
        // Inward spiral arm 1
        arrows.add(arrow("${prefix}_sp5", Direction.RIGHT,
            GridPoint(offsetX + 1, offsetY + 2),
            GridPoint(offsetX + 5, offsetY + 2)
        ))
        // Inward spiral arm 2
        arrows.add(arrow("${prefix}_sp6", Direction.DOWN,
            GridPoint(offsetX + 5, offsetY + 3),
            GridPoint(offsetX + 5, offsetY + 5)
        ))
        // Inner core
        arrows.add(arrow("${prefix}_sp7", Direction.LEFT,
            GridPoint(offsetX + 4, offsetY + 5),
            GridPoint(offsetX + 2, offsetY + 5)
        ))
        // Center pin
        arrows.add(arrow("${prefix}_sp8", Direction.UP,
            GridPoint(offsetX + 2, offsetY + 4),
            GridPoint(offsetX + 2, offsetY + 3)
        ))
        return arrows
    }

    // -------------------------------------------------------------
    // FAMILY 7: PINWHEEL / ROTATIONAL
    // 4 directional arms rotating around a loose center (without crossing lines):
    //       ↑
    //       │
    // ←──── CENTER ────→
    //       │
    //       ↓
    // -------------------------------------------------------------
    fun generatePinwheelPattern(prefix: String, cx: Int, cy: Int, random: Random): List<Arrow> {
        val arrows = mutableListOf<Arrow>()
        // Top arm pointing UP
        arrows.add(arrow("${prefix}_pw_up", Direction.UP,
            GridPoint(cx, cy - 1),
            GridPoint(cx, cy - 4)
        ))
        // Right arm pointing RIGHT
        arrows.add(arrow("${prefix}_pw_right", Direction.RIGHT,
            GridPoint(cx + 1, cy),
            GridPoint(cx + 4, cy)
        ))
        // Bottom arm pointing DOWN
        arrows.add(arrow("${prefix}_pw_down", Direction.DOWN,
            GridPoint(cx, cy + 1),
            GridPoint(cx, cy + 4)
        ))
        // Left arm pointing LEFT
        arrows.add(arrow("${prefix}_pw_left", Direction.LEFT,
            GridPoint(cx - 1, cy),
            GridPoint(cx - 4, cy)
        ))

        // Interstitial angled blades creating rotational inertia
        arrows.add(arrow("${prefix}_pw_b1", Direction.RIGHT,
            GridPoint(cx - 2, cy - 2),
            GridPoint(cx - 1, cy - 2)
        ))
        arrows.add(arrow("${prefix}_pw_b2", Direction.DOWN,
            GridPoint(cx + 2, cy - 2),
            GridPoint(cx + 2, cy - 1)
        ))
        arrows.add(arrow("${prefix}_pw_b3", Direction.LEFT,
            GridPoint(cx + 2, cy + 2),
            GridPoint(cx + 1, cy + 2)
        ))
        arrows.add(arrow("${prefix}_pw_b4", Direction.UP,
            GridPoint(cx - 2, cy + 2),
            GridPoint(cx - 2, cy + 1)
        ))
        return arrows
    }

    // -------------------------------------------------------------
    // FAMILY 8: STAR / BURST
    // Several independent radial arms extending outwards:
    // -------------------------------------------------------------
    fun generateStarPattern(prefix: String, cx: Int, cy: Int, random: Random): List<Arrow> {
        val arrows = mutableListOf<Arrow>()
        // North burst with L-branch
        arrows.add(arrow("${prefix}_st_n", Direction.UP,
            GridPoint(cx - 1, cy - 2),
            GridPoint(cx - 1, cy - 4)
        ))
        // North-East burst
        arrows.add(arrow("${prefix}_st_ne", Direction.RIGHT,
            GridPoint(cx + 1, cy - 3),
            GridPoint(cx + 4, cy - 3)
        ))
        // East burst
        arrows.add(arrow("${prefix}_st_e", Direction.RIGHT,
            GridPoint(cx + 2, cy),
            GridPoint(cx + 5, cy)
        ))
        // South-East burst
        arrows.add(arrow("${prefix}_st_se", Direction.DOWN,
            GridPoint(cx + 2, cy + 1),
            GridPoint(cx + 2, cy + 4)
        ))
        // South burst
        arrows.add(arrow("${prefix}_st_s", Direction.DOWN,
            GridPoint(cx - 1, cy + 2),
            GridPoint(cx - 1, cy + 5)
        ))
        // West burst
        arrows.add(arrow("${prefix}_st_w", Direction.LEFT,
            GridPoint(cx - 2, cy),
            GridPoint(cx - 5, cy)
        ))
        // Central spark
        arrows.add(arrow("${prefix}_st_core", Direction.UP,
            GridPoint(cx, cy + 1),
            GridPoint(cx, cy - 1)
        ))
        return arrows
    }

    // -------------------------------------------------------------
    // FAMILY 9: FAN
    // Several paths spreading outward like an open folding fan:
    //     ↗
    //   →
    // →
    //   →
    //     ↘
    // -------------------------------------------------------------
    fun generateFanPattern(prefix: String, originX: Int, originY: Int, random: Random): List<Arrow> {
        val arrows = mutableListOf<Arrow>()
        // Upper ray
        arrows.add(arrow("${prefix}_fan_up", Direction.UP,
            GridPoint(originX + 2, originY - 1),
            GridPoint(originX + 2, originY - 3)
        ))
        // Diagonal upper step
        arrows.add(arrow("${prefix}_fan_hi", Direction.RIGHT,
            GridPoint(originX + 1, originY - 1),
            GridPoint(originX + 4, originY - 1)
        ))
        // Central ray
        arrows.add(arrow("${prefix}_fan_mid", Direction.RIGHT,
            GridPoint(originX, originY),
            GridPoint(originX + 5, originY)
        ))
        // Diagonal lower step
        arrows.add(arrow("${prefix}_fan_lo", Direction.RIGHT,
            GridPoint(originX + 1, originY + 1),
            GridPoint(originX + 4, originY + 1)
        ))
        // Lower ray
        arrows.add(arrow("${prefix}_fan_dn", Direction.DOWN,
            GridPoint(originX + 2, originY + 2),
            GridPoint(originX + 2, originY + 4)
        ))
        // Lateral blockers
        arrows.add(arrow("${prefix}_fan_b1", Direction.LEFT,
            GridPoint(originX + 3, originY - 2),
            GridPoint(originX, originY - 2)
        ))
        arrows.add(arrow("${prefix}_fan_b2", Direction.LEFT,
            GridPoint(originX + 3, originY + 2),
            GridPoint(originX, originY + 2)
        ))
        return arrows
    }

    // -------------------------------------------------------------
    // FAMILY 10: COMB
    // Spine with multiple uneven teeth/branches:
    // ────────────→
    //    │
    //    ├──→
    //    │
    //    ├──→
    // -------------------------------------------------------------
    fun generateCombPattern(prefix: String, offsetX: Int, offsetY: Int, random: Random): List<Arrow> {
        val arrows = mutableListOf<Arrow>()
        // Spine (vertical backbone on left)
        arrows.add(arrow("${prefix}_comb_spine", Direction.DOWN,
            GridPoint(offsetX, offsetY),
            GridPoint(offsetX, offsetY + 6)
        ))
        // Tooth 1 (long)
        arrows.add(arrow("${prefix}_comb_t1", Direction.RIGHT,
            GridPoint(offsetX + 1, offsetY),
            GridPoint(offsetX + 6, offsetY)
        ))
        // Tooth 2 (short)
        arrows.add(arrow("${prefix}_comb_t2", Direction.RIGHT,
            GridPoint(offsetX + 1, offsetY + 2),
            GridPoint(offsetX + 4, offsetY + 2)
        ))
        // Tooth 3 (medium with bend)
        arrows.add(arrow("${prefix}_comb_t3", Direction.UP,
            GridPoint(offsetX + 5, offsetY + 4),
            GridPoint(offsetX + 2, offsetY + 4),
            GridPoint(offsetX + 2, offsetY + 3)
        ))
        // Tooth 4 (bottom escape)
        arrows.add(arrow("${prefix}_comb_t4", Direction.RIGHT,
            GridPoint(offsetX + 1, offsetY + 6),
            GridPoint(offsetX + 7, offsetY + 6)
        ))
        // Inter-tooth blocker
        arrows.add(arrow("${prefix}_comb_ib", Direction.UP,
            GridPoint(offsetX + 4, offsetY + 5),
            GridPoint(offsetX + 4, offsetY + 3)
        ))
        return arrows
    }

    // -------------------------------------------------------------
    // FAMILY 11: FORK / BRANCH
    // Branching structures with divergent pathways:
    // -------------------------------------------------------------
    fun generateForkPattern(prefix: String, offsetX: Int, offsetY: Int, random: Random): List<Arrow> {
        val arrows = mutableListOf<Arrow>()
        // Trunk
        arrows.add(arrow("${prefix}_fk_trunk", Direction.RIGHT,
            GridPoint(offsetX, offsetY + 3),
            GridPoint(offsetX + 2, offsetY + 3)
        ))
        // Upper Prong
        arrows.add(arrow("${prefix}_fk_u_bend", Direction.UP,
            GridPoint(offsetX + 3, offsetY + 3),
            GridPoint(offsetX + 3, offsetY + 1)
        ))
        arrows.add(arrow("${prefix}_fk_u_out", Direction.RIGHT,
            GridPoint(offsetX + 4, offsetY + 1),
            GridPoint(offsetX + 7, offsetY + 1)
        ))
        // Center Prong
        arrows.add(arrow("${prefix}_fk_c_out", Direction.RIGHT,
            GridPoint(offsetX + 3, offsetY + 3),
            GridPoint(offsetX + 7, offsetY + 3)
        ))
        // Lower Prong
        arrows.add(arrow("${prefix}_fk_l_bend", Direction.DOWN,
            GridPoint(offsetX + 3, offsetY + 4),
            GridPoint(offsetX + 3, offsetY + 5)
        ))
        arrows.add(arrow("${prefix}_fk_l_out", Direction.RIGHT,
            GridPoint(offsetX + 4, offsetY + 5),
            GridPoint(offsetX + 6, offsetY + 5)
        ))
        // Prong cross-blocker
        arrows.add(arrow("${prefix}_fk_cross", Direction.DOWN,
            GridPoint(offsetX + 5, offsetY),
            GridPoint(offsetX + 5, offsetY + 2)
        ))
        return arrows
    }

    // -------------------------------------------------------------
    // FAMILY 12: TREE / HIERARCHICAL
    // Multi-tiered branching tree structure:
    // Main trunk -> primary limbs -> secondary twigs (no rectangular frame!)
    // -------------------------------------------------------------
    fun generateTreePattern(prefix: String, offsetX: Int, offsetY: Int, random: Random): List<Arrow> {
        val arrows = mutableListOf<Arrow>()
        // Main Trunk
        arrows.add(arrow("${prefix}_tr_trunk", Direction.UP,
            GridPoint(offsetX + 3, offsetY + 6),
            GridPoint(offsetX + 3, offsetY + 4)
        ))
        // Left Branch
        arrows.add(arrow("${prefix}_tr_lb", Direction.LEFT,
            GridPoint(offsetX + 2, offsetY + 3),
            GridPoint(offsetX + 1, offsetY + 3)
        ))
        arrows.add(arrow("${prefix}_tr_ltwig", Direction.UP,
            GridPoint(offsetX, offsetY + 2),
            GridPoint(offsetX, offsetY)
        ))
        // Right Branch
        arrows.add(arrow("${prefix}_tr_rb", Direction.RIGHT,
            GridPoint(offsetX + 4, offsetY + 3),
            GridPoint(offsetX + 5, offsetY + 3)
        ))
        arrows.add(arrow("${prefix}_tr_rtwig", Direction.UP,
            GridPoint(offsetX + 6, offsetY + 2),
            GridPoint(offsetX + 6, offsetY)
        ))
        // Crown Top
        arrows.add(arrow("${prefix}_tr_crown", Direction.UP,
            GridPoint(offsetX + 3, offsetY + 2),
            GridPoint(offsetX + 3, offsetY)
        ))
        // Canopy cross-branches
        arrows.add(arrow("${prefix}_tr_cb1", Direction.LEFT,
            GridPoint(offsetX + 5, offsetY + 1),
            GridPoint(offsetX + 4, offsetY + 1)
        ))
        arrows.add(arrow("${prefix}_tr_cb2", Direction.RIGHT,
            GridPoint(offsetX, offsetY + 5),
            GridPoint(offsetX + 2, offsetY + 5)
        ))
        return arrows
    }

    // -------------------------------------------------------------
    // FAMILY 13: ABSTRACT LETTER SILHOUETTES (E, F, H, K, T, Z)
    // Non-box letter-inspired geometric layouts
    // -------------------------------------------------------------
    fun generateLetterSilhouettePattern(prefix: String, offsetX: Int, offsetY: Int, random: Random): List<Arrow> {
        val arrows = mutableListOf<Arrow>()
        val variant = random.nextInt(3)
        when (variant) {
            0 -> {
                // T-Silhouette: broad crossbar with descending pillar and side supports
                arrows.add(arrow("${prefix}_t_bar_l", Direction.LEFT,
                    GridPoint(offsetX + 2, offsetY),
                    GridPoint(offsetX, offsetY)
                ))
                arrows.add(arrow("${prefix}_t_bar_r", Direction.RIGHT,
                    GridPoint(offsetX + 4, offsetY),
                    GridPoint(offsetX + 6, offsetY)
                ))
                arrows.add(arrow("${prefix}_t_stem", Direction.DOWN,
                    GridPoint(offsetX + 3, offsetY + 1),
                    GridPoint(offsetX + 3, offsetY + 6)
                ))
                arrows.add(arrow("${prefix}_t_sup1", Direction.UP,
                    GridPoint(offsetX + 1, offsetY + 3),
                    GridPoint(offsetX + 1, offsetY + 1)
                ))
                arrows.add(arrow("${prefix}_t_sup2", Direction.UP,
                    GridPoint(offsetX + 5, offsetY + 3),
                    GridPoint(offsetX + 5, offsetY + 1)
                ))
                arrows.add(arrow("${prefix}_t_bot_l", Direction.LEFT,
                    GridPoint(offsetX + 2, offsetY + 5),
                    GridPoint(offsetX, offsetY + 5)
                ))
                arrows.add(arrow("${prefix}_t_bot_r", Direction.RIGHT,
                    GridPoint(offsetX + 4, offsetY + 5),
                    GridPoint(offsetX + 6, offsetY + 5)
                ))
            }
            1 -> {
                // H-Silhouette: two upright pillars linked by transverse span
                arrows.add(arrow("${prefix}_h_l", Direction.UP,
                    GridPoint(offsetX + 1, offsetY + 6),
                    GridPoint(offsetX + 1, offsetY)
                ))
                arrows.add(arrow("${prefix}_h_r", Direction.DOWN,
                    GridPoint(offsetX + 5, offsetY),
                    GridPoint(offsetX + 5, offsetY + 6)
                ))
                arrows.add(arrow("${prefix}_h_cross", Direction.RIGHT,
                    GridPoint(offsetX + 2, offsetY + 3),
                    GridPoint(offsetX + 4, offsetY + 3)
                ))
                arrows.add(arrow("${prefix}_h_flank1", Direction.LEFT,
                    GridPoint(offsetX + 3, offsetY + 1),
                    GridPoint(offsetX + 2, offsetY + 1)
                ))
                arrows.add(arrow("${prefix}_h_flank2", Direction.RIGHT,
                    GridPoint(offsetX + 3, offsetY + 5),
                    GridPoint(offsetX + 4, offsetY + 5)
                ))
            }
            else -> {
                // Z-Silhouette: staggered horizontal shelves linked by diagonal step
                arrows.add(arrow("${prefix}_z_top", Direction.RIGHT,
                    GridPoint(offsetX, offsetY),
                    GridPoint(offsetX + 5, offsetY)
                ))
                arrows.add(arrow("${prefix}_z_diag_step1", Direction.DOWN,
                    GridPoint(offsetX + 4, offsetY + 1),
                    GridPoint(offsetX + 4, offsetY + 2)
                ))
                arrows.add(arrow("${prefix}_z_mid", Direction.LEFT,
                    GridPoint(offsetX + 3, offsetY + 3),
                    GridPoint(offsetX + 2, offsetY + 3)
                ))
                arrows.add(arrow("${prefix}_z_diag_step2", Direction.DOWN,
                    GridPoint(offsetX + 1, offsetY + 4),
                    GridPoint(offsetX + 1, offsetY + 5)
                ))
                arrows.add(arrow("${prefix}_z_bot", Direction.RIGHT,
                    GridPoint(offsetX, offsetY + 6),
                    GridPoint(offsetX + 6, offsetY + 6)
                ))
            }
        }
        return arrows
    }

    // -------------------------------------------------------------
    // FAMILY 14: BROKEN GRID
    // Partial, discontinuous grid arrangement with deliberate gaps
    // (NO enclosing outer boundary!)
    // -------------------------------------------------------------
    fun generateBrokenGridPattern(prefix: String, offsetX: Int, offsetY: Int, random: Random): List<Arrow> {
        val arrows = mutableListOf<Arrow>()
        // Broken row 0
        arrows.add(arrow("${prefix}_bg_r0_a", Direction.RIGHT,
            GridPoint(offsetX, offsetY),
            GridPoint(offsetX + 3, offsetY)
        ))
        arrows.add(arrow("${prefix}_bg_r0_b", Direction.LEFT,
            GridPoint(offsetX + 6, offsetY),
            GridPoint(offsetX + 5, offsetY)
        ))
        // Column drop in gap
        arrows.add(arrow("${prefix}_bg_c1", Direction.DOWN,
            GridPoint(offsetX + 4, offsetY + 1),
            GridPoint(offsetX + 4, offsetY + 3)
        ))
        // Broken row 1
        arrows.add(arrow("${prefix}_bg_r1", Direction.RIGHT,
            GridPoint(offsetX + 1, offsetY + 2),
            GridPoint(offsetX + 3, offsetY + 2)
        ))
        // Broken column on left
        arrows.add(arrow("${prefix}_bg_c0", Direction.UP,
            GridPoint(offsetX + 1, offsetY + 5),
            GridPoint(offsetX + 1, offsetY + 4)
        ))
        // Broken row 2
        arrows.add(arrow("${prefix}_bg_r2", Direction.LEFT,
            GridPoint(offsetX + 6, offsetY + 4),
            GridPoint(offsetX + 3, offsetY + 4)
        ))
        // Broken row 3
        arrows.add(arrow("${prefix}_bg_r3", Direction.RIGHT,
            GridPoint(offsetX + 2, offsetY + 6),
            GridPoint(offsetX + 5, offsetY + 6)
        ))
        return arrows
    }

    // -------------------------------------------------------------
    // FAMILY 15: OFFSET PARALLELS
    // Multiple parallel paths with irregular length, vertical offsets and bends
    // -------------------------------------------------------------
    fun generateOffsetParallelPattern(prefix: String, offsetX: Int, offsetY: Int, random: Random): List<Arrow> {
        val arrows = mutableListOf<Arrow>()
        // Row 0: offset left
        arrows.add(arrow("${prefix}_op1", Direction.RIGHT,
            GridPoint(offsetX, offsetY),
            GridPoint(offsetX + 4, offsetY)
        ))
        // Row 1: offset right, pointing left
        arrows.add(arrow("${prefix}_op2", Direction.LEFT,
            GridPoint(offsetX + 6, offsetY + 1),
            GridPoint(offsetX + 2, offsetY + 1)
        ))
        // Row 2: centered with bend
        arrows.add(arrow("${prefix}_op3", Direction.RIGHT,
            GridPoint(offsetX + 1, offsetY + 3),
            GridPoint(offsetX + 5, offsetY + 3)
        ))
        // Row 3: offset far right
        arrows.add(arrow("${prefix}_op4", Direction.LEFT,
            GridPoint(offsetX + 7, offsetY + 4),
            GridPoint(offsetX + 3, offsetY + 4)
        ))
        // Row 4: bottom shelf
        arrows.add(arrow("${prefix}_op5", Direction.RIGHT,
            GridPoint(offsetX, offsetY + 6),
            GridPoint(offsetX + 5, offsetY + 6)
        ))
        // Interlacing vertical interlocks
        arrows.add(arrow("${prefix}_op_lock1", Direction.DOWN,
            GridPoint(offsetX + 5, offsetY + 1),
            GridPoint(offsetX + 5, offsetY + 2)
        ))
        arrows.add(arrow("${prefix}_op_lock2", Direction.UP,
            GridPoint(offsetX + 2, offsetY + 5),
            GridPoint(offsetX + 2, offsetY + 4)
        ))
        return arrows
    }

    // -------------------------------------------------------------
    // FAMILY 16: DIAGONAL FLOW
    // Orthogonal segments arranged so overall composition appears diagonal:
    // →────
    //     ↓
    //     └──→
    //        ↓
    //        └────→
    // -------------------------------------------------------------
    fun generateDiagonalFlowPattern(prefix: String, offsetX: Int, offsetY: Int, random: Random): List<Arrow> {
        val arrows = mutableListOf<Arrow>()
        // Top step
        arrows.add(arrow("${prefix}_df1", Direction.RIGHT,
            GridPoint(offsetX, offsetY),
            GridPoint(offsetX + 2, offsetY),
            GridPoint(offsetX + 2, offsetY + 1)
        ))
        // Mid-upper step
        arrows.add(arrow("${prefix}_df2", Direction.DOWN,
            GridPoint(offsetX + 3, offsetY + 1),
            GridPoint(offsetX + 3, offsetY + 3)
        ))
        arrows.add(arrow("${prefix}_df3", Direction.RIGHT,
            GridPoint(offsetX + 2, offsetY + 3),
            GridPoint(offsetX + 4, offsetY + 3),
            GridPoint(offsetX + 4, offsetY + 4)
        ))
        // Mid-lower step
        arrows.add(arrow("${prefix}_df4", Direction.DOWN,
            GridPoint(offsetX + 5, offsetY + 3),
            GridPoint(offsetX + 5, offsetY + 5)
        ))
        // Bottom step
        arrows.add(arrow("${prefix}_df5", Direction.RIGHT,
            GridPoint(offsetX + 4, offsetY + 5),
            GridPoint(offsetX + 7, offsetY + 5)
        ))
        // Counter-diagonal flow accent
        arrows.add(arrow("${prefix}_df_c1", Direction.LEFT,
            GridPoint(offsetX + 6, offsetY + 2),
            GridPoint(offsetX + 4, offsetY + 2)
        ))
        arrows.add(arrow("${prefix}_df_c2", Direction.UP,
            GridPoint(offsetX + 1, offsetY + 4),
            GridPoint(offsetX + 1, offsetY + 2)
        ))
        return arrows
    }

    // -------------------------------------------------------------
    // FAMILY 17: TUNNEL / CORRIDOR
    // Narrow escape corridors with offset openings (open ends, NO closed box!)
    // -------------------------------------------------------------
    fun generateTunnelPattern(prefix: String, offsetX: Int, offsetY: Int, random: Random): List<Arrow> {
        val arrows = mutableListOf<Arrow>()
        // Wall Left Top
        arrows.add(arrow("${prefix}_tun_wlt", Direction.UP,
            GridPoint(offsetX + 1, offsetY + 3),
            GridPoint(offsetX + 1, offsetY)
        ))
        // Wall Right Top
        arrows.add(arrow("${prefix}_tun_wrt", Direction.UP,
            GridPoint(offsetX + 4, offsetY + 3),
            GridPoint(offsetX + 4, offsetY)
        ))
        // Central Runner
        arrows.add(arrow("${prefix}_tun_core", Direction.DOWN,
            GridPoint(offsetX + 2, offsetY),
            GridPoint(offsetX + 2, offsetY + 4)
        ))
        // Escape Bend from corridor
        arrows.add(arrow("${prefix}_tun_bend", Direction.RIGHT,
            GridPoint(offsetX + 3, offsetY + 4),
            GridPoint(offsetX + 6, offsetY + 4)
        ))
        // Wall Left Bottom
        arrows.add(arrow("${prefix}_tun_wlb", Direction.DOWN,
            GridPoint(offsetX + 1, offsetY + 4),
            GridPoint(offsetX + 1, offsetY + 6)
        ))
        // Wall Right Bottom
        arrows.add(arrow("${prefix}_tun_wrb", Direction.RIGHT,
            GridPoint(offsetX + 3, offsetY + 6),
            GridPoint(offsetX + 6, offsetY + 6)
        ))
        // Choke-point gatekeeper
        arrows.add(arrow("${prefix}_tun_choke", Direction.LEFT,
            GridPoint(offsetX + 7, offsetY + 2),
            GridPoint(offsetX + 5, offsetY + 2)
        ))
        return arrows
    }

    // -------------------------------------------------------------
    // FAMILY 18: SWITCHBACK
    // Repeated directional reversals with asymmetric lengths
    // -------------------------------------------------------------
    fun generateSwitchbackPattern(prefix: String, offsetX: Int, offsetY: Int, random: Random): List<Arrow> {
        val arrows = mutableListOf<Arrow>()
        // Shelf 1 (short)
        arrows.add(arrow("${prefix}_sw1", Direction.RIGHT,
            GridPoint(offsetX, offsetY),
            GridPoint(offsetX + 3, offsetY)
        ))
        // Reversal 1
        arrows.add(arrow("${prefix}_sw2", Direction.LEFT,
            GridPoint(offsetX + 6, offsetY + 1),
            GridPoint(offsetX + 1, offsetY + 1)
        ))
        // Shelf 2 (long)
        arrows.add(arrow("${prefix}_sw3", Direction.RIGHT,
            GridPoint(offsetX, offsetY + 3),
            GridPoint(offsetX + 7, offsetY + 3)
        ))
        // Reversal 2 (medium)
        arrows.add(arrow("${prefix}_sw4", Direction.LEFT,
            GridPoint(offsetX + 5, offsetY + 4),
            GridPoint(offsetX + 2, offsetY + 4)
        ))
        // Shelf 3
        arrows.add(arrow("${prefix}_sw5", Direction.RIGHT,
            GridPoint(offsetX + 1, offsetY + 6),
            GridPoint(offsetX + 6, offsetY + 6)
        ))
        // Switchback pins
        arrows.add(arrow("${prefix}_sw_pin1", Direction.DOWN,
            GridPoint(offsetX + 4, offsetY),
            GridPoint(offsetX + 4, offsetY + 2)
        ))
        arrows.add(arrow("${prefix}_sw_pin2", Direction.UP,
            GridPoint(offsetX + 2, offsetY + 5),
            GridPoint(offsetX + 2, offsetY + 4)
        ))
        return arrows
    }

    // -------------------------------------------------------------
    // FAMILY 19: CLUSTER / MULTI-REGION
    // Separate local modular structures forming one cohesive puzzle
    // (NO connecting perimeter frame!)
    // -------------------------------------------------------------
    fun generateClusterPattern(prefix: String, offsetX: Int, offsetY: Int, random: Random): List<Arrow> {
        val arrows = mutableListOf<Arrow>()
        // Cluster A (Top-Left small zigzag)
        arrows.add(arrow("${prefix}_cl_a1", Direction.RIGHT,
            GridPoint(offsetX, offsetY),
            GridPoint(offsetX + 3, offsetY)
        ))
        arrows.add(arrow("${prefix}_cl_a2", Direction.DOWN,
            GridPoint(offsetX + 2, offsetY + 1),
            GridPoint(offsetX + 2, offsetY + 2)
        ))
        arrows.add(arrow("${prefix}_cl_a3", Direction.LEFT,
            GridPoint(offsetX + 3, offsetY + 3),
            GridPoint(offsetX, offsetY + 3)
        ))

        // Cluster B (Bottom-Right fan/stair)
        arrows.add(arrow("${prefix}_cl_b1", Direction.UP,
            GridPoint(offsetX + 5, offsetY + 5),
            GridPoint(offsetX + 5, offsetY + 3)
        ))
        arrows.add(arrow("${prefix}_cl_b2", Direction.RIGHT,
            GridPoint(offsetX + 4, offsetY + 6),
            GridPoint(offsetX + 7, offsetY + 6)
        ))
        arrows.add(arrow("${prefix}_cl_b3", Direction.DOWN,
            GridPoint(offsetX + 7, offsetY + 3),
            GridPoint(offsetX + 7, offsetY + 5)
        ))

        // Inter-cluster bridge arrow providing puzzle dependency
        arrows.add(arrow("${prefix}_cl_bridge", Direction.RIGHT,
            GridPoint(offsetX + 1, offsetY + 4),
            GridPoint(offsetX + 4, offsetY + 4)
        ))
        return arrows
    }

    // -------------------------------------------------------------
    // FAMILY 20: ASYMMETRIC ORGANIC
    // Deliberately irregular, asymmetrical layout:
    // No symmetry, no outer frame, completely unique silhouette.
    // -------------------------------------------------------------
    fun generateAsymmetricPattern(prefix: String, offsetX: Int, offsetY: Int, random: Random): List<Arrow> {
        val arrows = mutableListOf<Arrow>()
        arrows.add(arrow("${prefix}_as1", Direction.RIGHT,
            GridPoint(offsetX, offsetY),
            GridPoint(offsetX + 3, offsetY),
            GridPoint(offsetX + 3, offsetY + 1)
        ))
        arrows.add(arrow("${prefix}_as2", Direction.LEFT,
            GridPoint(offsetX + 2, offsetY + 2),
            GridPoint(offsetX, offsetY + 2),
            GridPoint(offsetX, offsetY + 3)
        ))
        arrows.add(arrow("${prefix}_as3", Direction.RIGHT,
            GridPoint(offsetX + 1, offsetY + 4),
            GridPoint(offsetX + 5, offsetY + 4)
        ))
        arrows.add(arrow("${prefix}_as4", Direction.UP,
            GridPoint(offsetX + 4, offsetY + 3),
            GridPoint(offsetX + 4, offsetY + 1)
        ))
        arrows.add(arrow("${prefix}_as5", Direction.DOWN,
            GridPoint(offsetX + 6, offsetY),
            GridPoint(offsetX + 6, offsetY + 3)
        ))
        arrows.add(arrow("${prefix}_as6", Direction.UP,
            GridPoint(offsetX + 2, offsetY + 6),
            GridPoint(offsetX + 2, offsetY + 5)
        ))
        arrows.add(arrow("${prefix}_as7", Direction.RIGHT,
            GridPoint(offsetX + 3, offsetY + 6),
            GridPoint(offsetX + 6, offsetY + 6)
        ))
        return arrows
    }
}
