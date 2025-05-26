package com.example.battlecommand.model

object InitialShips {
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
}