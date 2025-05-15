package com.example.battlecommand

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.battlecommand.ui.theme.BattleCommandTheme


const val PREFS_GAME_HISTORY = "BattleCommandGameHistoryPrefs"
const val KEY_PLAYER1_WINS = "player1_wins_count"
const val KEY_PLAYER2_WINS = "player2_wins_count"

enum class CellState {
    EMPTY, SHIP, HIT, MISS
}

data class Ship(
    val id: String,
    val size: Int,
    val coordinates: List<Pair<Int, Int>>,
    var hits: Int = 0,
    val isVertical: Boolean
) {
    fun isSunk(): Boolean = hits >= size
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BattleCommandTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GameScreen()
                }
            }
        }
    }
}


val initialP1ShipConfigs = listOf(
    Ship(
        id = "P1_Destroyer",
        size = 3,
        coordinates = listOf(Pair(0, 1), Pair(1, 1), Pair(2, 1)),
        isVertical = true,
        hits = 0
    ),
    Ship(
        id = "P1_Submarine",
        size = 2,
        coordinates = listOf(Pair(4, 3), Pair(4, 4)),
        isVertical = false,
        hits = 0
    )
)

val initialP2ShipConfigs = listOf(
    Ship(
        id = "P2_Destroyer",
        size = 3,
        coordinates = listOf(Pair(1, 3), Pair(2, 3), Pair(3, 3)),
        isVertical = true,
        hits = 0
    ),
    Ship(
        id = "P2_Submarine",
        size = 2,
        coordinates = listOf(Pair(0, 0), Pair(0, 1)),
        isVertical = false,
        hits = 0
    )
)

fun initializeBoard(size: Int, ships: List<Ship>): List<List<CellState>> {
    val initialBoard = MutableList(size) { MutableList(size) { CellState.EMPTY } }
    ships.forEach { ship ->
        ship.coordinates.forEach { (row, col) ->
            if (row < size && col < size) {
                initialBoard[row][col] = CellState.SHIP
            }
        }
    }
    return initialBoard.map { it.toList() }
}

@Preview
@Composable
fun GameScreen() {
    val gridSize = 5
    val P1 = "Player 1"
    val P2 = "Player 2"
    val context = LocalContext.current

    var player1Wins by remember { mutableStateOf(0) }
    var player2Wins by remember { mutableStateOf(0) }

    LaunchedEffect(key1 = context) {
        val prefs = context.getSharedPreferences(PREFS_GAME_HISTORY, Context.MODE_PRIVATE)
        player1Wins = prefs.getInt(KEY_PLAYER1_WINS, 0)
        player2Wins = prefs.getInt(KEY_PLAYER2_WINS, 0)
        println("Loaded Win History: P1 Wins = $player1Wins, P2 Wins = $player2Wins")
    }

    fun recordWin(winnerIdentifier: String) {
        val prefs = context.getSharedPreferences(PREFS_GAME_HISTORY, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        var newP1Wins = player1Wins
        var newP2Wins = player2Wins

        if (winnerIdentifier == P1) {
            newP1Wins += 1
            editor.putInt(KEY_PLAYER1_WINS, newP1Wins)
            player1Wins = newP1Wins
        } else if (winnerIdentifier == P2) {
            newP2Wins += 1
            editor.putInt(KEY_PLAYER2_WINS, newP2Wins)
            player2Wins = newP2Wins
        }
        editor.apply()
        println("Recorded Win: $winnerIdentifier. New scores: P1=$newP1Wins, P2=$newP2Wins")
    }

    val player1Ships =
        remember { mutableStateListOf<Ship>().apply { addAll(initialP1ShipConfigs.map { it.copy() }) } }
    val player2Ships =
        remember { mutableStateListOf<Ship>().apply { addAll(initialP2ShipConfigs.map { it.copy() }) } }

    val player1Board = remember { mutableStateOf(initializeBoard(gridSize, player1Ships)) }
    val player2Board = remember { mutableStateOf(initializeBoard(gridSize, player2Ships)) }

    var currentPlayer by remember { mutableStateOf(P1) }
    var isGameOver by remember { mutableStateOf(false) }
    var gameStatusMessage by remember { mutableStateOf("") }

    var isInFortifyMode by remember { mutableStateOf(false) }
    var selectedShipForFortify by remember { mutableStateOf<Ship?>(null) }
    var fortifyStatusMessage by remember { mutableStateOf("") }

    var showGameOverDialog by remember { mutableStateOf(false) }
    var showRulesDialog by remember { mutableStateOf(false) }
    var showDescriptionDialog by remember { mutableStateOf(false) }

    fun resetGame() {
        player1Ships.clear()
        player1Ships.addAll(initialP1ShipConfigs.map { it.copy() })

        player2Ships.clear()
        player2Ships.addAll(initialP2ShipConfigs.map { it.copy() })

        player1Board.value = initializeBoard(gridSize, player1Ships)
        player2Board.value = initializeBoard(gridSize, player2Ships)

        currentPlayer = P1
        isGameOver = false
        gameStatusMessage = ""

        isInFortifyMode = false
        selectedShipForFortify = null
        fortifyStatusMessage = ""

        println("Game Reset!")
        showGameOverDialog = false
    }

    val opponentBoardState = if (currentPlayer == P1) player2Board else player1Board
    val opponentShipsList = if (currentPlayer == P1) player2Ships else player1Ships
    val ownBoard = if (currentPlayer == P1) player1Board else player2Board
    val ownShipsList = if (currentPlayer == P1) player1Ships else player2Ships

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("P1 Wins: $player1Wins", style = MaterialTheme.typography.titleMedium)
                Text("P2 Wins: $player2Wins", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(8.dp))

            Text("Turn: $currentPlayer", style = MaterialTheme.typography.headlineSmall)
            if (isGameOver && !showGameOverDialog) {
                Text(
                    gameStatusMessage,
                    color = Color.Red,
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            Text(fortifyStatusMessage, color = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.height(16.dp))
        }

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Opponent's grid
            Text("Opponent's Grid", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            GameBoard(
                boardData = opponentBoardState.value,
                isOpponentBoard = true,
                onCellClick = { row, col ->
                    if (isGameOver) return@GameBoard
                    val clickedCellStateOnAttackerView = opponentBoardState.value[row][col]
                    if (clickedCellStateOnAttackerView == CellState.HIT || clickedCellStateOnAttackerView == CellState.MISS) {
                        println("Already attacked ($row, $col).")
                        return@GameBoard
                    }

                    val actualOpponentRealBoard =
                        if (currentPlayer == P1) player2Board else player1Board
                    val actualOpponentShips =
                        if (currentPlayer == P1) player2Ships else player1Ships

                    var actualState = CellState.EMPTY
                    val shipAtTarget =
                        actualOpponentShips.find { it.coordinates.contains(Pair(row, col)) }
                    if (shipAtTarget != null) {
                        if (actualOpponentRealBoard.value[row][col] != CellState.HIT) {
                            actualState = CellState.SHIP
                        } else {
                            actualState = CellState.HIT
                        }
                    } else {
                        actualState = actualOpponentRealBoard.value[row][col]
                    }


                    val newOpponentViewBoard =
                        opponentBoardState.value.map { it.toMutableList() }.toMutableList()
                    var message = ""
                    var validMoveMade = false

                    when (actualState) {
                        CellState.EMPTY -> {
                            newOpponentViewBoard[row][col] = CellState.MISS
                            val defenderBoardMutable =
                                actualOpponentRealBoard.value.map { it.toMutableList() }
                                    .toMutableList()
                            defenderBoardMutable[row][col] = CellState.MISS
                            actualOpponentRealBoard.value = defenderBoardMutable.map { it.toList() }

                            message = "MISS at ($row, $col)"
                            validMoveMade = true
                        }

                        CellState.SHIP -> {
                            newOpponentViewBoard[row][col] = CellState.HIT
                            val defenderBoardMutable =
                                actualOpponentRealBoard.value.map { it.toMutableList() }
                                    .toMutableList()
                            defenderBoardMutable[row][col] = CellState.HIT
                            actualOpponentRealBoard.value = defenderBoardMutable.map { it.toList() }

                            validMoveMade = true
                            val targetShipObject = actualOpponentShips.find { ship ->
                                ship.coordinates.contains(
                                    Pair(
                                        row,
                                        col
                                    )
                                )
                            }!!
                            targetShipObject.hits += 1
                            message = "HIT ${targetShipObject.id} at ($row, $col)!"
                            if (targetShipObject.isSunk()) {
                                message += " ${targetShipObject.id} SUNK!"
                                if (actualOpponentShips.all { it.isSunk() }) {
                                    isGameOver = true
                                    gameStatusMessage = "GAME OVER! $currentPlayer Wins!"
                                    showGameOverDialog = true
                                    recordWin(currentPlayer)
                                }
                            }
                        }

                        CellState.HIT, CellState.MISS -> {
                            println("Error: Attacked an already revealed HIT/MISS cell: ($row, $col)")
                            return@GameBoard
                        }
                    }

                    if (validMoveMade) {
                        opponentBoardState.value = newOpponentViewBoard.map { it.toList() }
                        println(message)
                        if (!isGameOver) {
                            currentPlayer = if (currentPlayer == P1) P2 else P1
                        }
                    }
                }
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Button( // Fortify Button
                onClick = {
                    isInFortifyMode = !isInFortifyMode
                    selectedShipForFortify = null
                    fortifyStatusMessage =
                        if (isInFortifyMode) "FORTIFY MODE: Select undamaged ship." else ""
                },
                enabled = !isGameOver,
                shape = RoundedCornerShape(percent = 50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
                modifier = Modifier
                    .defaultMinSize(minWidth = 150.dp, minHeight = 48.dp)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = if (isInFortifyMode) "Cancel Fortify" else "FORTIFY",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { // Rules & Description
                Button(
                    onClick = { showRulesDialog = true },
                    shape = RoundedCornerShape(percent = 50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.defaultMinSize(minWidth = 150.dp, minHeight = 48.dp)
                ) { Text("Game Rules", fontSize = 16.sp) }
                Button(
                    onClick = { showDescriptionDialog = true },
                    shape = RoundedCornerShape(percent = 50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.defaultMinSize(minWidth = 180.dp, minHeight = 48.dp)
                ) { Text("Game Description", fontSize = 16.sp) }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button( // Reset Button
                onClick = { resetGame() },
                shape = RoundedCornerShape(percent = 50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                ),
                modifier = Modifier.defaultMinSize(minWidth = 150.dp, minHeight = 48.dp)
            ) {
                Text("Reset Game", fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Player's board
            Text("Your Grid", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            GameBoard(
                boardData = ownBoard.value,
                isOpponentBoard = false,
                isInFortifyMode = isInFortifyMode,
                cellSize = 25.dp,
                onCellClick = onCellClick@{ row, col ->
                    if (isGameOver) return@onCellClick

                    if (isInFortifyMode) {
                        fortifyStatusMessage = ""
                        val shipAtClickedCell =
                            ownShipsList.find { ship -> ship.coordinates.contains(Pair(row, col)) }

                        if (selectedShipForFortify == null) {
                            if (shipAtClickedCell != null) {
                                if (shipAtClickedCell.hits == 0) {
                                    selectedShipForFortify = shipAtClickedCell
                                    fortifyStatusMessage =
                                        "${shipAtClickedCell.id} selected. Click an empty area to move it."
                                } else {
                                    fortifyStatusMessage =
                                        "${shipAtClickedCell.id} is damaged and cannot be moved."
                                }
                            } else {
                                fortifyStatusMessage =
                                    "Click one of your undamaged ships to select it."
                            }
                        } else {
                            val shipToMove = selectedShipForFortify!!
                            val targetAnchorRow = row
                            val targetAnchorCol = col

                            if (shipAtClickedCell == shipToMove) {
                                selectedShipForFortify = null
                                fortifyStatusMessage =
                                    "Ship deselected. Select a ship or destination."
                            } else if (shipAtClickedCell != null && shipAtClickedCell != shipToMove) {
                                fortifyStatusMessage =
                                    "${shipToMove.id} is selected. Click an empty destination or deselect to choose another ship."
                            } else {

                                val newProposedCoordinates = mutableListOf<Pair<Int, Int>>()
                                for (i in 0 until shipToMove.size) {
                                    if (shipToMove.isVertical) {
                                        newProposedCoordinates.add(
                                            Pair(
                                                targetAnchorRow + i,
                                                targetAnchorCol
                                            )
                                        )
                                    } else {
                                        newProposedCoordinates.add(
                                            Pair(
                                                targetAnchorRow,
                                                targetAnchorCol + i
                                            )
                                        )
                                    }
                                }

                                var isValidMove = true
                                var validationMessage = ""

                                for ((r, c) in newProposedCoordinates) {
                                    if (r !in 0 until gridSize || c !in 0 until gridSize) {
                                        isValidMove = false; validationMessage =
                                            "Out of bounds."; break
                                    }
                                    if (ownBoard.value[r][c] == CellState.MISS || ownBoard.value[r][c] == CellState.HIT) {
                                        isValidMove = false; validationMessage =
                                            "Cannot move to MISS/HIT."; break
                                    }
                                }
                                if (isValidMove) {
                                    for (otherShip in ownShipsList) {
                                        if (otherShip.id == shipToMove.id) continue
                                        if (newProposedCoordinates.any {
                                                otherShip.coordinates.contains(
                                                    it
                                                )
                                            }) {
                                            isValidMove = false; validationMessage =
                                                "Collides with ${otherShip.id}."; break
                                        }
                                    }
                                }
                                if (isValidMove && newProposedCoordinates.size == shipToMove.coordinates.size && newProposedCoordinates.all {
                                        shipToMove.coordinates.contains(
                                            it
                                        )
                                    }) {
                                    isValidMove = false; validationMessage =
                                        "Already at that location."
                                }


                                if (isValidMove) {
                                    val currentBoardMutable =
                                        ownBoard.value.map { it.toMutableList() }.toMutableList()

                                    shipToMove.coordinates.forEach { (r, c) ->
                                        if (r < gridSize && c < gridSize) currentBoardMutable[r][c] =
                                            CellState.EMPTY
                                    }

                                    val shipIndex =
                                        ownShipsList.indexOfFirst { it.id == shipToMove.id }
                                    if (shipIndex != -1) {
                                        val updatedShip =
                                            shipToMove.copy(coordinates = newProposedCoordinates.toList())
                                        ownShipsList[shipIndex] = updatedShip
                                    } else {
                                        fortifyStatusMessage =
                                            "Error: Could not find ship to update."
                                        return@onCellClick
                                    }

                                    newProposedCoordinates.forEach { (r, c) ->
                                        if (r < gridSize && c < gridSize) currentBoardMutable[r][c] =
                                            CellState.SHIP
                                    }
                                    ownBoard.value = currentBoardMutable.map { it.toList() }

                                    fortifyStatusMessage = "${shipToMove.id} moved successfully!"
                                    selectedShipForFortify = null
                                    isInFortifyMode = false

                                    if (!isGameOver) {
                                        currentPlayer = if (currentPlayer == P1) P2 else P1
                                    }

                                } else {
                                    fortifyStatusMessage =
                                        "Cannot move ${shipToMove.id} to ($targetAnchorRow, $targetAnchorCol). $validationMessage"
                                }
                            }
                        }
                    } else {
                        fortifyStatusMessage =
                            "This is your own grid. Enter Fortify mode to move ships, or attack on the opponent's grid."
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (showRulesDialog) {
            AlertDialog(
                onDismissRequest = { showRulesDialog = false },
                title = { Text("Battle Command - Rules") },
                text = {
                    Column {
                        Text(stringResource(R.string.game_rules))
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { showRulesDialog = false }
                    ) {
                        Text("OK")
                    }
                }
            )
        }

        if (showDescriptionDialog) {
            AlertDialog(
                onDismissRequest = { showDescriptionDialog = false },
                title = { Text("About Battle Command") },
                text = {
                    Column {
                        Text(stringResource(R.string.game_desc))
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { showDescriptionDialog = false }
                    ) {
                        Text("OK")
                    }
                }
            )
        }

        if (showGameOverDialog) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("Game Over!") },
                text = { Text(gameStatusMessage) },
                confirmButton = {
                    Button(
                        onClick = {
                            showGameOverDialog = false
                            resetGame()
                        },
                        shape = RoundedCornerShape(percent = 50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.defaultMinSize(minHeight = 40.dp)
                    ) {
                        Text("Play Again", fontSize = 14.sp)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showGameOverDialog = false
                        }
                    ) {
                        Text("Close")
                    }
                }
            )
        }
    }
}

@Composable
fun GameBoard(
    boardData: List<List<CellState>>,
    onCellClick: (row: Int, col: Int) -> Unit,
    isOpponentBoard: Boolean,
    isInFortifyMode: Boolean = false,
    cellSize: androidx.compose.ui.unit.Dp = 35.dp
) {
    Column {
        boardData.forEachIndexed { rowIndex, rowList ->
            Row {
                rowList.forEachIndexed { colIndex, cellState ->
                    GridCell(
                        state = cellState,
                        isOpponentCell = isOpponentBoard,
                        isInFortifyModeForOwnBoard = if (!isOpponentBoard) isInFortifyMode else false,
                        onClick = { onCellClick(rowIndex, colIndex) },
                        size = cellSize
                    )
                }
            }
        }
    }
}

@Composable
fun GridCell(
    state: CellState,
    isOpponentCell: Boolean,
    isInFortifyModeForOwnBoard: Boolean,
    onClick: () -> Unit,
    size: androidx.compose.ui.unit.Dp
) {
    val yourShipColor = Color.Gray

    val backgroundColor = when {
        isOpponentCell && state == CellState.SHIP -> MaterialTheme.colorScheme.surfaceVariant
        !isOpponentCell && state == CellState.SHIP -> {
            if (isInFortifyModeForOwnBoard) yourShipColor else MaterialTheme.colorScheme.surfaceVariant
        }

        state == CellState.EMPTY -> MaterialTheme.colorScheme.surfaceVariant
        state == CellState.HIT -> MaterialTheme.colorScheme.tertiary
        state == CellState.MISS -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val markerText: String?
    val markerColor: Color

    when (state) {
        CellState.HIT -> {
            markerText = "X"
            markerColor = MaterialTheme.colorScheme.onTertiary
        }

        CellState.MISS -> {
            markerText = "•"
            markerColor = MaterialTheme.colorScheme.onPrimary
        }

        else -> {
            markerText = null
            markerColor = Color.Unspecified
        }
    }

    Box(
        modifier = Modifier
            .size(size)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
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
