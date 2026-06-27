package com.nthg.matrixcamera.ui.camera

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nthg.matrixcamera.capture.ImageCapture
import com.nthg.matrixcamera.face.FaceData
import com.nthg.matrixcamera.face.FaceDetector
import com.nthg.matrixcamera.matrix.MatrixRenderer
import com.nthg.matrixcamera.matrix.MatrixStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for the camera screen. Manages:
 * - Camera frame → matrix rendering pipeline
 * - Face detection results
 * - UI state (style, brightness, mirror, etc.)
 * - Capture and save
 */
class CameraViewModel(application: Application) : AndroidViewModel(application) {

    data class UiState(
        val currentStyle: MatrixStyle = MatrixStyle.NOTHING_MATRIX,
        val matrixBitmap: Bitmap? = null,
        val faceData: FaceData = FaceData.NONE,
        val brightness: Float = 1.0f,
        val isMirrored: Boolean = true,
        val showStyleSwitcher: Boolean = false,
        val showBrightnessSlider: Boolean = false,
        val isCaptureFlash: Boolean = false,
        val lastSavedPath: String? = null,
        val screenWidth: Int = 0,
        val screenHeight: Int = 0,
        val isBooting: Boolean = true,
        val faceDetectionEnabled: Boolean = true,
        val dotSize: Int = 12
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val renderer = MatrixRenderer()
    private val faceDetector = FaceDetector()
    private val imageCapture = ImageCapture(application)

    // Throttle face detection to every 3rd frame for performance
    private var frameCount = 0
    private var processingJob: Job? = null

    // Boot animation delay
    init {
        viewModelScope.launch {
            delay(800)
            _uiState.update { it.copy(isBooting = false) }
        }
    }

    /**
     * Process a camera frame. Called from the camera analysis thread.
     */
    fun processFrame(frame: Bitmap, frameWidth: Int, frameHeight: Int) {
        // Cancel the previous frame's processing if still running (backpressure)
        processingJob?.cancel()
        processingJob = viewModelScope.launch(Dispatchers.Default) {
            val state = _uiState.value
            if (state.screenWidth == 0 || state.screenHeight == 0) {
                frame.recycle()
                return@launch
            }

            // Update renderer settings
            renderer.brightness = state.brightness
            renderer.mirrorHorizontal = state.isMirrored
            renderer.dotGridSize = state.dotSize

            // Run matrix rendering
            val matrixBitmap = renderer.process(
                frame,
                state.currentStyle,
                state.screenWidth,
                state.screenHeight
            )

            // Run face detection on every 3rd frame
            val faceData = if (state.faceDetectionEnabled && frameCount % 3 == 0) {
                faceDetector.detectFace(frame, frameWidth, frameHeight)
            } else {
                state.faceData
            }
            frameCount++
            frame.recycle()

            withContext(Dispatchers.Main.immediate) {
                _uiState.update { current ->
                    current.copy(
                        matrixBitmap = matrixBitmap ?: current.matrixBitmap,
                        faceData = smoothFaceData(current.faceData, faceData)
                    )
                }
            }
        }
    }

    /** Exponential moving average on face bounding box for smooth tracking */
    private fun smoothFaceData(prev: FaceData, next: FaceData): FaceData {
        if (next.boundingBox == null) return if (prev.boundingBox != null) prev else FaceData.NONE
        if (prev.boundingBox == null) return next
        val alpha = 0.4f // smoothing factor
        return next.copy(
            normalizedCenterX = lerp(prev.normalizedCenterX, next.normalizedCenterX, alpha),
            normalizedCenterY = lerp(prev.normalizedCenterY, next.normalizedCenterY, alpha),
            normalizedWidth = lerp(prev.normalizedWidth, next.normalizedWidth, alpha)
        )
    }

    private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t

    fun setScreenSize(w: Int, h: Int) {
        if (_uiState.value.screenWidth != w || _uiState.value.screenHeight != h) {
            _uiState.update { it.copy(screenWidth = w, screenHeight = h) }
        }
    }

    fun setStyle(style: MatrixStyle) {
        _uiState.update { it.copy(currentStyle = style, showStyleSwitcher = false) }
    }

    fun setBrightness(brightness: Float) {
        _uiState.update { it.copy(brightness = brightness) }
    }

    fun toggleMirror() {
        _uiState.update { it.copy(isMirrored = !it.isMirrored) }
    }

    fun toggleStyleSwitcher() {
        _uiState.update { it.copy(showStyleSwitcher = !it.showStyleSwitcher, showBrightnessSlider = false) }
    }

    fun toggleBrightnessSlider() {
        _uiState.update { it.copy(showBrightnessSlider = !it.showBrightnessSlider, showStyleSwitcher = false) }
    }

    fun dismissOverlays() {
        _uiState.update { it.copy(showStyleSwitcher = false, showBrightnessSlider = false) }
    }

    fun setDotSize(size: Int) {
        _uiState.update { it.copy(dotSize = size) }
    }

    /**
     * Capture the current matrix frame and save to gallery.
     */
    fun captureFrame(format: ImageCapture.Format = ImageCapture.Format.PNG) {
        val bitmap = _uiState.value.matrixBitmap ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isCaptureFlash = true) }
            delay(150)
            _uiState.update { it.copy(isCaptureFlash = false) }
            val path = imageCapture.saveToGallery(bitmap, format)
            _uiState.update { it.copy(lastSavedPath = path) }
            delay(2000)
            _uiState.update { it.copy(lastSavedPath = null) }
        }
    }

    /**
     * Get the current bitmap for avatar generation.
     */
    fun getCurrentBitmap(): Bitmap? = _uiState.value.matrixBitmap

    override fun onCleared() {
        super.onCleared()
        processingJob?.cancel()
        renderer.release()
        faceDetector.close()
    }
}
