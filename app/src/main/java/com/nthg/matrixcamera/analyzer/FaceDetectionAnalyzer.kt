package com.nthg.matrixcamera.analyzer

import android.graphics.Bitmap
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.nthg.matrixcamera.processor.PixelMosaicProcessor
import com.nthg.matrixcamera.util.ImageConversionUtils
import java.util.concurrent.atomic.AtomicBoolean

class FaceDetectionAnalyzer(
    private val processor: PixelMosaicProcessor,
    private val onFrameProcessed: (Bitmap) -> Unit,
) : ImageAnalysis.Analyzer {

    // AtomicBoolean guard — drops frames while previous is still processing
    // This is the primary jitter cause if removed
    private val isProcessing = AtomicBoolean(false)

    override fun analyze(imageProxy: ImageProxy) {
        if (!isProcessing.compareAndSet(false, true)) {
            imageProxy.close()
            return
        }

        val bitmap = ImageConversionUtils.imageProxyToBitmap(imageProxy)
        imageProxy.close()

        if (bitmap == null) {
            isProcessing.set(false)
            return
        }

        // Process on current thread (already on cameraExecutor background thread)
        runCatching {
            val result = processor.processFrame(bitmap)
            bitmap.recycle()
            onFrameProcessed(result)
        }.onFailure {
            bitmap.recycle()
        }

        isProcessing.set(false)
    }
}
