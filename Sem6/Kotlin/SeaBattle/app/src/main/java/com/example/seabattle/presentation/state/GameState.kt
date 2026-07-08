package com.example.seabattle.presentation.state

import com.example.seabattle.data.entity.game.CellStatus
import com.example.seabattle.data.entity.game.ConfigGame
import com.example.seabattle.data.entity.game.GameStatus

data class GameState(
    val gameId: String? = null,
    val myId: String? = null,
    val hostId: String? = null,
    val guestId: String? = null,
    val status: GameStatus = GameStatus.WAITING,
    val myTurn: Boolean = false,
    val winnerId: String? = null,
    val hostCells: List<CellStatus> = List(ConfigGame.BOARD_SIZE * ConfigGame.BOARD_SIZE) { CellStatus.EMPTY },
    val guestCells: List<CellStatus> = List(ConfigGame.BOARD_SIZE * ConfigGame.BOARD_SIZE) { CellStatus.EMPTY },
)
