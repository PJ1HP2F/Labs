package com.example.seabattle.presentation.action

sealed interface LobbyAction {
    object CreateGame: LobbyAction
    data class JoinToGame(val gameId: String): LobbyAction
    data class UpdateGameIdInput(val gameId: String) : LobbyAction
    object CopyGameId : LobbyAction
    object ResetNavigation : LobbyAction
}