package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ProgressFill
import com.example.ui.theme.ProgressTrack

/**
 * Ultra-clean, subtle horizontal progress line below the hearts.
 * Smoothly animates strictly from the current displayed progress to the new target progress.
 * Never resets to 0 during an active attempt, never flashes, and survives all recompositions.
 */
@Composable
fun PuzzleProgressBar(
    progress: Float,
    sessionId: Long = 0L,
    modifier: Modifier = Modifier
) {
    val target = progress.coerceIn(0f, 1f)
    // Keyed on sessionId: on level restart or loading a new level, animatable resets cleanly.
    // Throughout gameplay of the same attempt, animatable retains its current value.
    val animatable = remember(sessionId) { Animatable(target) }

    LaunchedEffect(target, sessionId) {
        animatable.animateTo(
            targetValue = target,
            animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
        )
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(3.dp)
            .testTag("progress_bar")
    ) {
        val w = size.width
        val h = size.height

        // Background track
        drawRoundRect(
            color = ProgressTrack,
            topLeft = Offset.Zero,
            size = Size(w, h),
            cornerRadius = CornerRadius(h / 2f, h / 2f)
        )

        // Progress fill
        val currentFill = animatable.value.coerceIn(0f, 1f)
        if (currentFill > 0f) {
            drawRoundRect(
                color = ProgressFill,
                topLeft = Offset.Zero,
                size = Size(w * currentFill, h),
                cornerRadius = CornerRadius(h / 2f, h / 2f)
            )
        }
    }
}
