package com.nthg.matrixcamera.capture

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Saves the current matrix frame to the device gallery.
 * Supports PNG, transparent PNG, and JPG formats.
 * Inserts into MediaStore so the image appears in the gallery immediately.
 */
class ImageCapture(private val context: Context) {

    enum class Format { PNG, TRANSPARENT_PNG, JPG }

    /**
     * Save [bitmap] to the gallery.
     *
     * @param bitmap The matrix-rendered bitmap to save
     * @param format Output format
     * @return The saved file path, or null on failure
     */
    suspend fun saveToGallery(bitmap: Bitmap, format: Format): String? =
        withContext(Dispatchers.IO) {
            try {
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                    .format(Date())
                val prefix = "MatrixCam_$timestamp"

                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                        saveViaMediaStore(bitmap, format, prefix)
                    }
                    else -> {
                        saveLegacy(bitmap, format, prefix)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    private fun saveViaMediaStore(bitmap: Bitmap, format: Format, prefix: String): String? {
        val mimeType = when (format) {
            Format.JPG -> "image/jpeg"
            else -> "image/png"
        }
        val extension = when (format) {
            Format.JPG -> ".jpg"
            else -> ".png"
        }
        val displayName = "$prefix$extension"

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, mimeType)
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MatrixCamera")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return null

        return try {
            resolver.openOutputStream(uri)?.use { out ->
                when (format) {
                    Format.JPG -> bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                    Format.TRANSPARENT_PNG -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    Format.PNG -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
            }
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            uri.toString()
        } catch (e: Exception) {
            resolver.delete(uri, null, null)
            null
        }
    }

    @Suppress("DEPRECATION")
    private fun saveLegacy(bitmap: Bitmap, format: Format, prefix: String): String? {
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "MatrixCamera"
        )
        if (!dir.exists()) dir.mkdirs()

        val extension = if (format == Format.JPG) ".jpg" else ".png"
        val file = File(dir, "$prefix$extension")

        FileOutputStream(file).use { out ->
            when (format) {
                Format.JPG -> bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                else -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
        }
        return file.absolutePath
    }

    /**
     * Create a circular-cropped avatar bitmap from [source].
     *
     * @param source The source matrix bitmap
     * @param size Output size in pixels (square)
     * @return Circular cropped Bitmap with transparent background
     */
    fun createCircularAvatar(source: Bitmap, size: Int): Bitmap {
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(output)
        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
        val scaled = Bitmap.createScaledBitmap(source, size, size, true)
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
        paint.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(scaled, 0f, 0f, paint)
        scaled.recycle()
        return output
    }
}
