package com.example.seabattle.presentation.action

sealed interface GameAction {
    data class Init(val gameId: String) : GameAction
    data class StartListening(val gameId: String, val currentUserId: String) : GameAction
    data class CreateGame(val hostId: String) : GameAction
    data class JoinGame(val gameId: String, val guestId: String) : GameAction
    data class MakeMove(val x: Int, val y: Int) : GameAction
    object StopListening : GameAction
}