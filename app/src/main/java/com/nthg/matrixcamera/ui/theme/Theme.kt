package com.nthg.matrixcamera.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val MatrixColorScheme = darkColorScheme(
    primary = White,
    onPrimary = Black,
    primaryContainer = VerySubtleWhite,
    onPrimaryContainer = White,
    secondary = OffWhite,
    onSecondary = Black,
    secondaryContainer = VerySubtleWhite,
    onSecondaryContainer = OffWhite,
    tertiary = DimWhite,
    onTertiary = Black,
    background = Black,
    onBackground = White,
    surface = Black,
    onSurface = White,
    surfaceVariant = VerySubtleWhite,
    onSurfaceVariant = OffWhite,
    outline = SubtleWhite,
    outlineVariant = VerySubtleWhite,
    scrim = Black,
    error = White,
    onError = Black,
)

@Composable
fun MatrixCameraTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MatrixColorScheme,
        typography = MatrixTypography,
        shapes = MatrixShapes,
        content = content
    )
}
