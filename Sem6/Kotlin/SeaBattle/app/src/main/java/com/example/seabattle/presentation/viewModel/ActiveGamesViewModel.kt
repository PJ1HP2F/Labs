package com.example.seabattle.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seabattle.data.repository.AuthRepository
import com.example.seabattle.data.repository.GameRepository
import com.example.seabattle.presentation.state.ActiveGamesState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ActiveGamesViewModel(
    private val gameRepository: GameRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ActiveGamesState())
    val state = _state.asStateFlow()

    init {
        loadActiveGames()
    }

    private fun loadActiveGames() {
        viewModelScope.launch {
            val userId = getCurrentUserId() ?: return@launch
            _state.update {
                it.copy(
                    activeGamesList = gameRepository.getActiveGamesForUser(userId),
                    currentUserId = userId
                )
            }
        }
    }

    private fun getCurrentUserId(): String? = authRepository.getCurrentUser()?.uid
}