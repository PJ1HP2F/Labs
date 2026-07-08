package com.example.seabattle.presentation.state

import com.example.seabattle.data.entity.game.Game

data class ActiveGamesState(
    val currentUserId: String? = null,
    val activeGamesList: List<Game> = emptyList(),
)