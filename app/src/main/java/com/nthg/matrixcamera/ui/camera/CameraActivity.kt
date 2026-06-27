package com.nthg.matrixcamera.ui.camera

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.nthg.matrixcamera.ui.settings.SettingsActivity
import com.nthg.matrixcamera.ui.theme.MatrixCameraTheme

/**
 * Main Activity — hosts the full-screen [CameraScreen].
 *
 * Configured for:
 * - True fullscreen (no status bar, no navigation bar)
 * - Edge-to-edge rendering
 * - Portrait only
 * - singleTop launch mode (instant widget tap response)
 */
class CameraActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge
        enableEdgeToEdge()

        // Hide system bars for true immersive fullscreen
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            MatrixCameraTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ) {
                    CameraScreen(
                        onOpenSettings = {
                            startActivity(Intent(this@CameraActivity, SettingsActivity::class.java))
                        }
                    )
                }
            }
        }
    }
}
