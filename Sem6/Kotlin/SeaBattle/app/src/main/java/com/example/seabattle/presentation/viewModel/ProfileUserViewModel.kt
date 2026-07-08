package com.example.seabattle.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seabattle.R
import com.example.seabattle.data.entity.game.AvatarItem
import com.example.seabattle.data.repository.AuthRepository
import com.example.seabattle.data.repository.UserRepository
import com.example.seabattle.presentation.action.ProfileUserAction
import com.example.seabattle.presentation.state.ProfileUserState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProfileUserViewModel(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
): ViewModel() {
    private val _state = MutableStateFlow(ProfileUserState())
    val state = _state.asStateFlow()

    private val _error = MutableSharedFlow<String>()
    val error: SharedFlow<String> = _error.asSharedFlow()

    private val _success = MutableSharedFlow<String>()
    val success: SharedFlow<String> = _success.asSharedFlow()

    init {
        _state.update {
            it.copy(
                avatarList = listOf(
                    AvatarItem(R.drawable.avatar_1, "avatar_1"),
                    AvatarItem(R.drawable.avatar_2, "avatar_2"),
                    AvatarItem(R.drawable.avatar_3, "avatar_3")
                ),
                avatarItem = AvatarItem(
                    resId = R.drawable.avatar_1,
                    name = "avatar_1"
                )
            )
        }

        loadProfile()
    }

    fun onAction(action: ProfileUserAction) {
        when (action) {
            ProfileUserAction.UpdateNickname -> {
                updateNickname(_state.value.newNicknameField)
            }

            is ProfileUserAction.UpdateAvatar -> {
                updateAvatar(action.avatarName)
            }

            ProfileUserAction.Logout -> {
                authRepository.signOut()
            }

            ProfileUserAction.ToggleAvatarSelector -> {
                toggleAvatarState()
            }

            ProfileUserAction.ToggleEditingNickname -> {
                toggleEditNickname()
            }

            is ProfileUserAction.UpdateNicknameField -> {
                updateNicknameState(action.newNickname)
            }
        }
    }

    private fun toggleAvatarState() {
        _state.update {
            it.copy(
                isShowAvatarSelector = !it.isShowAvatarSelector
            )
        }
    }

    private fun toggleEditNickname() {
        _state.update {
            it.copy(
                isEditingNickname = !it.isEditingNickname
            )
        }
    }

    private fun updateNicknameState(newNickname: String) {
        _state.update {
            it.copy(
                newNicknameField = newNickname
            )
        }
    }

    private fun getCurrentUid(): String? = authRepository.getCurrentUser()?.uid

    private fun loadProfile() {
        _state.update {
            it.copy(isLoading = true)
        }

        val uid = getCurrentUid() ?: run {
            viewModelScope.launch {
                _error.emit("Пользователь не авторизован")
            }
            return
        }

        viewModelScope.launch {
            try {
                userRepository.getUserProfile(uid)?.let { profileUser ->
                    val avatarName = profileUser.avatarName ?: "avatar_1"

                    _state.update {
                        it.copy(
                            uid = profileUser.uid,
                            nickname = profileUser.nickname,
                            avatarItem = AvatarItem(
                                resId = mapAvatarNameToResId(avatarName),
                                name = avatarName
                            ),
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _error.emit("Ошибка загрузки профиля: ${e.message}")
                _state.update {
                    it.copy(isLoading = false)
                }
            }
        }
    }

    private fun updateNickname(newNickname: String) {
        val uid = _state.value.uid ?: run {
            viewModelScope.launch {
                _error.emit("Пользователь не авторизован")
            }
            return
        }
        if (newNickname.isBlank()) {
            viewModelScope.launch {
                _error.emit("Никнейм не может быть пустым")
            }
            return
        }
        _state.update { it.copy(isUpdating = true) }
        viewModelScope.launch {
            try {
                userRepository.updateNickname(uid, newNickname)
                _state.update { state ->
                    state.copy(
                        nickname = newNickname,
                        isUpdating = false
                    )
                }
                _success.emit("Никнейм успешно обновлён")
            } catch (e: Exception) {
                _error.emit("Ошибка обновления никнейма: ${e.message}")
                _state.update {
                    it.copy(isUpdating = false)
                }
            }
        }
    }

    private fun updateAvatar(avatarName: String) {
        val uid = _state.value.uid ?: run {
            viewModelScope.launch {
                _error.emit("Пользователь не авторизован")
            }
            return
        }
        _state.update { it.copy(isUpdating = true) }
        viewModelScope.launch {
            try {
                userRepository.updateAvatarName(uid, avatarName)
                _state.update { state ->
                    state.copy(
                        avatarItem = AvatarItem(
                            resId = mapAvatarNameToResId(avatarName),
                            name = avatarName
                        ),
                        isUpdating = false
                    )
                }
                _success.emit("Аватар успешно обновлён")
            } catch (e: Exception) {
                _error.emit("Ошибка обновления аватара: ${e.message}")
                _state.update {
                    it.copy(isUpdating = false)
                }
            }
        }
    }

    private fun mapAvatarNameToResId(name: String): Int = when (name) {
        "avatar_1" -> R.drawable.avatar_1
        "avatar_2" -> R.drawable.avatar_2
        "avatar_3" -> R.drawable.avatar_3
        else -> R.drawable.avatar_1
    }
}