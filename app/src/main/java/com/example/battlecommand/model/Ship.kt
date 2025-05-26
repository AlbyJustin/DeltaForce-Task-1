package com.example.battlecommand.model

data class Ship(val id: String,
                val size: Int,
                val coordinates: List<Pair<Int, Int>>,
                var hits: Int = 0,
                val isVertical: Boolean
) {
    fun isSunk(): Boolean = hits >= size
}
