package com.example.seabattle.presentation.action

sealed interface AuthAction {
    object SignUpWithEmail: AuthAction
    object SignInWithEmail: AuthAction
    object SignOut: AuthAction
    data class UpdateEmailField(val email: String): AuthAction
    data class UpdatePasswordField(val password: String): AuthAction
    data class UpdateNicknameField(val nickname: String): AuthAction
    data class ToggleLoginRegisterMode(val newMode: Boolean): AuthAction
}