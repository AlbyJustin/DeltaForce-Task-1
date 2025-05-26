package com.example.battlecommand

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.example.battlecommand.model.CellState

@Composable
fun GameBoard(
    boardData: List<List<CellState>>,
    onCellClick: (row: Int, col: Int) -> Unit,
    isOpponentBoard: Boolean,
    isInFortifyMode: Boolean = false,
    cellSize: androidx.compose.ui.unit.Dp = 35.dp,
    cellSpacing: androidx.compose.ui.unit.Dp = 2.dp,
    cornerRadius: androidx.compose.ui.unit.Dp = 4.dp
) {
    val neutralCellColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)

    Column(verticalArrangement = Arrangement.spacedBy(cellSpacing)) {
        boardData.forEachIndexed { rowIndex, rowList ->
            Row(horizontalArrangement = Arrangement.spacedBy(cellSpacing)) {
                rowList.forEachIndexed { colIndex, cellState ->
                    GridCell(
                        state = cellState,
                        isOpponentCell = isOpponentBoard,
                        isInFortifyModeForOwnBoard = isInFortifyMode,
                        defaultCellBackgroundColor = neutralCellColor,
                        onClick = { onCellClick(rowIndex, colIndex) },
                        size = cellSize,
                        cornerRadius = cornerRadius
                    )
                }
            }
        }
    }
}