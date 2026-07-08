package com.example.seabattle.presentation.state

import com.example.seabattle.data.entity.game.AvatarItem

data class ProfileUserState(
    val uid: String? = null,
    val nickname: String = "",
    val avatarItem: AvatarItem? = null,
    val avatarList: List<AvatarItem> = emptyList(),
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val isShowAvatarSelector: Boolean = false,
    val isEditingNickname: Boolean = false,
    val newNicknameField: String = "",
)