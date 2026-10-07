package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val ArrowColorScheme = lightColorScheme(
    primary = ArrowNavy,
    onPrimary = ButtonPrimaryText,
    secondary = ArrowBlockedPink,
    onSecondary = ButtonPrimaryText,
    tertiary = ArrowHintCyan,
    background = ArrowBackground,
    onBackground = TextPrimary,
    surface = SurfaceCard,
    onSurface = TextPrimary,
    surfaceVariant = ArrowBackgroundAlt,
    onSurfaceVariant = TextSecondary,
    outline = HeartInactive
)

@Composable
fun ArrowsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ArrowColorScheme,
        typography = Typography,
        content = content
    )
}

// Keep backward compatibility for existing tests
@Composable
fun MyApplicationTheme(content: @Composable () -> Unit) {
    ArrowsTheme(content = content)
}
