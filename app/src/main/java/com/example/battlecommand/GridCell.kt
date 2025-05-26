package com.example.battlecommand

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.battlecommand.model.CellState

@Composable
fun GridCell(
    state: CellState,
    isOpponentCell: Boolean,
    isInFortifyModeForOwnBoard: Boolean,
    defaultCellBackgroundColor: Color,
    onClick: () -> Unit,
    size: androidx.compose.ui.unit.Dp,
    cornerRadius: androidx.compose.ui.unit.Dp
) {

    val backgroundColor = when {
        state == CellState.HIT -> MaterialTheme.colorScheme.tertiary
        state == CellState.MISS -> Color.Gray.copy(alpha = 0.0f)
        state == CellState.SHIP -> {
            if (isOpponentCell || !isInFortifyModeForOwnBoard) {
                defaultCellBackgroundColor
            } else {
                Color.DarkGray.copy(alpha = 0.8f)
            }
        }

        state == CellState.EMPTY -> defaultCellBackgroundColor
        else -> defaultCellBackgroundColor
    }

    val markerText: String?
    val markerColor: Color

    when (state) {
        CellState.HIT -> {
            markerText = "•"
            markerColor = defaultCellBackgroundColor
        }

        CellState.MISS -> {
            markerText = "✖"
            markerColor = defaultCellBackgroundColor
        }

        else -> {
            markerText = null
            markerColor = Color.Unspecified
        }
    }

    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .clickable(onClick = onClick)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        if (markerText != null) {
            Text(
                text = markerText,
                color = markerColor,
                fontSize = size.value.sp / 1.8f,
                fontWeight = FontWeight.Bold
            )
        }
    }
}