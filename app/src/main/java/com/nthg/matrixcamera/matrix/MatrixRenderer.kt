package com.nthg.matrixcamera.matrix

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import androidx.annotation.WorkerThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

/**
 * Core matrix rendering pipeline.
 *
 * Pipeline:
 *   Camera Frame → Grayscale → Contrast Enhancement → Edge Detection
 *   → Threshold → Downscale → Style-specific dot/char render
 *
 * All heavy operations run on Dispatchers.Default to keep the main thread free.
 * Bitmap objects are reused via [outputBitmap] to minimize GC pressure.
 */
class MatrixRenderer {

    // Dot/cell configuration
    var dotGridSize: Int = 12        // pixel size of each matrix cell
    var brightness: Float = 1.0f     // 0.5 to 2.0
    var mirrorHorizontal: Boolean = true // front camera mirror

    // Reusable output bitmap (avoids allocations on each frame)
    private var outputBitmap: Bitmap? = null

    // Paint objects (reused)
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }
    private val bgPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        typeface = Typeface.MONOSPACE
    }
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    // ASCII brightness map (darkest → brightest)
    private val asciiChars = " .:-=+*#%@".toCharArray()

    // Game Boy palette
    private val gameBoyPalette = intArrayOf(
        Color.rgb(0x0F, 0x38, 0x0F), // darkest
        Color.rgb(0x30, 0x62, 0x30), // dark
        Color.rgb(0x8B, 0xAC, 0x0F), // light
        Color.rgb(0x9B, 0xBC, 0x0F)  // lightest
    )

    /**
     * Process a camera frame bitmap into a matrix-style rendering.
     * This is a suspend function and runs on [Dispatchers.Default].
     *
     * @param frame Raw camera frame (any size)
     * @param style The current [MatrixStyle] to render with
     * @param screenWidth Output width in pixels
     * @param screenHeight Output height in pixels
     * @return A new [Bitmap] with the matrix rendering, or null on error
     */
    @WorkerThread
    suspend fun process(
        frame: Bitmap,
        style: MatrixStyle,
        screenWidth: Int,
        screenHeight: Int
    ): Bitmap? = withContext(Dispatchers.Default) {
        try {
            // Step 1: Scale frame to fill screen, apply mirror
            val scaled = scaleAndMirror(frame, screenWidth, screenHeight)

            // Step 2: Downscale to dot grid resolution
            val cols = screenWidth / dotGridSize
            val rows = screenHeight / dotGridSize
            val small = Bitmap.createScaledBitmap(scaled, cols, rows, false)
            scaled.recycle()

            // Step 3: Prepare output bitmap (reuse if same size)
            val output = getOrCreateOutput(screenWidth, screenHeight)
            val canvas = Canvas(output)
            canvas.drawRect(0f, 0f, screenWidth.toFloat(), screenHeight.toFloat(), bgPaint)

            // Step 4: Style-specific rendering
            when (style) {
                MatrixStyle.NOTHING_MATRIX -> renderNothingMatrix(canvas, small, cols, rows)
                MatrixStyle.LED_MATRIX     -> renderLedMatrix(canvas, small, cols, rows)
                MatrixStyle.ASCII_TERMINAL -> renderAscii(canvas, small, cols, rows)
                MatrixStyle.DOT_GLYPH      -> renderDotGlyph(canvas, small, cols, rows)
                MatrixStyle.RETRO_LCD      -> renderRetroLcd(canvas, small, cols, rows)
                MatrixStyle.PIXEL_GAMEBOY  -> renderGameBoy(canvas, small, cols, rows)
            }

            small.recycle()
            output

        } catch (e: Exception) {
            null
        }
    }

    // ------------------------------------------------------------------
    // Step 1: Scale + mirror
    // ------------------------------------------------------------------

    private fun scaleAndMirror(src: Bitmap, w: Int, h: Int): Bitmap {
        val matrix = android.graphics.Matrix()
        val scaleX = w.toFloat() / src.width
        val scaleY = h.toFloat() / src.height
        val scale = maxOf(scaleX, scaleY)
        matrix.setScale(if (mirrorHorizontal) -scale else scale, scale)
        matrix.postTranslate(if (mirrorHorizontal) w.toFloat() else 0f, 0f)
        return Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, false)
    }

    // ------------------------------------------------------------------
    // Output bitmap pool (single slot)
    // ------------------------------------------------------------------

    private fun getOrCreateOutput(w: Int, h: Int): Bitmap {
        val existing = outputBitmap
        if (existing != null && existing.width == w && existing.height == h && !existing.isRecycled) {
            return existing
        }
        existing?.recycle()
        return Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).also { outputBitmap = it }
    }

    // ------------------------------------------------------------------
    // Brightness helpers
    // ------------------------------------------------------------------

    private fun getBrightness(pixel: Int): Float {
        val r = Color.red(pixel) / 255f
        val g = Color.green(pixel) / 255f
        val b = Color.blue(pixel) / 255f
        // Luminance formula (perceptual)
        return (0.2126f * r + 0.7152f * g + 0.0722f * b) * brightness
    }

    // ------------------------------------------------------------------
    // Style: Nothing Matrix — white circular dots
    // ------------------------------------------------------------------

    private fun renderNothingMatrix(canvas: Canvas, small: Bitmap, cols: Int, rows: Int) {
        val cellSize = dotGridSize.toFloat()
        val maxRadius = cellSize * 0.45f
        val minRadius = cellSize * 0.05f

        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val pixel = small.getPixel(col, row)
                val lum = getBrightness(pixel).coerceIn(0f, 1f)
                val radius = minRadius + lum * (maxRadius - minRadius)
                val cx = col * cellSize + cellSize / 2f
                val cy = row * cellSize + cellSize / 2f
                val alpha = (lum * 255).toInt().coerceIn(20, 255)
                dotPaint.alpha = alpha
                dotPaint.color = Color.WHITE
                canvas.drawCircle(cx, cy, radius, dotPaint)
            }
        }
    }

    // ------------------------------------------------------------------
    // Style: LED Matrix — circular dots with radial glow
    // ------------------------------------------------------------------

    private fun renderLedMatrix(canvas: Canvas, small: Bitmap, cols: Int, rows: Int) {
        val cellSize = dotGridSize.toFloat()
        val dotRadius = cellSize * 0.3f

        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val pixel = small.getPixel(col, row)
                val lum = getBrightness(pixel).coerceIn(0f, 1f)
                val cx = col * cellSize + cellSize / 2f
                val cy = row * cellSize + cellSize / 2f

                if (lum > 0.1f) {
                    // Glow halo
                    val glowRadius = dotRadius * 2f * lum
                    val glowAlpha = (lum * 80).toInt()
                    glowPaint.shader = RadialGradient(
                        cx, cy, glowRadius,
                        Color.argb(glowAlpha, 255, 255, 255),
                        Color.TRANSPARENT,
                        Shader.TileMode.CLAMP
                    )
                    canvas.drawCircle(cx, cy, glowRadius, glowPaint)
                }

                // Core LED dot
                val brightness = (30 + lum * 225).toInt().coerceIn(0, 255)
                dotPaint.color = Color.rgb(brightness, brightness, brightness)
                dotPaint.alpha = 255
                canvas.drawCircle(cx, cy, dotRadius * lum.coerceAtLeast(0.2f), dotPaint)
            }
        }
    }

    // ------------------------------------------------------------------
    // Style: ASCII Terminal — characters mapped to brightness
    // ------------------------------------------------------------------

    private fun renderAscii(canvas: Canvas, small: Bitmap, cols: Int, rows: Int) {
        val cellSize = dotGridSize.toFloat()
        textPaint.textSize = cellSize * 0.85f

        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val pixel = small.getPixel(col, row)
                val lum = getBrightness(pixel).coerceIn(0f, 1f)
                val charIndex = ((1f - lum) * (asciiChars.size - 1)).roundToInt()
                val char = asciiChars[charIndex.coerceIn(0, asciiChars.size - 1)].toString()

                val alpha = (lum * 255).toInt().coerceIn(40, 255)
                textPaint.alpha = alpha
                textPaint.color = Color.WHITE

                val x = col * cellSize + cellSize * 0.1f
                val y = row * cellSize + cellSize * 0.85f
                canvas.drawText(char, x, y, textPaint)
            }
        }
    }

    // ------------------------------------------------------------------
    // Style: Dot Glyph — Nothing Glyph-inspired segmented dots
    // ------------------------------------------------------------------

    private fun renderDotGlyph(canvas: Canvas, small: Bitmap, cols: Int, rows: Int) {
        val cellSize = dotGridSize.toFloat()
        val dotR = cellSize * 0.2f
        val subOffsets = floatArrayOf(-0.25f, 0.25f)

        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val pixel = small.getPixel(col, row)
                val lum = getBrightness(pixel).coerceIn(0f, 1f)
                val baseX = col * cellSize + cellSize / 2f
                val baseY = row * cellSize + cellSize / 2f

                // Draw 2x2 sub-dots per cell to create glyph-like clusters
                for (dy in subOffsets) {
                    for (dx in subOffsets) {
                        val subLum = (lum + (Math.random() * 0.1 - 0.05)).coerceIn(0.0, 1.0).toFloat()
                        if (subLum > 0.15f) {
                            val alpha = (subLum * 255).toInt()
                            dotPaint.alpha = alpha
                            dotPaint.color = Color.WHITE
                            canvas.drawCircle(
                                baseX + dx * cellSize,
                                baseY + dy * cellSize,
                                dotR * subLum,
                                dotPaint
                            )
                        }
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // Style: Retro LCD — rectangular pixel segments
    // ------------------------------------------------------------------

    private fun renderRetroLcd(canvas: Canvas, small: Bitmap, cols: Int, rows: Int) {
        val cellSize = dotGridSize.toFloat()
        val segW = cellSize * 0.7f
        val segH = cellSize * 0.7f
        val gap = cellSize * 0.15f
        val rect = RectF()

        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val pixel = small.getPixel(col, row)
                val lum = getBrightness(pixel).coerceIn(0f, 1f)
                val left = col * cellSize + gap
                val top = row * cellSize + gap
                rect.set(left, top, left + segW, top + segH)

                val grayVal = (lum * 255).toInt()
                val color = when {
                    grayVal > 192 -> Color.WHITE
                    grayVal > 128 -> Color.rgb(170, 170, 170)
                    grayVal > 64  -> Color.rgb(85, 85, 85)
                    grayVal > 20  -> Color.rgb(26, 26, 26)
                    else -> Color.TRANSPARENT
                }

                if (color != Color.TRANSPARENT) {
                    dotPaint.color = color
                    dotPaint.alpha = 255
                    canvas.drawRoundRect(rect, 2f, 2f, dotPaint)
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // Style: Pixel Game Boy — 4-color green palette
    // ------------------------------------------------------------------

    private fun renderGameBoy(canvas: Canvas, small: Bitmap, cols: Int, rows: Int) {
        val cellSize = dotGridSize.toFloat()
        val rect = RectF()

        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val pixel = small.getPixel(col, row)
                val lum = getBrightness(pixel).coerceIn(0f, 1f)
                val paletteIndex = ((1f - lum) * 3).roundToInt().coerceIn(0, 3)
                dotPaint.color = gameBoyPalette[paletteIndex]
                dotPaint.alpha = 255
                rect.set(
                    col * cellSize, row * cellSize,
                    col * cellSize + cellSize, row * cellSize + cellSize
                )
                canvas.drawRect(rect, dotPaint)
            }
        }
    }

    /**
     * Clean up resources when no longer needed.
     */
    fun release() {
        outputBitmap?.recycle()
        outputBitmap = null
    }
}
