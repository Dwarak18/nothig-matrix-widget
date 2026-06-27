package com.nthg.matrixcamera.util

import android.graphics.*
import androidx.camera.core.ImageProxy

object ImageConversionUtils {

    /**
     * Converts an RGBA_8888 ImageProxy to a Bitmap.
     * Eliminates JPEG compression round-trip.
     */
    fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
        return try {
            val plane = imageProxy.planes[0]
            val buffer = plane.buffer
            val pixelStride = plane.pixelStride
            val rowStride = plane.rowStride
            val rowPadding = rowStride - pixelStride * imageProxy.width

            // Direct buffer to bitmap copy
            val bitmap = Bitmap.createBitmap(
                imageProxy.width + rowPadding / pixelStride,
                imageProxy.height,
                Bitmap.Config.ARGB_8888
            )
            bitmap.copyPixelsFromBuffer(buffer)

            // Crop padding if necessary
            val result = if (rowPadding > 0) {
                Bitmap.createBitmap(bitmap, 0, 0, imageProxy.width, imageProxy.height).also {
                    if (it != bitmap) bitmap.recycle()
                }
            } else {
                bitmap
            }

            // Apply rotation and mirroring
            val rotation = imageProxy.imageInfo.rotationDegrees
            val rotated = rotateBitmap(result, rotation.toFloat())
            mirrorHorizontally(rotated)
        } catch (e: Exception) {
            null
        }
    }

    private fun rotateBitmap(source: Bitmap, degrees: Float): Bitmap {
        if (degrees == 0f) return source
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
            .also { if (it != source) source.recycle() }
    }

    private fun mirrorHorizontally(source: Bitmap): Bitmap {
        val matrix = Matrix().apply {
            postScale(-1f, 1f, source.width / 2f, source.height / 2f)
        }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
            .also { if (it != source) source.recycle() }
    }
}
