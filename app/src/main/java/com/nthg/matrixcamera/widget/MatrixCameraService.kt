package com.nthg.matrixcamera.widget

import android.app.NotificationChannel
import android.app.NotificationManager
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import android.util.Log
import android.util.Size
import android.view.Surface
import android.widget.RemoteViews
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.nthg.matrixcamera.R
import com.nthg.matrixcamera.analyzer.FaceDetectionAnalyzer
import com.nthg.matrixcamera.processor.PixelMosaicProcessor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.Executor

/**
 * Foreground service that captures camera frames in the background,
 * processes them into Matrix images, and updates the homescreen widget.
 * 
 * Optimized for Nothing Phone 2a and CMF Phone 1.
 */
class MatrixCameraService : LifecycleService() {

    // L7: HandlerThread with video priority for elevated scheduling
    private lateinit var cameraThread: HandlerThread
    private lateinit var cameraHandler: Handler
    private lateinit var cameraExecutor: Executor

    private val processor = PixelMosaicProcessor(outputSize = 480, cellSize = 9)
    private var lastProcessedBitmap: Bitmap? = null
    private var updateJob: Job? = null
    private var cameraProvider: ProcessCameraProvider? = null

    // L5: Frame rate capping
    private var lastFrameTime = 0L
    private val frameInterval = 66L // ~15 FPS target

    companion object {
        private const val CHANNEL_ID = "matrix_camera_service"
        private const val NOTIFICATION_ID = 101
        private const val TAG = "MatrixCameraService"
        // Q8: Higher source resolution for detail retention
        private val CAPTURE_SIZE = Size(640, 480)

        fun start(context: Context) {
            val intent = Intent(context, MatrixCameraService::class.java)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, MatrixCameraService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        
        // L7: Initialize high-priority thread
        cameraThread = HandlerThread("CameraThread", Process.THREAD_PRIORITY_VIDEO)
        cameraThread.start()
        cameraHandler = Handler(cameraThread.looper)
        cameraExecutor = Executor { command -> cameraHandler.post(command) }

        MatrixWidgetProvider.isServiceRunning = true
        createNotificationChannel()
        startForegroundService()
        if (hasCameraPermission()) {
            startCamera()
            startWidgetUpdateLoop()
        } else {
            notifyPermissionRequired()
            stopSelf()
        }
    }

    private fun hasCameraPermission(): Boolean {
        val permission = android.Manifest.permission.CAMERA
        val res = ContextCompat.checkSelfPermission(this, permission)
        return res == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    private fun notifyPermissionRequired() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Permission Required")
            .setContentText("Please open Matrix Camera to grant camera access.")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Matrix Widget Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun startForegroundService() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Matrix Widget Active")
            .setContentText("Capturing background matrix feed")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun startCamera() {
        val future = ProcessCameraProvider.getInstance(this)
        future.addListener({
            runCatching {
                val provider = future.get()
                cameraProvider = provider
                
                val frontCamera = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                    .build()

                val imageAnalysis = ImageAnalysis.Builder()
                    .setResolutionSelector(
                        ResolutionSelector.Builder()
                            .setResolutionStrategy(
                                ResolutionStrategy(
                                    CAPTURE_SIZE,
                                    ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
                                )
                            )
                            .build()
                    )
                    .setTargetRotation(Surface.ROTATION_0)
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    // L1/Q1: Eliminate JPEG trip with direct RGBA_8888
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                    .build()
                    .also { 
                        it.setAnalyzer(cameraExecutor, FaceDetectionAnalyzer(processor) { bitmap ->
                            // L5: Frame rate limiting
                            val now = System.currentTimeMillis()
                            if (now - lastFrameTime >= frameInterval) {
                                // L6: Safe swap and recycle
                                val old = lastProcessedBitmap
                                lastProcessedBitmap = bitmap
                                old?.recycle()
                                lastFrameTime = now
                            } else {
                                bitmap.recycle()
                            }
                        })
                    }

                provider.unbindAll()
                provider.bindToLifecycle(this, frontCamera, imageAnalysis)
            }.onFailure { e ->
                Log.e(TAG, "Camera init failed", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun startWidgetUpdateLoop() {
        updateJob = lifecycleScope.launch {
            while (true) {
                val bitmap = lastProcessedBitmap
                if (bitmap != null) {
                    updateWidgets(bitmap)
                }
                delay(1000) 
            }
        }
    }

    private fun updateWidgets(bitmap: Bitmap) {
        try {
            val appWidgetManager = AppWidgetManager.getInstance(this)
            val componentName = ComponentName(this, MatrixWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

            if (appWidgetIds.isEmpty()) return

            val views = RemoteViews(packageName, R.layout.widget_matrix)
            views.setImageViewBitmap(R.id.widget_icon, bitmap)
            views.setTextViewText(R.id.widget_style, "LIVE FEED")

            appWidgetIds.forEach { id ->
                appWidgetManager.updateAppWidget(id, views)
            }
        } catch (e: Exception) {
            // L6: Silently fail to avoid crashing the service on IPC errors
            Log.e(TAG, "Widget update failed", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        MatrixWidgetProvider.isServiceRunning = false
        updateJob?.cancel()
        cameraProvider?.unbindAll()
        cameraThread.quitSafely()
        lastProcessedBitmap?.recycle()
        lastProcessedBitmap = null
    }
}
