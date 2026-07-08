package com.example.seabattle.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seabattle.data.repository.AuthRepository
import com.example.seabattle.data.repository.GameRepository
import com.example.seabattle.presentation.action.LobbyAction
import com.example.seabattle.presentation.state.LobbyState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LobbyViewModel(
    private val gameRepository: GameRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val gameIdRegex = Regex("[^A-Za-z0-9]")
    private val gameIdLength = 4

    private val _state = MutableStateFlow(LobbyState())
    val state = _state.asStateFlow()

    private val _error = MutableSharedFlow<String>()
    val error: SharedFlow<String> = _error.asSharedFlow()

    private val _success = MutableSharedFlow<String>()
    val success: SharedFlow<String> = _success.asSharedFlow()

    fun onAction(action: LobbyAction) {
        when (action) {
            is LobbyAction.CreateGame -> createGame()
            is LobbyAction.JoinToGame -> joinGame(action.gameId)
            is LobbyAction.UpdateGameIdInput -> updateGameIdInput(action.gameId)
            is LobbyAction.CopyGameId -> copyGameId()
            LobbyAction.ResetNavigation -> resetNavigation()
        }
    }

    private fun createGame() {
        viewModelScope.launch {
            val hostId = authRepository.getCurrentUser()?.uid
            if (hostId == null) {
                _error.emit("Пользователь не авторизован")
                return@launch
            }
            try {
                val gameId = gameRepository.createGame(hostId)
                _state.update { it.copy(createdGameId = gameId, isLoading = false) }

                _state.update {
                    it.copy(
                        navigateToPlacement = true,
                        isHost = true,
                        gameIdForPlacement = gameId
                    )
                }
            } catch (e: Exception) {
                _error.emit("Ошибка создания игры: ${e.message}")
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun joinGame(gameId: String) {
        val normalizedGameId = normalizeGameId(gameId)
        if (normalizedGameId.isBlank()) {
            viewModelScope.launch {
                _error.emit("Введите ID игры")
            }
            return
        }
        viewModelScope.launch {
            val guestId = authRepository.getCurrentUser()?.uid
            if (guestId == null) {
                _error.emit("Пользователь не авторизован")
                return@launch
            }
            try {
                val success = gameRepository.joinAsGuest(normalizedGameId, guestId)
                if (success) {
                    _success.emit("Присоединение успешно!")
                    _state.update {
                        it.copy(
                            navigateToPlacement = true,
                            isHost = false,
                            gameIdForPlacement = normalizedGameId
                        )
                    }
                } else {
                    _error.emit("Не удалось присоединиться: игра не найдена или уже начата")
                }
            } catch (e: Exception) {
                _error.emit("Ошибка присоединения: ${e.message}")
            }
        }
    }

    private fun updateGameIdInput(gameId: String) {
        _state.update { it.copy(gameIdInput = normalizeGameId(gameId)) }
    }

    private fun normalizeGameId(gameId: String): String {
        return gameId
            .uppercase()
            .replace(gameIdRegex, "")
            .take(gameIdLength)
    }

    private fun copyGameId() {
        val id = _state.value.createdGameId
        if (id != null) {
            _state.update {
                it.copy(
                    gameIdToCopy = id
                )
            }
        }
    }

    private fun resetNavigation() {
        _state.update {
            it.copy(
                navigateToPlacement = false,
                gameIdForPlacement = null,
                isHost = false
            )
        }
    }
}