package com.nthg.matrixcamera.face

import android.graphics.RectF

/**
 * Encapsulates all face detection data from ML Kit for a single frame.
 * All fields are nullable — face may not always be detected.
 */
data class FaceData(
    /** Bounding box of the face in image coordinates (0..imageWidth, 0..imageHeight) */
    val boundingBox: RectF?,

    /** Smile probability [0.0, 1.0] */
    val smilingProbability: Float?,

    /** Left eye open probability [0.0, 1.0] */
    val leftEyeOpenProbability: Float?,

    /** Right eye open probability [0.0, 1.0] */
    val rightEyeOpenProbability: Float?,

    /** Head rotation around the Y-axis (left/right) in degrees */
    val headEulerAngleY: Float,

    /** Head rotation around the Z-axis (tilt) in degrees */
    val headEulerAngleZ: Float,

    /** Head rotation around the X-axis (up/down) in degrees */
    val headEulerAngleX: Float,

    /** Normalized face center (0..1 for both dimensions) */
    val normalizedCenterX: Float,
    val normalizedCenterY: Float,

    /** Normalized face width (0..1) */
    val normalizedWidth: Float
) {
    val isSmiling: Boolean get() = (smilingProbability ?: 0f) > 0.7f
    val isLeftEyeClosed: Boolean get() = (leftEyeOpenProbability ?: 1f) < 0.3f
    val isRightEyeClosed: Boolean get() = (rightEyeOpenProbability ?: 1f) < 0.3f
    val isBothEyesClosed: Boolean get() = isLeftEyeClosed && isRightEyeClosed
    val isLookingLeft: Boolean get() = headEulerAngleY > 20f
    val isLookingRight: Boolean get() = headEulerAngleY < -20f

    companion object {
        /** Indicates no face detected in the current frame */
        val NONE = FaceData(
            boundingBox = null,
            smilingProbability = null,
            leftEyeOpenProbability = null,
            rightEyeOpenProbability = null,
            headEulerAngleY = 0f,
            headEulerAngleZ = 0f,
            headEulerAngleX = 0f,
            normalizedCenterX = 0.5f,
            normalizedCenterY = 0.5f,
            normalizedWidth = 0f
        )
    }
}
