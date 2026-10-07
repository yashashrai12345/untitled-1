package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.HeartActive
import com.example.ui.theme.HeartInactive

/**
 * Centered hearts/lives indicator conforming to the minimalist game style.
 */
@Composable
fun HeartsIndicator(
    hearts: Int,
    maxHearts: Int = 3,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.testTag("hearts_indicator"),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..maxHearts) {
            val isActive = i <= hearts
            val scale by animateFloatAsState(
                targetValue = if (isActive) 1f else 0.82f,
                animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
                label = "heartScale_$i"
            )
            val heartColor by animateColorAsState(
                targetValue = if (isActive) HeartActive else HeartInactive,
                label = "heartColor_$i"
            )

            HeartIcon(
                color = heartColor,
                isFilled = isActive,
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
            )
        }
    }
}

@Composable
private fun HeartIcon(
    color: Color,
    isFilled: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Symmetrical procedural vector heart path
        val path = Path().apply {
            moveTo(width / 2f, height * 0.85f)
            // Left curve
            cubicTo(
                width * 0.05f, height * 0.55f,
                width * 0.02f, height * 0.18f,
                width * 0.28f, height * 0.18f
            )
            cubicTo(
                width * 0.42f, height * 0.18f,
                width / 2f, height * 0.32f,
                width / 2f, height * 0.38f
            )
            // Right curve
            cubicTo(
                width / 2f, height * 0.32f,
                width * 0.58f, height * 0.18f,
                width * 0.72f, height * 0.18f
            )
            cubicTo(
                width * 0.98f, height * 0.18f,
                width * 0.95f, height * 0.55f,
                width / 2f, height * 0.85f
            )
            close()
        }

        if (isFilled) {
            drawPath(path = path, color = color, style = Fill)
        } else {
            drawPath(path = path, color = color, style = Stroke(width = 2.5f))
        }
    }
}
