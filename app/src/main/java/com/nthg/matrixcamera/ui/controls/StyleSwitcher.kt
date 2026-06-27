package com.nthg.matrixcamera.ui.controls

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nthg.matrixcamera.matrix.MatrixStyle
import com.nthg.matrixcamera.ui.theme.ControlBg
import com.nthg.matrixcamera.ui.theme.SpaceMono

/**
 * Horizontal style picker — shown above the control bar when style button is tapped.
 * Displays all 6 matrix styles as pill chips.
 */
@Composable
fun StyleSwitcher(
    currentStyle: MatrixStyle,
    onStyleSelected: (MatrixStyle) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(MatrixStyle.entries) { style ->
            StyleChip(
                style = style,
                isSelected = style == currentStyle,
                onClick = { onStyleSelected(style) }
            )
        }
    }
}

@Composable
private fun StyleChip(
    style: MatrixStyle,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isSelected) Color.White else ControlBg
    val textColor = if (isSelected) Color.Black else Color.White
    val borderColor = if (isSelected) Color.White else Color.White.copy(alpha = 0.2f)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(bg)
            .border(1.dp, borderColor, RoundedCornerShape(100.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = style.shortName,
                fontFamily = SpaceMono,
                fontSize = 9.sp,
                color = textColor.copy(alpha = 0.6f),
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Normal
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = style.displayName,
                fontFamily = SpaceMono,
                fontSize = 11.sp,
                color = textColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
