package com.nthg.matrixcamera.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import com.nthg.matrixcamera.R
import com.nthg.matrixcamera.ui.camera.CameraActivity

/**
 * Homescreen Widget Provider — 2x2 Matrix Camera launcher & Background Feed.
 */
class MatrixWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_TOGGLE_CAMERA = "com.nthg.matrixcamera.ACTION_TOGGLE_CAMERA"
        private const val TAG = "MatrixWidget"
        
        var isServiceRunning = false
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_TOGGLE_CAMERA) {
            if (isServiceRunning) {
                MatrixCameraService.stop(context)
                isServiceRunning = false
            } else {
                MatrixCameraService.start(context)
                isServiceRunning = true
            }
            // Trigger update to show state change
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, MatrixWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d(TAG, "onUpdate called. Service running: $isServiceRunning")
        appWidgetIds.forEach { widgetId ->
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        widgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_matrix)
        
        // Toggle Intent
        val toggleIntent = Intent(context, MatrixWidgetProvider::class.java).apply {
            action = ACTION_TOGGLE_CAMERA
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getBroadcast(context, widgetId, toggleIntent, flags)
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

        // UI State
        if (isServiceRunning) {
            views.setTextViewText(R.id.widget_style, "LIVE • ACTIVE")
        } else {
            views.setImageViewResource(R.id.widget_icon, R.mipmap.ic_launcher)
            views.setTextViewText(R.id.widget_style, "NOTHING MATRIX")
        }

        appWidgetManager.updateAppWidget(widgetId, views)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        MatrixCameraService.stop(context)
        isServiceRunning = false
    }
}
