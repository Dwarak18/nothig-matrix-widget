package com.nthg.matrixcamera.ui.avatar

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nthg.matrixcamera.ui.theme.ControlBorder
import com.nthg.matrixcamera.ui.theme.SpaceGrotesk
import com.nthg.matrixcamera.ui.theme.SpaceMono
import com.nthg.matrixcamera.ui.theme.VerySubtleWhite

enum class AvatarType(val label: String) {
    CIRCULAR("Circular"),
    SQUARE("Square"),
    WALLPAPER("Wallpaper"),
    CONTACT("Contact Photo"),
    PROFILE("Profile Image")
}

/**
 * Avatar generator screen.
 * One-tap generation of various avatar formats from the current matrix frame.
 */
@Composable
fun AvatarScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var selectedType by remember { mutableStateOf(AvatarType.CIRCULAR) }
    var savedUri by remember { mutableStateOf<Uri?>(null) }

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
                text = "AVATAR GENERATOR",
                fontFamily = SpaceMono,
                fontSize = 12.sp,
                color = Color.White,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Preview area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .aspectRatio(1f)
                .clip(
                    when (selectedType) {
                        AvatarType.CIRCULAR -> CircleShape
                        AvatarType.SQUARE, AvatarType.CONTACT, AvatarType.PROFILE -> RoundedCornerShape(24.dp)
                        AvatarType.WALLPAPER -> RoundedCornerShape(12.dp)
                    }
                )
                .background(Color.White.copy(alpha = 0.05f))
                .border(
                    1.dp,
                    ControlBorder,
                    when (selectedType) {
                        AvatarType.CIRCULAR -> CircleShape
                        else -> RoundedCornerShape(24.dp)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            // Preview placeholder — in production this shows the matrix bitmap
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("◉", fontSize = 48.sp, color = Color.White.copy(alpha = 0.2f))
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Matrix frame preview",
                    fontFamily = SpaceMono,
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.2f)
                )
            }
        }

        // Type selector
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = "FORMAT",
                fontFamily = SpaceMono,
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.4f),
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(AvatarType.CIRCULAR, AvatarType.SQUARE, AvatarType.WALLPAPER).forEach { type ->
                    AvatarTypeChip(
                        label = type.label,
                        isSelected = selectedType == type,
                        onClick = { selectedType = type },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(AvatarType.CONTACT, AvatarType.PROFILE).forEach { type ->
                    AvatarTypeChip(
                        label = type.label,
                        isSelected = selectedType == type,
                        onClick = { selectedType = type },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Action buttons
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Button(
                onClick = { /* Save logic via ImageCapture */ },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(100.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                Icon(Icons.Rounded.Download, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "SAVE TO GALLERY",
                    fontFamily = SpaceMono,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    savedUri?.let { uri ->
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "image/png"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share via"))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(100.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ControlBorder)
            ) {
                Icon(Icons.Rounded.Share, null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "SHARE",
                    fontFamily = SpaceMono,
                    fontSize = 12.sp,
                    color = Color.White,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun AvatarTypeChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isSelected) Color.White.copy(alpha = 0.1f) else Color.Transparent
    val border = if (isSelected) Color.White.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.1f)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(12.dp))
            .then(Modifier.padding(vertical = 10.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = SpaceGrotesk,
            fontSize = 13.sp,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
    }
}
