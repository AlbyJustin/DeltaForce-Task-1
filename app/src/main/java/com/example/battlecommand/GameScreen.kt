package com.example.battlecommand

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import com.example.battlecommand.model.CellState
import com.example.battlecommand.model.InitialShips.initialP1ShipConfigs
import com.example.battlecommand.model.InitialShips.initialP2ShipConfigs
import com.example.battlecommand.model.Ship
import com.example.battlecommand.ui.theme.GridBlueBackground
import com.example.battlecommand.ui.theme.GridOrangeBackground

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
fun GameScreen(modifier: Modifier = Modifier) {
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
        fortifyStatusMessage = "Attack Mode"

        println("Game Reset!")
        showGameOverDialog = false
    }

    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .background(GridBlueBackground)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("P1 Wins: $player1Wins", style = MaterialTheme.typography.titleMedium)
                Text("P2 Wins: $player2Wins", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(4.dp))

            Text("Turn: $currentPlayer", style = MaterialTheme.typography.headlineSmall)
            if (isGameOver && !showGameOverDialog) {
                Text(
                    gameStatusMessage,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Player 2's board
            Text(
                "Player 2's Grid",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            GameBoard(
                boardData = player2Board.value,
                isOpponentBoard = (currentPlayer == P1),
                isInFortifyMode = (currentPlayer == P2 && isInFortifyMode),
                onCellClick = { row, col ->
                    if (isGameOver) return@GameBoard

                    if (currentPlayer == P1) {
                        val attackersViewOfP2Board = player2Board
                        val defendersRealP2Board = player2Board
                        val defendersP2Ships = player2Ships

                        val clickedCellStateOnAttackersView = attackersViewOfP2Board.value[row][col]

                        if (clickedCellStateOnAttackersView == CellState.HIT || clickedCellStateOnAttackersView == CellState.MISS) {
                            println("P1 already attacked ($row, $col) on P2's board.")
                            fortifyStatusMessage = "Already attacked this cell."
                            return@GameBoard
                        }

                        var actualStateOnDefendersBoard = CellState.EMPTY
                        val shipAtTarget =
                            defendersP2Ships.find { it.coordinates.contains(Pair(row, col)) }

                        if (shipAtTarget != null) {
                            if (defendersRealP2Board.value[row][col] != CellState.HIT) {
                                actualStateOnDefendersBoard = CellState.SHIP
                            } else {
                                actualStateOnDefendersBoard = CellState.HIT
                            }
                        } else {
                            actualStateOnDefendersBoard = defendersRealP2Board.value[row][col]
                        }

                        val newAttackersViewBoardMutable =
                            attackersViewOfP2Board.value.map { it.toMutableList() }.toMutableList()
                        val newDefendersRealBoardMutable =
                            defendersRealP2Board.value.map { it.toMutableList() }.toMutableList()
                        var message = "(P1 attacking P2) "
                        var validMoveMade = false

                        when (actualStateOnDefendersBoard) {
                            CellState.EMPTY -> {
                                newAttackersViewBoardMutable[row][col] = CellState.MISS
                                newDefendersRealBoardMutable[row][col] = CellState.MISS
                                message += "MISS at ($row, $col)"
                                validMoveMade = true
                            }

                            CellState.SHIP -> {
                                newAttackersViewBoardMutable[row][col] = CellState.HIT
                                newDefendersRealBoardMutable[row][col] = CellState.HIT

                                val targetShipObject = shipAtTarget!!
                                targetShipObject.hits += 1
                                message += "HIT ${targetShipObject.id} at ($row, $col)!"
                                if (targetShipObject.isSunk()) {
                                    message += " ${targetShipObject.id} SUNK!"
                                    if (defendersP2Ships.all { it.isSunk() }) {
                                        isGameOver = true
                                        gameStatusMessage = "GAME OVER! $P1 Wins!"
                                        showGameOverDialog = true
                                        recordWin(P1)
                                    }
                                }
                                validMoveMade = true
                            }

                            CellState.HIT, CellState.MISS -> {
                                println("Error: P1 attacked an already revealed HIT/MISS cell on P2's board: ($row, $col)")
                                return@GameBoard
                            }
                        }

                        if (validMoveMade) {
                            attackersViewOfP2Board.value =
                                newAttackersViewBoardMutable.map { it.toList() }
                            fortifyStatusMessage = message
                            println(message)
                            if (!isGameOver) {
                                currentPlayer = P2
                            }
                        }
                    } else {
                        if (isInFortifyMode) {
                            val p2OwnBoard = player2Board
                            val p2OwnShips = player2Ships

                            fortifyStatusMessage = ""
                            val shipAtClickedCellOnP2Board =
                                p2OwnShips.find { ship ->
                                    ship.coordinates.contains(
                                        Pair(
                                            row,
                                            col
                                        )
                                    )
                                }

                            if (selectedShipForFortify == null) {
                                if (shipAtClickedCellOnP2Board != null) {
                                    if (shipAtClickedCellOnP2Board.hits == 0) {
                                        selectedShipForFortify = shipAtClickedCellOnP2Board
                                        fortifyStatusMessage =
                                            "(P2 Fortifying) ${shipAtClickedCellOnP2Board.id} selected. Click destination."
                                    } else {
                                        fortifyStatusMessage =
                                            "(P2 Fortifying) ${shipAtClickedCellOnP2Board.id} is damaged."
                                    }
                                } else {
                                    fortifyStatusMessage =
                                        "(P2 Fortifying) Click one of your undamaged ships."
                                }
                            } else {
                                val shipToMove = selectedShipForFortify!!
                                val targetAnchorRow = row
                                val targetAnchorCol = col

                                if (shipAtClickedCellOnP2Board == shipToMove) {
                                    selectedShipForFortify = null
                                    fortifyStatusMessage = "(P2 Fortifying) Ship deselected."
                                } else if (shipAtClickedCellOnP2Board != null && shipAtClickedCellOnP2Board != shipToMove) {
                                    fortifyStatusMessage =
                                        "(P2 Fortifying) ${shipToMove.id} selected. Click destination or deselect."
                                } else {

                                    val newProposedCoordinates = mutableListOf<Pair<Int, Int>>()
                                    for (i in 0 until shipToMove.size) {
                                        if (shipToMove.isVertical) newProposedCoordinates.add(
                                            Pair(
                                                targetAnchorRow + i,
                                                targetAnchorCol
                                            )
                                        )
                                        else newProposedCoordinates.add(
                                            Pair(
                                                targetAnchorRow,
                                                targetAnchorCol + i
                                            )
                                        )
                                    }

                                    var isValidMove = true
                                    var validationMessage = ""
                                    for ((r, c) in newProposedCoordinates) {
                                        if (r !in 0 until gridSize || c !in 0 until gridSize) {
                                            isValidMove = false; validationMessage =
                                                "Out of bounds."; break
                                        }
                                    }
                                    if (isValidMove) {
                                        for ((r, c) in newProposedCoordinates) {
                                            if (p2OwnBoard.value[r][c] == CellState.MISS || p2OwnBoard.value[r][c] == CellState.HIT) {
                                                isValidMove = false; validationMessage =
                                                    "Cannot move to MISS/HIT."; break
                                            }
                                        }
                                    }

                                    if (isValidMove) {
                                        for (otherShip in p2OwnShips) {
                                            if (otherShip.id == shipToMove.id) continue; if (newProposedCoordinates.any {
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
                                            p2OwnBoard.value.map { it.toMutableList() }
                                                .toMutableList()
                                        shipToMove.coordinates.forEach { (r_old, c_old) ->
                                            if (r_old < gridSize && c_old < gridSize) currentBoardMutable[r_old][c_old] =
                                                CellState.EMPTY
                                        }
                                        val shipIndex =
                                            p2OwnShips.indexOfFirst { it.id == shipToMove.id }
                                        if (shipIndex != -1) p2OwnShips[shipIndex] =
                                            shipToMove.copy(coordinates = newProposedCoordinates.toList())
                                        else {
                                            fortifyStatusMessage =
                                                "(P2 Fortify) Error: Ship not found."; return@GameBoard
                                        }
                                        newProposedCoordinates.forEach { (r_new, c_new) ->
                                            if (r_new < gridSize && c_new < gridSize) currentBoardMutable[r_new][c_new] =
                                                CellState.SHIP
                                        }
                                        p2OwnBoard.value = currentBoardMutable.map { it.toList() }
                                        fortifyStatusMessage =
                                            "(P2 Fortifying) ${shipToMove.id} moved!"
                                        selectedShipForFortify = null
                                        isInFortifyMode = false
                                        if (!isGameOver) {
                                            currentPlayer = P1
                                        }
                                    } else {
                                        fortifyStatusMessage =
                                            "(P2 Fortifying) Cannot move. $validationMessage"
                                    }
                                }
                            }
                        } else {
                            fortifyStatusMessage =
                                "Player 2: Click P1's grid to attack or enter Fortify mode."
                        }
                    }
                },
                cellSpacing = 2.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(fortifyStatusMessage, color = MaterialTheme.colorScheme.secondary)
            Button( // Fortify Button
                onClick = {
                    isInFortifyMode = !isInFortifyMode
                    selectedShipForFortify = null
                    fortifyStatusMessage =
                        if (isInFortifyMode) "Fortify Mode: Select undamaged ship." else "Attack Mode"
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
                    text = if (isInFortifyMode) "Cancel Fortify" else "Fortify",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
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
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(GridOrangeBackground)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            // Player 1's board
            Text("Player 1's Grid", style = MaterialTheme.typography.titleMedium, color = Color.Black)
            GameBoard(
                boardData = player1Board.value,
                isOpponentBoard = (currentPlayer == P2),
                isInFortifyMode = (currentPlayer == P1 && isInFortifyMode),
                onCellClick = { row, col ->
                    if (isGameOver) return@GameBoard

                    if (currentPlayer == P2) {
                        val attackersViewOfP1Board = player1Board
                        val defendersRealP1Board = player1Board
                        val defendersP1Ships = player1Ships

                        val clickedCellStateOnAttackersView = attackersViewOfP1Board.value[row][col]

                        if (clickedCellStateOnAttackersView == CellState.HIT || clickedCellStateOnAttackersView == CellState.MISS) {
                            println("P2 already attacked ($row, $col) on P1's board.")
                            fortifyStatusMessage = "Already attacked this cell."
                            return@GameBoard
                        }

                        var actualStateOnDefendersBoard = CellState.EMPTY
                        val shipAtTarget =
                            defendersP1Ships.find { it.coordinates.contains(Pair(row, col)) }

                        if (shipAtTarget != null) {
                            if (defendersRealP1Board.value[row][col] != CellState.HIT) {
                                actualStateOnDefendersBoard = CellState.SHIP
                            } else {
                                actualStateOnDefendersBoard = CellState.HIT
                            }
                        } else {
                            actualStateOnDefendersBoard = defendersRealP1Board.value[row][col]
                        }

                        val newAttackersViewBoardMutable =
                            attackersViewOfP1Board.value.map { it.toMutableList() }.toMutableList()
                        val newDefendersRealBoardMutable =
                            defendersRealP1Board.value.map { it.toMutableList() }.toMutableList()
                        var message = "(P2 attacking P1) "
                        var validMoveMade = false

                        when (actualStateOnDefendersBoard) {
                            CellState.EMPTY -> {
                                newAttackersViewBoardMutable[row][col] = CellState.MISS
                                newDefendersRealBoardMutable[row][col] = CellState.MISS
                                message += "MISS at ($row, $col)"
                                validMoveMade = true
                            }

                            CellState.SHIP -> {
                                newAttackersViewBoardMutable[row][col] = CellState.HIT
                                newDefendersRealBoardMutable[row][col] = CellState.HIT

                                val targetShipObject = shipAtTarget!!
                                targetShipObject.hits += 1
                                message += "HIT ${targetShipObject.id} at ($row, $col)!"
                                if (targetShipObject.isSunk()) {
                                    message += " ${targetShipObject.id} SUNK!"
                                    if (defendersP1Ships.all { it.isSunk() }) {
                                        isGameOver = true
                                        gameStatusMessage = "GAME OVER! $P2 Wins!"
                                        showGameOverDialog = true
                                        recordWin(P2)
                                    }
                                }
                                validMoveMade = true
                            }

                            CellState.HIT, CellState.MISS -> {
                                println("Error: P2 attacked an already revealed HIT/MISS cell on P1's board: ($row, $col)")
                                return@GameBoard
                            }
                        }

                        if (validMoveMade) {
                            attackersViewOfP1Board.value =
                                newAttackersViewBoardMutable.map { it.toList() }
                            fortifyStatusMessage = message
                            println(message)
                            if (!isGameOver) {
                                currentPlayer = P1
                            }
                        }

                    } else {
                        if (isInFortifyMode) {
                            val p1OwnBoard = player1Board
                            val p1OwnShips = player1Ships

                            fortifyStatusMessage = ""
                            val shipAtClickedCellOnP1Board =
                                p1OwnShips.find { ship ->
                                    ship.coordinates.contains(
                                        Pair(
                                            row,
                                            col
                                        )
                                    )
                                }

                            if (selectedShipForFortify == null) {
                                if (shipAtClickedCellOnP1Board != null) {
                                    if (shipAtClickedCellOnP1Board.hits == 0) {
                                        selectedShipForFortify = shipAtClickedCellOnP1Board
                                        fortifyStatusMessage =
                                            "(P1 Fortifying) ${shipAtClickedCellOnP1Board.id} selected. Click destination."
                                    } else {
                                        fortifyStatusMessage =
                                            "(P1 Fortifying) ${shipAtClickedCellOnP1Board.id} is damaged."
                                    }
                                } else {
                                    fortifyStatusMessage =
                                        "(P1 Fortifying) Click one of your undamaged ships."
                                }
                            } else {
                                val shipToMove = selectedShipForFortify!!
                                val targetAnchorRow = row
                                val targetAnchorCol = col

                                if (shipAtClickedCellOnP1Board == shipToMove) {
                                    selectedShipForFortify = null
                                    fortifyStatusMessage = "(P1 Fortifying) Ship deselected."
                                } else if (shipAtClickedCellOnP1Board != null && shipAtClickedCellOnP1Board != shipToMove) {
                                    fortifyStatusMessage =
                                        "(P1 Fortifying) ${shipToMove.id} selected. Click destination or deselect."
                                } else {
                                    val newProposedCoordinates = mutableListOf<Pair<Int, Int>>()
                                    for (i in 0 until shipToMove.size) {
                                        if (shipToMove.isVertical) newProposedCoordinates.add(
                                            Pair(
                                                targetAnchorRow + i,
                                                targetAnchorCol
                                            )
                                        )
                                        else newProposedCoordinates.add(
                                            Pair(
                                                targetAnchorRow,
                                                targetAnchorCol + i
                                            )
                                        )
                                    }

                                    var isValidMove = true
                                    var validationMessage = ""
                                    for ((r, c) in newProposedCoordinates) {
                                        if (r !in 0 until gridSize || c !in 0 until gridSize) {
                                            isValidMove = false; validationMessage =
                                                "Out of bounds."; break
                                        }
                                    }
                                    if (isValidMove) {
                                        for ((r, c) in newProposedCoordinates) {
                                            if (p1OwnBoard.value[r][c] == CellState.MISS || p1OwnBoard.value[r][c] == CellState.HIT) {
                                                isValidMove = false; validationMessage =
                                                    "Cannot move to MISS/HIT."; break
                                            }
                                        }
                                    }
                                    if (isValidMove) {
                                        for (otherShip in p1OwnShips) {
                                            if (otherShip.id == shipToMove.id) continue; if (newProposedCoordinates.any {
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
                                            p1OwnBoard.value.map { it.toMutableList() }
                                                .toMutableList()
                                        shipToMove.coordinates.forEach { (r_old, c_old) ->
                                            if (r_old < gridSize && c_old < gridSize) currentBoardMutable[r_old][c_old] =
                                                CellState.EMPTY
                                        }
                                        val shipIndex =
                                            p1OwnShips.indexOfFirst { it.id == shipToMove.id }
                                        if (shipIndex != -1) p1OwnShips[shipIndex] =
                                            shipToMove.copy(coordinates = newProposedCoordinates.toList())
                                        else {
                                            fortifyStatusMessage =
                                                "(P1 Fortify) Error: Ship not found."; return@GameBoard
                                        }
                                        newProposedCoordinates.forEach { (r_new, c_new) ->
                                            if (r_new < gridSize && c_new < gridSize) currentBoardMutable[r_new][c_new] =
                                                CellState.SHIP
                                        }
                                        p1OwnBoard.value = currentBoardMutable.map { it.toList() }
                                        fortifyStatusMessage =
                                            "(P1 Fortifying) ${shipToMove.id} moved!"
                                        selectedShipForFortify = null
                                        isInFortifyMode = false
                                        if (!isGameOver) {
                                            currentPlayer = P2
                                        }
                                    } else {
                                        fortifyStatusMessage =
                                            "(P1 Fortifying) Cannot move. $validationMessage"
                                    }
                                }
                            }
                        } else {
                            fortifyStatusMessage = "Player 1: Attack P2's grid or select Fortify."
                        }
                    }
                },
                cellSpacing = 2.dp,
            )
        }
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