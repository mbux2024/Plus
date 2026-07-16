package com.homeflix.tv.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

// Prime/Netflix-hybrid dark scheme. Color tokens live in Color.kt and the type
// scale in Type.kt. Brand red is kept as the accent (primary); the canvas and
// surfaces are the Prime blue-black. Repointing here cascades to every screen
// that reads MaterialTheme.colorScheme.*.
private val StreambertColors = darkColorScheme(
    primary = Red,
    onPrimary = Color.White,
    secondary = PrimeBlue,
    onSecondary = Color.White,
    tertiary = SurfaceVariant,
    onTertiary = TextPrimary,
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextSecondary,
    border = BadgeOutline
)

@Composable
fun StreambertTvTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = StreambertColors,
        typography = StreambertTypography,
        content = content
    )
}
