package com.nthg.matrixcamera.processor

import android.graphics.*

class PixelMosaicProcessor(
    private val outputSize: Int = 480,
    private val cellSize: Int = 9, // Q2: Reduced from 13 for more detail
) {
    private val gridCount = outputSize / cellSize
    private val centerX = gridCount / 2f
    private val centerY = gridCount / 2f

    // Q5: Expanded oval radii to include more face detail (forehead/chin)
    private val radiusX = gridCount / 2f - 0.8f
    private val radiusY = gridCount / 2f + 0.8f

    // Q4: Lower temporal blend to reduce ghosting trails
    private val temporalBlend = 0.15f

    // L2: Pre-allocated buffers to eliminate per-frame GC pressure
    private val rawLuminances = FloatArray(gridCount * gridCount)
    private val smoothedLuminances = FloatArray(gridCount * gridCount)
    private var previousLuminances: FloatArray? = null
    private val pixelsBuffer = IntArray(outputSize * outputSize)
    private val sourcePixels = IntArray(640 * 480) // Buffer for Q8 resolution

    fun processFrame(sourceBitmap: Bitmap): Bitmap {
        sampleLuminancesOptimized(sourceBitmap)
        
        applyTemporalSmoothingOptimized()
        
        // Q7: Contrast stretch to pop detail in varying light
        applyContrastStretch(smoothedLuminances)
        
        previousLuminances = smoothedLuminances.copyOf()

        return renderMosaicBitmapOptimized()
    }

    /**
     * L3: Optimized sampling using getPixels and manual stride.
     * Avoids Bitmap.createScaledBitmap allocations.
     */
    private fun sampleLuminancesOptimized(bitmap: Bitmap) {
        val w = bitmap.width
        val h = bitmap.height
        
        // Re-allocate if source resolution changed
        val pixels = if (w * h <= sourcePixels.size) sourcePixels else IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)

        val stepX = w.toFloat() / gridCount
        val stepY = h.toFloat() / gridCount

        for (row in 0 until gridCount) {
            val sourceY = (row * stepY).toInt().coerceIn(0, h - 1)
            for (col in 0 until gridCount) {
                val sourceX = (col * stepX).toInt().coerceIn(0, w - 1)
                val pixel = pixels[sourceY * w + sourceX]
                rawLuminances[row * gridCount + col] = pixelLuminance(pixel)
            }
        }
    }

    private fun applyTemporalSmoothingOptimized() {
        val prev = previousLuminances
        if (prev == null) {
            rawLuminances.copyInto(smoothedLuminances)
            return
        }
        for (i in rawLuminances.indices) {
            smoothedLuminances[i] = rawLuminances[i] * (1f - temporalBlend) + prev[i] * temporalBlend
        }
    }

    /**
     * Q7: Remaps luminance to full 0-255 range.
     */
    private fun applyContrastStretch(luminances: FloatArray) {
        var min = 255f
        var max = 0f
        for (lum in luminances) {
            if (lum < min) min = lum
            if (lum > max) max = lum
        }

        // Skip if contrast is too low (flat scene)
        if (max - min < 30f) return

        val range = max - min
        for (i in luminances.indices) {
            luminances[i] = ((luminances[i] - min) / range * 255f).coerceIn(0f, 255f)
        }
    }

    /**
     * L4: Batch draw calls by writing to a pixels array then using setPixels().
     * Replaces ~2000+ Canvas.drawRect calls with 1 JNI call.
     */
    private fun renderMosaicBitmapOptimized(): Bitmap {
        // Q3: ARGB_8888 for higher quality (no banding)
        val output = Bitmap.createBitmap(outputSize, outputSize, Bitmap.Config.ARGB_8888)
        
        // Background color
        pixelsBuffer.fill(Color.BLACK)

        val gap = 2.2f
        val drawSize = (cellSize - gap).toInt()
        val halfGap = (gap / 2f).toInt()
        val lumThreshold = 6f // Q6: Lowered to recover shadow detail

        for (row in 0 until gridCount) {
            for (col in 0 until gridCount) {
                // Oval clip
                val nx = (col - centerX) / radiusX
                val ny = (row - centerY) / radiusY
                if (nx * nx + ny * ny > 1f) continue

                val lum = smoothedLuminances[row * gridCount + col]
                if (lum < lumThreshold) continue

                val gray = lum.toInt().coerceIn(0, 255)
                val color = Color.rgb(gray, gray, gray)

                // Fill block in pixel array
                val startX = col * cellSize + halfGap
                val startY = row * cellSize + halfGap
                
                for (dy in 0 until drawSize) {
                    val y = startY + dy
                    if (y >= outputSize) break
                    for (dx in 0 until drawSize) {
                        val x = startX + dx
                        if (x >= outputSize) break
                        pixelsBuffer[y * outputSize + x] = color
                    }
                }
            }
        }

        output.setPixels(pixelsBuffer, 0, outputSize, 0, 0, outputSize, outputSize)
        return output
    }

    private fun pixelLuminance(pixel: Int): Float {
        // Bit-shift extraction is faster than Color.red/green/blue
        val r = (pixel shr 16) and 0xff
        val g = (pixel shr 8) and 0xff
        val b = pixel and 0xff
        return 0.299f * r + 0.587f * g + 0.114f * b
    }
}
