package com.nthg.matrixcamera.ui.controls

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Brightness6
import androidx.compose.material.icons.rounded.Cameraswitch
import androidx.compose.material.icons.rounded.FiberManualRecord
import androidx.compose.material.icons.rounded.Flip
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nthg.matrixcamera.matrix.MatrixStyle
import com.nthg.matrixcamera.ui.theme.ControlBg
import com.nthg.matrixcamera.ui.theme.ControlBorder
import com.nthg.matrixcamera.ui.theme.SpaceMono

/**
 * Bottom floating control bar.
 *
 * Layout:
 *   [ Mirror ]  [ Style ]  [ CAPTURE ]  [ Brightness ]  [ Settings ]
 *
 * Above the bar, optional overlays appear:
 * - Style switcher carousel
 * - Brightness slider
 */
@Composable
fun ControlBar(
    currentStyle: MatrixStyle,
    brightness: Float,
    showStyleSwitcher: Boolean,
    showBrightnessSlider: Boolean,
    onCapture: () -> Unit,
    onToggleMirror: () -> Unit,
    onToggleStyle: () -> Unit,
    onStyleSelected: (MatrixStyle) -> Unit,
    onToggleBrightness: () -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Style switcher overlay
        AnimatedVisibility(
            visible = showStyleSwitcher,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it }
        ) {
            StyleSwitcher(
                currentStyle = currentStyle,
                onStyleSelected = onStyleSelected,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // Brightness slider overlay
        AnimatedVisibility(
            visible = showBrightnessSlider,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it }
        ) {
            BrightnessSlider(
                brightness = brightness,
                onBrightnessChange = onBrightnessChange,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // Main control bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(40.dp))
                .background(ControlBg)
                .border(1.dp, ControlBorder, RoundedCornerShape(40.dp))
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mirror
            ControlIconButton(
                icon = { Icon(Icons.Rounded.Flip, "Mirror", tint = Color.White, modifier = Modifier.size(22.dp)) },
                onClick = onToggleMirror
            )

            // Style switcher
            ControlIconButton(
                icon = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.AutoAwesome, "Style", tint = Color.White, modifier = Modifier.size(20.dp))
                        Text(
                            text = currentStyle.shortName,
                            fontFamily = SpaceMono,
                            fontSize = 8.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Normal
                        )
                    }
                },
                onClick = onToggleStyle
            )

            // Capture button
            CaptureButton(onClick = onCapture)

            // Brightness
            ControlIconButton(
                icon = { Icon(Icons.Rounded.Brightness6, "Brightness", tint = Color.White, modifier = Modifier.size(22.dp)) },
                onClick = onToggleBrightness
            )

            // Settings
            ControlIconButton(
                icon = { Icon(Icons.Rounded.Settings, "Settings", tint = Color.White, modifier = Modifier.size(22.dp)) },
                onClick = onOpenSettings
            )
        }
    }
}

@Composable
private fun CaptureButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(Color.White)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = Color.Black)
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .border(2.dp, Color.Black, CircleShape)
                .background(Color.White)
        )
    }
}

@Composable
private fun ControlIconButton(
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        icon()
    }
}

@Composable
private fun BrightnessSlider(
    brightness: Float,
    onBrightnessChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(ControlBg)
            .border(1.dp, ControlBorder, RoundedCornerShape(24.dp))
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(
            text = "BRIGHTNESS",
            fontFamily = SpaceMono,
            fontSize = 10.sp,
            color = Color.White.copy(alpha = 0.6f),
            letterSpacing = 1.5.sp
        )
        Spacer(Modifier.height(8.dp))
        Slider(
            value = brightness,
            onValueChange = onBrightnessChange,
            valueRange = 0.3f..2.0f,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White,
                inactiveTrackColor = Color.White.copy(alpha = 0.2f)
            )
        )
    }
}
