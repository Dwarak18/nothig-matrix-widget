package com.nthg.matrixcamera.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope

/**
 * Renders the processed matrix [Bitmap] onto a full-screen Compose Canvas.
 * Uses hardware-accelerated bitmap drawing.
 */
@Composable
fun MatrixCanvas(
    bitmap: Bitmap?,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        bitmap?.let { bmp ->
            if (!bmp.isRecycled) {
                drawImage(image = bmp.asImageBitmap())
            }
        }
    }
}
