package com.nthg.matrixcamera.ui.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.nthg.matrixcamera.ui.theme.MatrixCameraTheme

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MatrixCameraTheme {
                SettingsScreen(onBack = { finish() })
            }
        }
    }
}
