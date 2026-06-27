package com.nthg.matrixcamera.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.nthg.matrixcamera.face.FaceData

/**
 * Overlays face scanning animation on top of the matrix canvas.
 *
 * Shows:
 * - Animated corner brackets around the detected face bounding box
 * - Pulsing dot at face center
 * - Horizontal scan line sweeping across the face box
 * - Subtle corner tick marks
 */
@Composable
fun FaceScanOverlay(
    faceData: FaceData,
    modifier: Modifier = Modifier
) {
    val hasFace = faceData.boundingBox != null

    val transition = rememberInfiniteTransition(label = "face_scan")

    val scanY by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanY"
    )

    val pulse by transition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val cornerAlpha by transition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cornerAlpha"
    )

    if (!hasFace) return

    Canvas(modifier = modifier.fillMaxSize()) {
        val box = faceData.boundingBox ?: return@Canvas
        val left = box.left * size.width
        val top = box.top * size.height
        val right = box.right * size.width
        val bottom = box.bottom * size.height
        val boxW = right - left
        val boxH = bottom - top

        // Add padding
        val pad = boxW * 0.12f
        val fl = left - pad
        val ft = top - pad
        val fr = right + pad
        val fb = bottom + pad
        val bw = fr - fl
        val bh = fb - ft

        drawFaceBox(fl, ft, fr, fb, bw, bh, cornerAlpha, scanY, pulse)
    }
}

private fun DrawScope.drawFaceBox(
    fl: Float, ft: Float, fr: Float, fb: Float,
    bw: Float, bh: Float,
    cornerAlpha: Float, scanY: Float, pulse: Float
) {
    val cornerLen = bw * 0.15f
    val strokeW = 2f
    val color = Color.White.copy(alpha = cornerAlpha)

    // Corner brackets — top-left
    drawLine(color, Offset(fl, ft), Offset(fl + cornerLen, ft), strokeW, StrokeCap.Round)
    drawLine(color, Offset(fl, ft), Offset(fl, ft + cornerLen), strokeW, StrokeCap.Round)
    // top-right
    drawLine(color, Offset(fr, ft), Offset(fr - cornerLen, ft), strokeW, StrokeCap.Round)
    drawLine(color, Offset(fr, ft), Offset(fr, ft + cornerLen), strokeW, StrokeCap.Round)
    // bottom-left
    drawLine(color, Offset(fl, fb), Offset(fl + cornerLen, fb), strokeW, StrokeCap.Round)
    drawLine(color, Offset(fl, fb), Offset(fl, fb - cornerLen), strokeW, StrokeCap.Round)
    // bottom-right
    drawLine(color, Offset(fr, fb), Offset(fr - cornerLen, fb), strokeW, StrokeCap.Round)
    drawLine(color, Offset(fr, fb), Offset(fr, fb - cornerLen), strokeW, StrokeCap.Round)

    // Scan line across face box
    val sy = ft + scanY * bh
    drawLine(
        Color.White.copy(alpha = 0.25f),
        Offset(fl, sy),
        Offset(fr, sy),
        strokeWidth = 1.5f
    )

    // Face center dot — pulse
    val cx = (fl + fr) / 2f
    val cy = (ft + fb) / 2f
    drawCircle(Color.White.copy(alpha = 0.3f * pulse), radius = 12f * pulse, center = Offset(cx, cy))
    drawCircle(Color.White.copy(alpha = 0.8f), radius = 4f, center = Offset(cx, cy))
}
