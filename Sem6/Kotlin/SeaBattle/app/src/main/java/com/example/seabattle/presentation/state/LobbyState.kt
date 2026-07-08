package com.example.seabattle.presentation.state

data class LobbyState(
    val gameIdInput: String = "",
    val createdGameId: String? = null,
    val isLoading: Boolean = false,
    val navigateToPlacement: Boolean = false,
    val gameIdForPlacement: String? = null,
    val isHost: Boolean = false,
    val gameIdToCopy: String? = null
)