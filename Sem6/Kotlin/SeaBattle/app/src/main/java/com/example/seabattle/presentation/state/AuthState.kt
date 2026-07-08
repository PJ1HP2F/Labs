package com.example.seabattle.presentation.state

import com.example.seabattle.data.entity.game.AuthStatus
import com.google.firebase.auth.FirebaseUser

data class AuthState(
    val statusSingIn: AuthStatus = AuthStatus.UNAUTHENTICATED,
    val userFirebase: FirebaseUser? = null,
    val emailInput: String = "",
    val passwortInput: String = "",
    val nicknameInput: String = "",
    val registerMode: Boolean = false,
)
