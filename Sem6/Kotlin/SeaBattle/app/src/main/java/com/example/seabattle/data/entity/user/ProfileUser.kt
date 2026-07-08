package com.example.seabattle.data.entity.user

data class ProfileUser(
    val uid: String = "",
    val nickname: String = "",
    val avatarName: String? = null,
    val countGames: Int = 0,
    val countWins: Int = 0,
)