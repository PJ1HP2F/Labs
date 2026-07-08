package com.example.seabattle.presentation.action

sealed interface ProfileUserAction {
    object UpdateNickname : ProfileUserAction
    data class UpdateAvatar(val avatarName: String) : ProfileUserAction
    object ToggleAvatarSelector: ProfileUserAction
    object Logout : ProfileUserAction
    object ToggleEditingNickname: ProfileUserAction
    data class UpdateNicknameField(val newNickname: String): ProfileUserAction
}