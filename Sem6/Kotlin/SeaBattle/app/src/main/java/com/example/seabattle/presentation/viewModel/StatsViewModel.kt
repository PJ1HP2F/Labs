package com.example.seabattle.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seabattle.data.repository.AuthRepository
import com.example.seabattle.data.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.seabattle.data.entity.game.Game

data class StatsState(
    val totalGames: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    val previousGames: List<Game> = emptyList(),
    val currentUserId: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
)

class StatsViewModel(
    private val gameRepository: GameRepository,
    private val authRepository: AuthRepository
): ViewModel() {

    private val _stats = MutableStateFlow(StatsState())
    val stats = _stats.asStateFlow()

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            _stats.update { it.copy(isLoading = true, error = null) }
            try {
                val userId = authRepository.getCurrentUser()?.uid
                if (userId == null) {
                    _stats.update { it.copy(isLoading = false, error = "Пользователь не авторизован") }
                    return@launch
                }

                val games = gameRepository.getGames().filter { 
                    it.hostId == userId || it.guestId == userId 
                }
                
                val totalGames = games.size
                val wins = games.count { it.winnerId == userId }
                val losses = totalGames - wins
                
                _stats.update { 
                    it.copy(
                        totalGames = totalGames,
                        wins = wins,
                        losses = losses,
                        previousGames = games,
                        currentUserId = userId,
                        isLoading = false
                    ) 
                }
            } catch (e: Exception) {
                _stats.update { it.copy(isLoading = false, error = e.message ?: "Ошибка загрузки") }
            }
        }
    }
}
