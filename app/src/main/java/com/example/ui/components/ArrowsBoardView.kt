package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.animation.EscapeTrack
import com.example.game.animation.SnakePathCalculator
import com.example.game.animation.SnakeSnapshot
import com.example.game.collision.CollisionDetector
import com.example.game.model.Arrow
import com.example.game.model.Direction
import com.example.game.model.GameState
import com.example.game.model.GameStatus
import com.example.ui.theme.ArrowBackgroundAlt
import com.example.ui.theme.ArrowBlockedPink
import com.example.ui.theme.ArrowHintCyan
import com.example.ui.theme.ArrowNavy
import com.example.ui.theme.BoardGridDot
import com.example.ui.theme.PillBackground
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Custom Canvas rendering the interactive puzzle board and arrows.
 * Supports fluid snake-like path-following escape animations,
 * dynamic wrong-move collision feedback with smooth reversal,
 * full multi-touch / button screen zoom (in/out) and pan, and
 * persistent grid anchor dots drawn underneath arrows so they
 * never appear on top of arrows but also never disappear when arrows move.
 */
@Composable
fun ArrowsBoardView(
    gameState: GameState,
    onArrowTapped: (String) -> Unit,
    onEscapeFinished: (String) -> Unit = {},
    onWrongMoveImpact: (String) -> Unit = {},
    onWrongMoveFinished: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Pulse animation for hints
    val infiniteTransition = rememberInfiniteTransition(label = "hintPulse")
    val hintPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hintAlpha"
    )

    // Screen Zoom & Pan state
    val coroutineScope = rememberCoroutineScope()
    val zoomScale = remember { Animatable(1.0f) }
    val panOffsetX = remember { Animatable(0.0f) }
    val panOffsetY = remember { Animatable(0.0f) }

    // Reset zoom and pan on new level or level restart
    LaunchedEffect(gameState.level.id, gameState.sessionId) {
        zoomScale.snapTo(1.0f)
        panOffsetX.snapTo(0.0f)
        panOffsetY.snapTo(0.0f)
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val board = gameState.level.board

        // Measure layout dimensions
        val availableWidth = constraints.maxWidth.toFloat()
        val availableHeight = constraints.maxHeight.toFloat()

        val paddingUnits = 1.6f
        val cellCountX = board.width - 1 + paddingUnits
        val cellCountY = board.height - 1 + paddingUnits

        val cellSize = minOf(
            availableWidth / cellCountX,
            availableHeight / cellCountY
        ).coerceAtLeast(30f)

        val gridPixelWidth = (board.width - 1) * cellSize
        val gridPixelHeight = (board.height - 1) * cellSize

        val originX = (availableWidth - gridPixelWidth) / 2f
        val originY = (availableHeight - gridPixelHeight) / 2f

        val pivotX = availableWidth / 2f
        val pivotY = availableHeight / 2f

        // Pan bounds calculation to keep board constrained in comfortable viewing range
        val calculateMaxPan: (Float) -> Offset = { scale ->
            if (scale <= 1.01f) {
                Offset.Zero
            } else {
                val maxPanX = maxOf(
                    (gridPixelWidth * scale - availableWidth) / 2f + cellSize * scale * 1.5f,
                    (scale - 1f) * availableWidth * 0.45f
                )
                val maxPanY = maxOf(
                    (gridPixelHeight * scale - availableHeight) / 2f + cellSize * scale * 1.5f,
                    (scale - 1f) * availableHeight * 0.45f
                )
                Offset(maxPanX.coerceAtLeast(0f), maxPanY.coerceAtLeast(0f))
            }
        }

        val onZoomIn: () -> Unit = {
            coroutineScope.launch {
                val targetScale = (zoomScale.value + 0.5f).coerceAtMost(3.5f)
                zoomScale.animateTo(targetScale, tween(250, easing = FastOutSlowInEasing))
            }
        }

        val onZoomOut: () -> Unit = {
            coroutineScope.launch {
                val targetScale = (zoomScale.value - 0.5f).coerceAtLeast(1.0f)
                if (targetScale <= 1.01f) {
                    launch { panOffsetX.animateTo(0f, tween(250, easing = FastOutSlowInEasing)) }
                    launch { panOffsetY.animateTo(0f, tween(250, easing = FastOutSlowInEasing)) }
                } else {
                    val maxP = calculateMaxPan(targetScale)
                    val clampedX = panOffsetX.value.coerceIn(-maxP.x, maxP.x)
                    val clampedY = panOffsetY.value.coerceIn(-maxP.y, maxP.y)
                    launch { panOffsetX.animateTo(clampedX, tween(250, easing = FastOutSlowInEasing)) }
                    launch { panOffsetY.animateTo(clampedY, tween(250, easing = FastOutSlowInEasing)) }
                }
                zoomScale.animateTo(targetScale, tween(250, easing = FastOutSlowInEasing))
            }
        }

        val onResetZoom: () -> Unit = {
            coroutineScope.launch {
                launch { panOffsetX.animateTo(0f, tween(250, easing = FastOutSlowInEasing)) }
                launch { panOffsetY.animateTo(0f, tween(250, easing = FastOutSlowInEasing)) }
                zoomScale.animateTo(1.0f, tween(250, easing = FastOutSlowInEasing))
            }
        }

        // Successful snake escape animation state
        val escaping = gameState.escapingArrow
        val escapeTrack = remember(escaping?.arrow?.id) {
            escaping?.let {
                SnakePathCalculator.buildEscapeTrack(it.arrow, board)
            }
        }
        val distanceTraveledState = remember(escaping?.arrow?.id) { mutableFloatStateOf(0f) }

        // Continuous frame-by-frame snake escape animation loop (60 FPS / vsync driven)
        val currentEscaping = escaping
        LaunchedEffect(currentEscaping?.arrow?.id) {
            if (currentEscaping == null) return@LaunchedEffect
            val track = escapeTrack ?: return@LaunchedEffect
            var lastFrameTimeNanos = 0L
            var distanceTraveled = 0f
            val totalDistance = track.totalEscapeDistance

            val successfulEscapeSpeedPx = 950f
            val baseSpeedInCells = (successfulEscapeSpeedPx / cellSize).coerceIn(13.0f, 17.5f)

            while (distanceTraveled < totalDistance) {
                withFrameNanos { frameTimeNanos ->
                    val dt = if (lastFrameTimeNanos != 0L) {
                        ((frameTimeNanos - lastFrameTimeNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                    } else {
                        0.016f
                    }
                    val p = (distanceTraveled / totalDistance).coerceIn(0f, 1f)

                    val startFactor = if (p < 0.15f) 0.90f + 0.10f * (p / 0.15f) else 1.0f
                    val exitFactor = if (p > 0.80f) 1.0f + 0.15f * ((p - 0.80f) / 0.20f) else 1.0f
                    val speed = baseSpeedInCells * startFactor * exitFactor

                    distanceTraveled += speed * dt
                    distanceTraveledState.floatValue = minOf(distanceTraveled, totalDistance)
                    lastFrameTimeNanos = frameTimeNanos
                }
            }

            onEscapeFinished(currentEscaping.arrow.id)
        }

        // Wrong Move snake animation state
        val wrongMove = gameState.wrongMoveArrow
        val wrongMoveTrack = remember(wrongMove?.arrow?.id) {
            wrongMove?.let {
                SnakePathCalculator.buildEscapeTrack(it.arrow, board)
            }
        }
        val wrongDistanceState = remember(wrongMove?.arrow?.id) { mutableFloatStateOf(0f) }
        val wrongColorState = remember(wrongMove?.arrow?.id) { mutableStateOf(ArrowNavy) }
        val wrongShakeOffsetState = remember(wrongMove?.arrow?.id) { mutableStateOf(Offset.Zero) }

        LaunchedEffect(wrongMove?.arrow?.id) {
            val wm = wrongMove ?: return@LaunchedEffect
            val track = wrongMoveTrack ?: return@LaunchedEffect
            val targetDistance = wm.collisionDistance

            val forwardSpeedInCells = (780f / cellSize).coerceIn(10.5f, 14.5f)
            val returnSpeedInCells = (700f / cellSize).coerceIn(9.5f, 13.5f)

            var distance = 0f
            var lastFrameTimeNanos = 0L

            // PHASE 1: Forward movement toward blocker as a snake
            wrongColorState.value = ArrowNavy
            while (distance < targetDistance) {
                withFrameNanos { frameTimeNanos ->
                    val dt = if (lastFrameTimeNanos != 0L) {
                        ((frameTimeNanos - lastFrameTimeNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                    } else {
                        0.016f
                    }
                    distance += forwardSpeedInCells * dt
                    wrongDistanceState.floatValue = minOf(distance, targetDistance)
                    lastFrameTimeNanos = frameTimeNanos
                }
            }

            // PHASE 2: Impact moment
            wrongDistanceState.floatValue = targetDistance
            wrongColorState.value = ArrowBlockedPink
            onWrongMoveImpact(wm.arrow.id)

            val impactDurationSec = 0.13f
            var impactElapsed = 0f
            lastFrameTimeNanos = 0L
            while (impactElapsed < impactDurationSec) {
                withFrameNanos { frameTimeNanos ->
                    if (lastFrameTimeNanos != 0L) {
                        val dt = ((frameTimeNanos - lastFrameTimeNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                        impactElapsed += dt
                        val progress = (impactElapsed / impactDurationSec).coerceIn(0f, 1f)
                        val wobble = (sin(progress * PI * 4.0) * (1.0 - progress) * (cellSize * 0.08f)).toFloat()
                        val shake = when (wm.arrow.direction) {
                            Direction.UP -> Offset(0f, wobble)
                            Direction.DOWN -> Offset(0f, -wobble)
                            Direction.LEFT -> Offset(wobble, 0f)
                            Direction.RIGHT -> Offset(-wobble, 0f)
                        }
                        wrongShakeOffsetState.value = shake
                    }
                    lastFrameTimeNanos = frameTimeNanos
                }
            }
            wrongShakeOffsetState.value = Offset.Zero

            // PHASE 3: Reverse return movement
            lastFrameTimeNanos = 0L
            while (distance > 0f) {
                withFrameNanos { frameTimeNanos ->
                    val dt = if (lastFrameTimeNanos != 0L) {
                        ((frameTimeNanos - lastFrameTimeNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                    } else {
                        0.016f
                    }
                    distance -= returnSpeedInCells * dt
                    wrongDistanceState.floatValue = maxOf(distance, 0f)
                    lastFrameTimeNanos = frameTimeNanos
                }
            }

            // PHASE 4: Fully returned to origin
            wrongDistanceState.floatValue = 0f
            wrongColorState.value = ArrowNavy
            onWrongMoveFinished(wm.arrow.id)
        }

        val currentGameState by rememberUpdatedState(gameState)
        val currentOnArrowTapped by rememberUpdatedState(onArrowTapped)

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("arrows_board_canvas")
                .pointerInput(originX, originY, cellSize, availableWidth, availableHeight) {
                    val touchSlop = viewConfiguration.touchSlop
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val downPos = down.position

                        var isDrag = false
                        var isMultiTouch = false
                        var prevCentroid = downPos
                        var prevSpan = 0f

                        do {
                            val event = awaitPointerEvent()
                            val activePointers = event.changes.filter { it.pressed }
                            if (activePointers.isEmpty()) break

                            if (activePointers.size >= 2) {
                                isMultiTouch = true
                                isDrag = true

                                val p0 = activePointers[0].position
                                val p1 = activePointers[1].position
                                val currentSpan = (p0 - p1).getDistance()
                                val currentCentroid = (p0 + p1) / 2f

                                if (prevSpan > 0f) {
                                    val zoomFactor = currentSpan / prevSpan
                                    val newScale = (zoomScale.value * zoomFactor).coerceIn(1.0f, 3.5f)
                                    coroutineScope.launch {
                                        zoomScale.snapTo(newScale)
                                    }

                                    val panDelta = currentCentroid - prevCentroid
                                    val maxP = calculateMaxPan(newScale)
                                    val newPanX = if (newScale <= 1.01f) 0f else (panOffsetX.value + panDelta.x).coerceIn(-maxP.x, maxP.x)
                                    val newPanY = if (newScale <= 1.01f) 0f else (panOffsetY.value + panDelta.y).coerceIn(-maxP.y, maxP.y)
                                    coroutineScope.launch {
                                        panOffsetX.snapTo(newPanX)
                                        panOffsetY.snapTo(newPanY)
                                    }
                                }

                                prevSpan = currentSpan
                                prevCentroid = currentCentroid
                                activePointers.forEach { it.consume() }
                            } else if (activePointers.size == 1) {
                                val pointer = activePointers.first()
                                if (isMultiTouch) {
                                    prevCentroid = pointer.position
                                    prevSpan = 0f
                                } else {
                                    val moveDist = (pointer.position - downPos).getDistance()
                                    if (moveDist > touchSlop) {
                                        isDrag = true
                                    }
                                    if (isDrag && zoomScale.value > 1.05f) {
                                        val panDelta = pointer.position - pointer.previousPosition
                                        val s = zoomScale.value
                                        val maxP = calculateMaxPan(s)
                                        val newPanX = (panOffsetX.value + panDelta.x).coerceIn(-maxP.x, maxP.x)
                                        val newPanY = (panOffsetY.value + panDelta.y).coerceIn(-maxP.y, maxP.y)
                                        coroutineScope.launch {
                                            panOffsetX.snapTo(newPanX)
                                            panOffsetY.snapTo(newPanY)
                                        }
                                        pointer.consume()
                                    }
                                }
                            }
                        } while (event.changes.any { it.pressed })

                        // Single tap detected on finger release
                        if (!isDrag && !isMultiTouch) {
                            val state = currentGameState
                            if (state.status == GameStatus.PLAYING &&
                                state.escapingArrow == null &&
                                state.wrongMoveArrow == null &&
                                state.blockedArrowId == null
                            ) {
                                val s = zoomScale.value
                                val px = panOffsetX.value
                                val py = panOffsetY.value

                                // Inverse transform screen tap to unscaled board space
                                val worldTapX = pivotX + (downPos.x - px - pivotX) / s
                                val worldTapY = pivotY + (downPos.y - py - pivotY) / s

                                val tapped = CollisionDetector.findTappedArrow(
                                    tapX = worldTapX,
                                    tapY = worldTapY,
                                    activeArrows = state.activeArrows,
                                    boardOriginX = originX,
                                    boardOriginY = originY,
                                    cellSize = cellSize,
                                    touchTolerancePx = cellSize * 0.48f
                                )
                                if (tapped != null) {
                                    down.consume()
                                    currentOnArrowTapped(tapped.id)
                                }
                            }
                        }
                    }
                }
        ) {
            val scaleVal = zoomScale.value
            val panX = panOffsetX.value
            val panY = panOffsetY.value
            val pivot = Offset(pivotX, pivotY)

            withTransform({
                translate(left = panX, top = panY)
                scale(scaleX = scaleVal, scaleY = scaleVal, pivot = pivot)
            }) {
                val strokeWidth = (cellSize * 0.18f).coerceIn(13f, 24f)

                // 1. Draw persistent grid anchor dots across entire board FIRST (underneath).
                // Dots stay fixed to grid intersections independent of arrow positions,
                // so they never disappear when arrows move/escape, and never render on top of arrows.
                for (gx in 0 until board.width) {
                    for (gy in 0 until board.height) {
                        val px = originX + gx * cellSize
                        val py = originY + gy * cellSize
                        drawCircle(
                            color = BoardGridDot,
                            radius = 3.6f,
                            center = Offset(px, py)
                        )
                    }
                }

                // 2. Draw active stationary arrows (cover dots beneath them)
                for (arrow in gameState.activeArrows) {
                    if (gameState.escapingArrow?.arrow?.id == arrow.id) continue
                    if (gameState.wrongMoveArrow?.arrow?.id == arrow.id) continue

                    val isHinted = arrow.id == gameState.hintArrowId
                    val arrowColor = if (isHinted) ArrowHintCyan.copy(alpha = hintPulseAlpha) else ArrowNavy

                    drawArrowOnCanvas(
                        arrow = arrow,
                        originX = originX,
                        originY = originY,
                        cellSize = cellSize,
                        strokeWidth = strokeWidth,
                        color = arrowColor,
                        alpha = 1f,
                        drawGlow = isHinted
                    )
                }

                // 3. Draw escaping arrow with smooth flowing snake-like path animation
                if (escaping != null && escapeTrack != null) {
                    val distance = distanceTraveledState.floatValue
                    val snapshot = escapeTrack.sampleSnake(distance)

                    drawSnakeOnCanvas(
                        snapshot = snapshot,
                        originX = originX,
                        originY = originY,
                        cellSize = cellSize,
                        strokeWidth = strokeWidth,
                        color = ArrowNavy
                    )
                }

                // 4. Draw wrong-moving arrow with forward snake travel, impact shake, and smooth reversal
                if (wrongMove != null && wrongMoveTrack != null) {
                    val distance = wrongDistanceState.floatValue
                    val snapshot = wrongMoveTrack.sampleSnake(distance)
                    val shake = wrongShakeOffsetState.value

                    drawSnakeOnCanvas(
                        snapshot = snapshot,
                        originX = originX + shake.x,
                        originY = originY + shake.y,
                        cellSize = cellSize,
                        strokeWidth = strokeWidth,
                        color = wrongColorState.value
                    )
                }
            }
        }

        // 5. Level 1 Instruction Hand: ONLY shown at Level 1, for ONLY ONE escapable arrow
        val showInstructionHand = gameState.level.id == 1 &&
                gameState.clearedArrowsCount == 0 &&
                gameState.escapingArrow == null &&
                gameState.status == GameStatus.PLAYING

        val instructionArrow = remember(gameState.activeArrows, showInstructionHand) {
            if (showInstructionHand) {
                gameState.activeArrows.firstOrNull { arrow ->
                    CollisionDetector.canArrowEscape(arrow, gameState.activeArrows, gameState.level.board)
                }
            } else null
        }

        if (instructionArrow != null) {
            val head = instructionArrow.head
            val targetPx = originX + head.x * cellSize
            val targetPy = originY + head.y * cellSize

            val scaleVal = zoomScale.value
            val panX = panOffsetX.value
            val panY = panOffsetY.value

            val screenTargetX = pivotX + (targetPx - pivotX) * scaleVal + panX
            val screenTargetY = pivotY + (targetPy - pivotY) * scaleVal + panY
            val scaledCellSize = cellSize * scaleVal

            InstructionHandOverlay(
                targetX = screenTargetX,
                targetY = screenTargetY,
                availableWidth = availableWidth,
                availableHeight = availableHeight,
                cellSize = scaledCellSize,
                onTapped = {
                    currentOnArrowTapped(instructionArrow.id)
                }
            )
        }

        // 6. Floating On-Screen Zoom Controls (In/Out/Reset)
        FloatingZoomControls(
            zoomScale = zoomScale.value,
            onZoomIn = onZoomIn,
            onZoomOut = onZoomOut,
            onResetZoom = onResetZoom,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 8.dp, bottom = 8.dp)
        )
    }
}

/**
 * Modern floating glassmorphism control panel for Screen Zoom (In / Out / Reset).
 * Useful for players making precise moves in congested or particular areas of the puzzle frame.
 */
@Composable
private fun FloatingZoomControls(
    zoomScale: Float,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onResetZoom: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xF5FFFFFF),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = 6.dp,
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
        ) {
            // Zoom In button
            IconButton(
                onClick = onZoomIn,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("zoom_in_button"),
                enabled = zoomScale < 3.49f
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Zoom In",
                    tint = if (zoomScale < 3.49f) ArrowNavy else TextSecondary.copy(alpha = 0.35f),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Percentage indicator pill (Click to reset zoom to 100%)
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (zoomScale > 1.05f) ArrowHintCyan.copy(alpha = 0.12f) else PillBackground,
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onResetZoom
                    )
                    .testTag("zoom_reset_indicator")
            ) {
                Text(
                    text = "${(zoomScale * 100).toInt()}%",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (zoomScale > 1.05f) ArrowHintCyan else TextSecondary,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }

            // Zoom Out button
            IconButton(
                onClick = onZoomOut,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("zoom_out_button"),
                enabled = zoomScale > 1.01f
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Zoom Out",
                    tint = if (zoomScale > 1.01f) ArrowNavy else TextSecondary.copy(alpha = 0.35f),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Fit-to-screen / Reset button (shown when zoomed in)
            if (zoomScale > 1.05f) {
                IconButton(
                    onClick = onResetZoom,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("zoom_fit_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CropFree,
                        contentDescription = "Fit to Screen",
                        tint = ArrowNavy,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawArrowOnCanvas(
    arrow: Arrow,
    originX: Float,
    originY: Float,
    cellSize: Float,
    strokeWidth: Float,
    color: Color,
    alpha: Float,
    drawGlow: Boolean
) {
    if (arrow.points.isEmpty() || alpha <= 0f) return

    val drawColor = color.copy(alpha = color.alpha * alpha)

    // Optional hint glow
    if (drawGlow) {
        val glowPath = Path().apply {
            val first = arrow.points.first()
            moveTo(originX + first.x * cellSize, originY + first.y * cellSize)
            for (i in 1 until arrow.points.size) {
                val pt = arrow.points[i]
                lineTo(originX + pt.x * cellSize, originY + pt.y * cellSize)
            }
        }
        drawPath(
            path = glowPath,
            color = ArrowHintCyan.copy(alpha = 0.35f * alpha),
            style = Stroke(
                width = strokeWidth * 2.2f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }

    // Main arrow path
    val path = Path().apply {
        val first = arrow.points.first()
        moveTo(originX + first.x * cellSize, originY + first.y * cellSize)
        for (i in 1 until arrow.points.size) {
            val pt = arrow.points[i]
            lineTo(originX + pt.x * cellSize, originY + pt.y * cellSize)
        }
    }

    drawPath(
        path = path,
        color = drawColor,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    // Arrowhead geometry at points.last()
    val head = arrow.head
    val headCenterX = originX + head.x * cellSize
    val headCenterY = originY + head.y * cellSize

    drawArrowHead(
        tipCenterX = headCenterX,
        tipCenterY = headCenterY,
        direction = arrow.direction,
        strokeWidth = strokeWidth,
        color = drawColor
    )
}

private fun DrawScope.drawArrowHead(
    tipCenterX: Float,
    tipCenterY: Float,
    direction: Direction,
    strokeWidth: Float,
    color: Color
) {
    val headLength = strokeWidth * 1.7f
    val headWidth = strokeWidth * 2.1f

    // Center coordinates for tip and base
    val dx = direction.dx.toFloat()
    val dy = direction.dy.toFloat()

    // Perpendicular vector for arrowhead wings
    val perpX = -dy
    val perpY = dx

    val tipX = tipCenterX + dx * (headLength * 0.55f)
    val tipY = tipCenterY + dy * (headLength * 0.55f)

    val baseX = tipX - dx * headLength
    val baseY = tipY - dy * headLength

    val leftWingX = baseX + perpX * (headWidth / 2f)
    val leftWingY = baseY + perpY * (headWidth / 2f)

    val rightWingX = baseX - perpX * (headWidth / 2f)
    val rightWingY = baseY - perpY * (headWidth / 2f)

    val headPath = Path().apply {
        moveTo(tipX, tipY)
        lineTo(leftWingX, leftWingY)
        // Slight inner notch for an elegant modern arrowhead
        lineTo(baseX + dx * (headLength * 0.25f), baseY + dy * (headLength * 0.25f))
        lineTo(rightWingX, rightWingY)
        close()
    }

    drawPath(
        path = headPath,
        color = color,
        style = Fill
    )
}

private fun DrawScope.drawSnakeOnCanvas(
    snapshot: SnakeSnapshot,
    originX: Float,
    originY: Float,
    cellSize: Float,
    strokeWidth: Float,
    color: Color
) {
    if (snapshot.points.size < 2) return

    val path = Path().apply {
        val first = snapshot.points.first()
        moveTo(originX + first.x * cellSize, originY + first.y * cellSize)
        for (i in 1 until snapshot.points.size) {
            val pt = snapshot.points[i]
            lineTo(originX + pt.x * cellSize, originY + pt.y * cellSize)
        }
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    // Render arrowhead attached to the leading head point, rotated to tangent orientation
    val headScreenX = originX + snapshot.headPoint.x * cellSize
    val headScreenY = originY + snapshot.headPoint.y * cellSize

    drawArrowHeadAngle(
        tipCenterX = headScreenX,
        tipCenterY = headScreenY,
        angleRad = snapshot.headAngleRad,
        strokeWidth = strokeWidth,
        color = color
    )
}

private fun DrawScope.drawArrowHeadAngle(
    tipCenterX: Float,
    tipCenterY: Float,
    angleRad: Float,
    strokeWidth: Float,
    color: Color
) {
    val headLength = strokeWidth * 1.7f
    val headWidth = strokeWidth * 2.1f

    val cosA = cos(angleRad)
    val sinA = sin(angleRad)

    // Perpendicular vector for arrowhead wings
    val perpX = -sinA
    val perpY = cosA

    val tipX = tipCenterX + cosA * (headLength * 0.55f)
    val tipY = tipCenterY + sinA * (headLength * 0.55f)

    val baseX = tipX - cosA * headLength
    val baseY = tipY - sinA * headLength

    val leftWingX = baseX + perpX * (headWidth / 2f)
    val leftWingY = baseY + perpY * (headWidth / 2f)

    val rightWingX = baseX - perpX * (headWidth / 2f)
    val rightWingY = baseY - perpY * (headWidth / 2f)

    val headPath = Path().apply {
        moveTo(tipX, tipY)
        lineTo(leftWingX, leftWingY)
        lineTo(baseX + cosA * (headLength * 0.25f), baseY + sinA * (headLength * 0.25f))
        lineTo(rightWingX, rightWingY)
        close()
    }

    drawPath(
        path = headPath,
        color = color,
        style = Fill
    )
}

/**
 * Animated Instructional Hand overlay shown only at Level 1 for the single movable arrow.
 * Displays a rhythmic tap bob animation, ripple contact waves, and "Tap to move arrow" badge.
 */
@Composable
private fun InstructionHandOverlay(
    targetX: Float,
    targetY: Float,
    availableWidth: Float,
    availableHeight: Float,
    cellSize: Float,
    onTapped: () -> Unit
) {
    val density = LocalDensity.current
    val targetXDp = with(density) { targetX.toDp() }
    val targetYDp = with(density) { targetY.toDp() }
    val availableWidthDp = with(density) { availableWidth.toDp() }

    val infiniteTransition = rememberInfiniteTransition(label = "instructionHand")
    val bobProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "handBob"
    )

    val rippleFraction by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripplePulse"
    )

    val bobOffset = 10.dp * bobProgress
    val rippleRadius = (cellSize * 0.28f) + (cellSize * 0.38f) * rippleFraction
    val rippleAlpha = (1f - rippleFraction).coerceIn(0f, 0.85f)

    // 1. Glowing ripple effect at the target arrow head
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            color = ArrowHintCyan.copy(alpha = rippleAlpha),
            radius = rippleRadius,
            center = Offset(targetX, targetY),
            style = Stroke(width = 4f)
        )
        drawCircle(
            color = ArrowHintCyan.copy(alpha = rippleAlpha * 0.30f),
            radius = rippleRadius * 0.6f,
            center = Offset(targetX, targetY),
            style = Fill
        )
    }

    // 2. Position hand and badge so they don't clip outside board
    val isNearTop = targetY < availableHeight * 0.35f
    val handY = if (isNearTop) {
        targetYDp + 8.dp + bobOffset
    } else {
        targetYDp - 50.dp - bobOffset
    }
    val pillY = if (isNearTop) {
        handY + 46.dp
    } else {
        handY - 38.dp
    }
    val pillX = (targetXDp - 65.dp).coerceIn(16.dp, (availableWidthDp - 170.dp).coerceAtLeast(16.dp))

    // Interactive hand icon (vibrant warm yellow with shadow)
    Box(
        modifier = Modifier
            .offset(x = targetXDp - 18.dp, y = handY)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onTapped
            )
    ) {
        Icon(
            imageVector = Icons.Default.TouchApp,
            contentDescription = "Tap to move arrow",
            tint = Color(0xFFFACC15),
            modifier = Modifier
                .size(46.dp)
                .shadow(elevation = 10.dp, shape = CircleShape)
        )
    }

    // Floating instructional pill: "Tap to move arrow"
    Box(
        modifier = Modifier
            .offset(x = pillX, y = pillY)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onTapped
            )
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xF20F172A),
            border = BorderStroke(1.5.dp, Color(0x8038BDF8)),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tap to move arrow",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

