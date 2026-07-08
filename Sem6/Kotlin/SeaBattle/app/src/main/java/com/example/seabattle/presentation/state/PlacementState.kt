package com.example.seabattle.presentation.state

import com.example.seabattle.data.entity.game.CellStatus
import com.example.seabattle.data.entity.game.PlacedShip

data class PlacementState(
    val board: List<CellStatus> = List(100) { CellStatus.EMPTY },
    val remainingShips: Map<Int, Int> = mapOf(1 to 4, 2 to 3, 3 to 2, 4 to 1),
    val selectedShipSize: Int? = null,
    val isHorizontal: Boolean = true,
    val placedShips: List<PlacedShip> = emptyList(),
    val ready: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val gameId: String = "",
    val isHost: Boolean = false
)

