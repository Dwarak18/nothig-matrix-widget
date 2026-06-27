package com.nthg.matrixcamera.face

import android.graphics.Bitmap
import android.graphics.RectF
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Wraps ML Kit FaceDetector with a coroutine-friendly API.
 *
 * Detects the largest visible face and returns [FaceData].
 * Tracks continuously with landmark and classification enabled.
 * Smooth tracking is handled at the ViewModel level via
 * exponential moving average on bounding box coordinates.
 */
class FaceDetector {

    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)  // skip landmarks for perf
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
        .setMinFaceSize(0.10f) // detect faces >= 10% of frame width
        .enableTracking()
        .build()

    private val detector = FaceDetection.getClient(options)

    /**
     * Detect the largest face in [bitmap].
     *
     * @param bitmap The camera frame to analyze
     * @param imageWidth Width of the original camera frame
     * @param imageHeight Height of the original camera frame
     * @return [FaceData] with detected face info, or [FaceData.NONE] if no face found
     */
    suspend fun detectFace(
        bitmap: Bitmap,
        imageWidth: Int,
        imageHeight: Int
    ): FaceData = suspendCancellableCoroutine { cont ->
        val image = InputImage.fromBitmap(bitmap, 0)
        detector.process(image)
            .addOnSuccessListener { faces ->
                if (!cont.isActive) return@addOnSuccessListener
                val face = faces.maxByOrNull { it.boundingBox.width() * it.boundingBox.height() }
                cont.resume(face?.toFaceData(imageWidth, imageHeight) ?: FaceData.NONE)
            }
            .addOnFailureListener {
                if (cont.isActive) cont.resume(FaceData.NONE)
            }
    }

    private fun Face.toFaceData(imgW: Int, imgH: Int): FaceData {
        val box = boundingBox
        val normalizedBox = RectF(
            box.left.toFloat() / imgW,
            box.top.toFloat() / imgH,
            box.right.toFloat() / imgW,
            box.bottom.toFloat() / imgH
        )
        return FaceData(
            boundingBox = normalizedBox,
            smilingProbability = smilingProbability,
            leftEyeOpenProbability = leftEyeOpenProbability,
            rightEyeOpenProbability = rightEyeOpenProbability,
            headEulerAngleY = headEulerAngleY,
            headEulerAngleZ = headEulerAngleZ,
            headEulerAngleX = headEulerAngleX,
            normalizedCenterX = (box.left + box.width() / 2f) / imgW,
            normalizedCenterY = (box.top + box.height() / 2f) / imgH,
            normalizedWidth = box.width().toFloat() / imgW
        )
    }

    /**
     * Release ML Kit detector resources.
     */
    fun close() {
        detector.close()
    }
}
