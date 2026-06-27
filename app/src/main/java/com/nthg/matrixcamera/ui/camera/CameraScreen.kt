package com.nthg.matrixcamera.ui.camera

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nthg.matrixcamera.camera.CameraManager
import com.nthg.matrixcamera.capture.ImageCapture
import com.nthg.matrixcamera.ui.components.BootAnimation
import com.nthg.matrixcamera.ui.components.FaceScanOverlay
import com.nthg.matrixcamera.ui.components.MatrixCanvas
import com.nthg.matrixcamera.ui.controls.ControlBar
import com.nthg.matrixcamera.ui.theme.SpaceGrotesk
import com.nthg.matrixcamera.ui.theme.SpaceMono

/**
 * Main camera screen.
 *
 * Layout:
 *   ┌─────────────────────────────┐
 *   │   Status bar (transparent)  │
 *   │   Style label (top center)  │
 *   │                             │
 *   │     Matrix Canvas           │
 *   │     + Face Scan Overlay     │
 *   │                             │
 *   │   Control Bar (bottom)      │
 *   └─────────────────────────────┘
 */
@Composable
fun CameraScreen(
    viewModel: CameraViewModel = viewModel(),
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()

    var hasCameraPermission by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasCameraPermission = permissions[Manifest.permission.CAMERA] ?: false
    }

    LaunchedEffect(Unit) {
        val permissions = mutableListOf(Manifest.permission.CAMERA)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(permissions.toTypedArray())
    }

    // Start camera once permission granted
    val cameraManager = remember { CameraManager(context) }
    DisposableEffect(hasCameraPermission) {
        if (hasCameraPermission) {
            cameraManager.startCamera(lifecycleOwner) { bitmap, w, h ->
                viewModel.processFrame(bitmap, w, h)
            }
        }
        onDispose { cameraManager.shutdown() }
    }

    // Capture flash alpha
    val flashAlpha by animateFloatAsState(
        targetValue = if (uiState.isCaptureFlash) 1f else 0f,
        animationSpec = tween(150),
        label = "captureFlash"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { viewModel.dismissOverlays() }
    ) {
        // Measure screen size
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { coords ->
                    viewModel.setScreenSize(coords.size.width, coords.size.height)
                }
        ) {
            // Matrix canvas
            MatrixCanvas(bitmap = uiState.matrixBitmap)

            // Face scan overlay
            if (!uiState.isBooting) {
                FaceScanOverlay(faceData = uiState.faceData)
            }

            // Boot animation overlay
            AnimatedVisibility(
                visible = uiState.isBooting,
                enter = fadeIn(),
                exit = fadeOut(animationSpec = tween(400))
            ) {
                BootAnimation()
            }

            // Capture flash
            if (flashAlpha > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(flashAlpha)
                        .background(Color.White)
                )
            }
        }

        // Top overlay: style name + face status
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = uiState.currentStyle.displayName.uppercase(),
                fontFamily = SpaceMono,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.5f),
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Normal
            )
            if (uiState.faceData.boundingBox != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = when {
                        uiState.faceData.isSmiling -> "◉ SMILING"
                        uiState.faceData.isBothEyesClosed -> "◉ BLINK"
                        else -> "◉ FACE DETECTED"
                    },
                    fontFamily = SpaceMono,
                    fontSize = 9.sp,
                    color = Color.White.copy(alpha = 0.4f),
                    letterSpacing = 1.5.sp
                )
            }
        }

        // Saved toast
        AnimatedVisibility(
            visible = uiState.lastSavedPath != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 200.dp)
        ) {
            Text(
                text = "Saved to gallery",
                fontFamily = SpaceGrotesk,
                fontSize = 14.sp,
                color = Color.White,
                modifier = Modifier
                    .background(
                        Color.White.copy(alpha = 0.15f),
                        RoundedCornerShape(100.dp)
                    )
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            )
        }

        // Permission denied state
        if (!hasCameraPermission) {
            PermissionDeniedView(
                onGrantPermission = {
                    val permissions = mutableListOf(Manifest.permission.CAMERA)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissions.add(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    permissionLauncher.launch(permissions.toTypedArray())
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Bottom control bar
        if (hasCameraPermission) {
            ControlBar(
                currentStyle = uiState.currentStyle,
                brightness = uiState.brightness,
                showStyleSwitcher = uiState.showStyleSwitcher,
                showBrightnessSlider = uiState.showBrightnessSlider,
                onCapture = { viewModel.captureFrame(ImageCapture.Format.PNG) },
                onToggleMirror = viewModel::toggleMirror,
                onToggleStyle = viewModel::toggleStyleSwitcher,
                onStyleSelected = viewModel::setStyle,
                onToggleBrightness = viewModel::toggleBrightnessSlider,
                onBrightnessChange = viewModel::setBrightness,
                onOpenSettings = onOpenSettings,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
            )
        }
    }
}

@Composable
private fun PermissionDeniedView(
    onGrantPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(40.dp)
        ) {
            Text(
                text = "◉",
                fontSize = 40.sp,
                color = Color.White.copy(alpha = 0.4f)
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = "CAMERA REQUIRED",
                fontFamily = SpaceMono,
                fontSize = 13.sp,
                color = Color.White,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Matrix Camera needs access to your\nfront camera to create the matrix effect.",
                fontFamily = SpaceGrotesk,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = onGrantPermission,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(100.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "GRANT PERMISSION",
                    fontFamily = SpaceMono,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
        }
    }
}
