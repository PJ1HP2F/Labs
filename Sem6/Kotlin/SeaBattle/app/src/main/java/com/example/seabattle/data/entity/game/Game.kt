package com.example.seabattle.data.entity.game

data class Game(
    val gameId: String = "",
    val hostId: String = "",
    val guestId: String? = null,
    val hostReady: Boolean = false,
    val guestReady: Boolean = false,
    val status: GameStatus = GameStatus.WAITING,
    val currentTurnId: String? = null,
    val winnerId: String? = null,
    val hostCells: List<CellStatus> = List(ConfigGame.BOARD_SIZE * ConfigGame.BOARD_SIZE) { CellStatus.EMPTY },
    val guestCells: List<CellStatus> = List(ConfigGame.BOARD_SIZE * ConfigGame.BOARD_SIZE) { CellStatus.EMPTY },
)
