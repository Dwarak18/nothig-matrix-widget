package com.nthg.matrixcamera.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nthg.matrixcamera.matrix.MatrixStyle
import com.nthg.matrixcamera.ui.theme.ControlBorder
import com.nthg.matrixcamera.ui.theme.SpaceGrotesk
import com.nthg.matrixcamera.ui.theme.SpaceMono
import com.nthg.matrixcamera.ui.theme.VerySubtleWhite

/**
 * Settings screen.
 *
 * Options:
 * - Default style
 * - Dot size (grid resolution)
 * - Capture format preference
 * - About / version
 */
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    var selectedStyle by remember { mutableStateOf(MatrixStyle.NOTHING_MATRIX) }
    var dotSize by remember { mutableFloatStateOf(12f) }
    var captureFormat by remember { mutableStateOf("PNG") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, "Back", tint = Color.White)
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = "SETTINGS",
                fontFamily = SpaceMono,
                fontSize = 13.sp,
                color = Color.White,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            // Default Style
            SettingsSection(title = "DEFAULT STYLE") {
                MatrixStyle.entries.forEach { style ->
                    SettingsSelectable(
                        label = style.displayName,
                        sublabel = style.shortName,
                        isSelected = selectedStyle == style,
                        onClick = { selectedStyle = style }
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }

            Spacer(Modifier.height(24.dp))

            // Dot Size
            SettingsSection(title = "DOT GRID SIZE") {
                Text(
                    text = "${dotSize.toInt()} px",
                    fontFamily = SpaceMono,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
                Spacer(Modifier.height(8.dp))
                Slider(
                    value = dotSize,
                    onValueChange = { dotSize = it },
                    valueRange = 8f..24f,
                    steps = 7,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.White,
                        inactiveTrackColor = Color.White.copy(alpha = 0.2f),
                        activeTickColor = Color.Transparent,
                        inactiveTickColor = Color.Transparent
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Fine", fontFamily = SpaceMono, fontSize = 9.sp, color = Color.White.copy(alpha = 0.3f))
                    Text("Coarse", fontFamily = SpaceMono, fontSize = 9.sp, color = Color.White.copy(alpha = 0.3f))
                }
            }

            Spacer(Modifier.height(24.dp))

            // Capture Format
            SettingsSection(title = "CAPTURE FORMAT") {
                listOf("PNG", "JPG", "TRANSPARENT PNG").forEach { format ->
                    SettingsSelectable(
                        label = format,
                        sublabel = when (format) {
                            "PNG" -> "Lossless"
                            "JPG" -> "Compressed"
                            else -> "Transparent background"
                        },
                        isSelected = captureFormat == format,
                        onClick = { captureFormat = format }
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }

            Spacer(Modifier.height(24.dp))

            // About
            SettingsSection(title = "ABOUT") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(VerySubtleWhite)
                        .border(1.dp, ControlBorder, RoundedCornerShape(16.dp))
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Matrix Camera",
                        fontFamily = SpaceGrotesk,
                        fontSize = 18.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Version 1.0.0",
                        fontFamily = SpaceMono,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.4f)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Live Matrix-style camera inspired by Nothing OS. Processing happens entirely on-device. No data leaves your phone.",
                        fontFamily = SpaceGrotesk,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Text(
        text = title,
        fontFamily = SpaceMono,
        fontSize = 10.sp,
        color = Color.White.copy(alpha = 0.4f),
        letterSpacing = 2.sp,
        modifier = Modifier.padding(bottom = 12.dp)
    )
    content()
}

@Composable
private fun SettingsSelectable(
    label: String,
    sublabel: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isSelected) Color.White.copy(alpha = 0.08f) else Color.Transparent
    val border = if (isSelected) Color.White.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.1f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = label,
                fontFamily = SpaceGrotesk,
                fontSize = 15.sp,
                color = Color.White,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
            )
            Text(
                text = sublabel,
                fontFamily = SpaceMono,
                fontSize = 9.sp,
                color = Color.White.copy(alpha = 0.4f)
            )
        }
        if (isSelected) {
            Icon(
                Icons.Rounded.Check,
                contentDescription = "Selected",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
