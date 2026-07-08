package com.example.seabattle.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seabattle.data.entity.game.GameStatus
import com.example.seabattle.data.entity.game.MoveResult
import com.example.seabattle.data.repository.AuthRepository
import com.example.seabattle.data.repository.GameRepository
import com.example.seabattle.presentation.action.GameAction
import com.example.seabattle.presentation.state.GameState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BattleViewModel(
    private val gameRepository: GameRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _gameState = MutableStateFlow(GameState())
    val gameState = _gameState.asStateFlow()

    private val _error = MutableSharedFlow<String>()
    val error: SharedFlow<String> = _error.asSharedFlow()

    private val _moveResult = MutableSharedFlow<MoveResult>()
    val moveResult: SharedFlow<MoveResult> = _moveResult.asSharedFlow()

    fun onAction(action: GameAction) {
        when (action) {
            is GameAction.Init -> {
                val userId = authRepository.getCurrentUser()?.uid ?: return
                startListening(action.gameId, userId)
            }
            is GameAction.StartListening ->{
                startListening(action.gameId, action.currentUserId)
            }
            is GameAction.CreateGame -> {
                createGame(action.hostId)
            }
            is GameAction.JoinGame -> {
                joinGame(action.gameId, action.guestId)
            }
            is GameAction.MakeMove -> {
                makeMove(action.x, action.y)
            }
            GameAction.StopListening -> {
                stopListening()
            }
        }
    }

    private fun startListening(gameId: String, currentUserId: String) {
        _gameState.update {
            it.copy(
                gameId = gameId,
                myId = currentUserId
            )
        }

        viewModelScope.launch {
            gameRepository.listenGame(gameId).collect { game ->
                game?.let {
                    _gameState.update { state ->
                        state.copy(
                            hostId = it.hostId,
                            guestId = it.guestId,
                            status = it.status,
                            myTurn = it.currentTurnId == state.myId && it.status == GameStatus.ACTIVE,
                            winnerId = it.winnerId,
                            hostCells = it.hostCells,
                            guestCells = it.guestCells
                        )
                    }
                }
            }
        }
    }

    private fun createGame(hostId: String) {
        viewModelScope.launch {
            try {
                val gameId = gameRepository.createGame(hostId)
                startListening(gameId, hostId)
            } catch (e: Exception) {
                _error.emit("Ошибка создания игры: ${e.message}")
            }
        }
    }

    private fun joinGame(gameId: String, guestId: String) {
        viewModelScope.launch {
            try {
                val success = gameRepository.joinAsGuest(gameId, guestId)
                if (success) {
                    startListening(gameId, guestId)
                } else {
                    _error.emit("Не удалось присоединиться: игра уже начата или не существует")
                }
            } catch (e: Exception) {
                _error.emit("Ошибка присоединения: ${e.message}")
            }
        }
    }

    private fun makeMove(x: Int, y: Int) {
        val state = _gameState.value
        val myId = state.myId ?: run {
            viewModelScope.launch {
                _error.emit("Вы не авторизованы")
            }
            return
        }
        if (!state.myTurn) {
            viewModelScope.launch {
                _error.emit("Сейчас не ваш ход")
            }
            return
        }
        if (state.status != GameStatus.ACTIVE) {
            viewModelScope.launch {
                _error.emit("Игра уже закончена")
            }
            return
        }

        val opponentId = if (state.hostId == myId) state.guestId else state.hostId
        if (opponentId == null) {
            viewModelScope.launch {
                _error.emit("Противник не присоединился")
            }
            return
        }

        viewModelScope.launch {
            try {
                val result = gameRepository.makeMove(
                    gameId = state.gameId!!,
                    playerId = myId,
                    targetPlayerId = opponentId,
                    x = x,
                    y = y
                )
                _moveResult.emit(result)
                if (result == MoveResult.INVALID) {
                    _error.emit("Некорректный ход")
                }
            } catch (e: Exception) {
                _error.emit("Ошибка хода: ${e.message}")
            }
        }
    }

    private fun stopListening() {
        _gameState.value = GameState()
    }
}