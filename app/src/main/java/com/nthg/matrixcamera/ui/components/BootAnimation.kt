package com.nthg.matrixcamera.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.floor
import kotlin.random.Random

/**
 * Full-screen boot / loading animation.
 *
 * Displays a Matrix-rain style cascade of dots falling down the screen,
 * combined with a horizontal scan line sweep.
 * Shown for ~800ms while the camera initializes.
 */
@Composable
fun BootAnimation(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "boot")

    val scanY by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanY"
    )

    val dotPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 300, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dotPhase"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawBootDots(dotPhase)
            drawScanLine(scanY)
        }
    }
}

private fun DrawScope.drawBootDots(phase: Float) {
    val cols = (size.width / 16f).toInt()
    val rows = (size.height / 16f).toInt()
    val cellSize = 16f
    val rng = Random(42) // fixed seed for consistent pattern

    for (col in 0 until cols) {
        for (row in 0 until rows) {
            val noise = rng.nextFloat()
            val active = ((noise + phase) % 1f) < 0.12f
            if (active) {
                val alpha = ((noise + phase) % 1f) / 0.12f
                val adjustedAlpha = (alpha * 0.6f).coerceIn(0f, 0.6f)
                drawCircle(
                    color = Color.White.copy(alpha = adjustedAlpha),
                    radius = 3f,
                    center = Offset(
                        col * cellSize + cellSize / 2f,
                        row * cellSize + cellSize / 2f
                    )
                )
            }
        }
    }
}

private fun DrawScope.drawScanLine(scanY: Float) {
    val y = scanY * size.height
    // Main bright scan line
    drawLine(
        color = Color.White.copy(alpha = 0.7f),
        start = Offset(0f, y),
        end = Offset(size.width, y),
        strokeWidth = 2f
    )
    // Soft glow above and below
    for (i in 1..8) {
        val dist = i * 4f
        val alpha = (0.3f * (1f - i / 8f))
        drawLine(
            color = Color.White.copy(alpha = alpha),
            start = Offset(0f, y - dist),
            end = Offset(size.width, y - dist),
            strokeWidth = 1f
        )
        drawLine(
            color = Color.White.copy(alpha = alpha * 0.5f),
            start = Offset(0f, y + dist),
            end = Offset(size.width, y + dist),
            strokeWidth = 1f
        )
    }
}
