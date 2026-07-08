package com.example.seabattle.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seabattle.data.entity.game.CellStatus
import com.example.seabattle.data.entity.game.PlacedShip
import com.example.seabattle.data.repository.AuthRepository
import com.example.seabattle.data.repository.GameRepository
import com.example.seabattle.presentation.action.PlacementAction
import com.example.seabattle.presentation.effect.PlacementEffect
import com.example.seabattle.presentation.state.PlacementState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.collections.copy
import kotlin.collections.emptyList
import kotlin.collections.toMutableList
import kotlin.text.set
import kotlin.times

class PlacementViewModel(
    private val gameRepository: GameRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PlacementState())
    val state: StateFlow<PlacementState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<PlacementEffect>()
    val effect: SharedFlow<PlacementEffect> = _effect.asSharedFlow()

    private var gameListener: Job? = null
    private var currentUserId: String = ""

    fun onAction(action: PlacementAction) {
        when (action) {
            is PlacementAction.Init -> init(action.gameId, action.isHost)
            is PlacementAction.SelectShip -> selectShip(action.size)
            PlacementAction.ToggleOrientation -> toggleOrientation()
            is PlacementAction.TryPlaceShip -> tryPlaceShip(action.x, action.y)
            is PlacementAction.PlaceDraggedShip -> placeDraggedShip(action.size, action.x, action.y)
            PlacementAction.RandomPlacement -> randomPlacement()
            PlacementAction.ConfirmPlacement -> confirmPlacement()
            PlacementAction.ClearErrorMessage -> clearErrorMessage()
            PlacementAction.UndoLastShip -> undoLastShip()
            PlacementAction.ClearBoard -> clearBoard()
        }
    }

    private fun init(gameId: String, isHost: Boolean) {
        currentUserId = authRepository.getCurrentUser()?.uid ?: return
        _state.update {
            it.copy(
                gameId = gameId,
                isHost = isHost,
                board = List(100) { CellStatus.EMPTY },
                remainingShips = mapOf(1 to 4, 2 to 3, 3 to 2, 4 to 1),
                placedShips = emptyList(),
                selectedShipSize = null,
                isHorizontal = true,
                ready = false,
                isLoading = false,
                errorMessage = null
            )
        }

        gameListener = viewModelScope.launch {
            gameRepository.listenGame(gameId).collect { game ->
                game?.let {
                    if (it.hostReady && it.guestReady) {
                        _effect.emit(PlacementEffect.NavigateToBattle)
                    }
                }
            }
        }
    }

    private fun selectShip(size: Int) {
        val remaining = _state.value.remainingShips[size] ?: 0
        if (remaining == 0) {
            viewModelScope.launch {
                _effect.emit(PlacementEffect.ShowSnackbar("Корабль уже размещён"))
            }
            return
        }
        _state.update { it.copy(selectedShipSize = size) }
    }

    private fun toggleOrientation() {
        _state.update { it.copy(isHorizontal = !it.isHorizontal) }
    }

    private fun tryPlaceShip(x: Int, y: Int) {
        val shipSize = _state.value.selectedShipSize ?: return
        val currentBoard = _state.value.board.toMutableList()
        val orientation = _state.value.isHorizontal
        val positions = mutableListOf<Int>()
        val shipCellStatus = if (shipSize >= 3) CellStatus.SUBMARINE else CellStatus.SHIP

        for (i in 0 until shipSize) {
            val cellX = if (orientation) x + i else x
            val cellY = if (orientation) y else y + i
            if (cellX !in 0..9 || cellY !in 0..9) {
                viewModelScope.launch {
                    _effect.emit(PlacementEffect.ShowSnackbar("Корабль выходит за границы"))
                }
                return
            }
            val idx = cellY * 10 + cellX
            if (currentBoard[idx] != CellStatus.EMPTY) {
                viewModelScope.launch {
                    _effect.emit(PlacementEffect.ShowSnackbar("Клетка занята"))
                }
                return
            }
            positions.add(idx)
        }

        if (!hasNoAdjacentShips(currentBoard, positions)) {
            viewModelScope.launch {
                _effect.emit(PlacementEffect.ShowSnackbar("Корабли не должны касаться"))
            }
            return
        }

        for (idx in positions) {
            currentBoard[idx] = shipCellStatus
        }

        val newRemaining = _state.value.remainingShips.toMutableMap()
        newRemaining[shipSize] = (newRemaining[shipSize] ?: 0) - 1

        val newPlacedShip = PlacedShip(x, y, shipSize, orientation)

        _state.update {
            it.copy(
                board = currentBoard,
                remainingShips = newRemaining,
                placedShips = it.placedShips + newPlacedShip,
                selectedShipSize = null
            )
        }
    }

    private fun placeDraggedShip(size: Int, x: Int, y: Int) {
        _state.update { it.copy(selectedShipSize = size) }
        tryPlaceShip(x, y)
    }

    private fun randomPlacement() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val newBoard = List(100) { CellStatus.EMPTY }.toMutableList()
            val ships = mutableListOf<PlacedShip>()
            val allShips = mutableListOf(4, 3, 3, 2, 2, 2, 1, 1, 1, 1)
            allShips.shuffle()

            for (size in allShips) {
                var placed = false
                var attempts = 0
                val shipCellStatus = if (size >= 3) CellStatus.SUBMARINE else CellStatus.SHIP
                while (!placed && attempts < 500) {
                    val isHorizontal = kotlin.random.Random.nextBoolean()
                    val maxX = if (isHorizontal) 10 - size else 9
                    val maxY = if (isHorizontal) 9 else 10 - size
                    val x = kotlin.random.Random.nextInt(0, maxX + 1)
                    val y = kotlin.random.Random.nextInt(0, maxY + 1)
                    val positions = mutableListOf<Int>()
                    var valid = true
                    for (i in 0 until size) {
                        val cellX = if (isHorizontal) x + i else x
                        val cellY = if (isHorizontal) y else y + i
                        val idx = cellY * 10 + cellX
                        if (newBoard[idx] != CellStatus.EMPTY) {
                            valid = false
                            break
                        }
                        positions.add(idx)
                    }
                    if (valid && hasNoAdjacentShips(newBoard, positions)) {
                        for (idx in positions) {
                            newBoard[idx] = shipCellStatus
                        }
                        ships.add(PlacedShip(x, y, size, isHorizontal))
                        placed = true
                    }
                    attempts++
                }
                if (!placed) {
                    randomPlacement()
                    return@launch
                }
            }

            _state.update {
                it.copy(
                    board = newBoard,
                    placedShips = ships,
                    remainingShips = mapOf(1 to 0, 2 to 0, 3 to 0, 4 to 0),
                    selectedShipSize = null,
                    isLoading = false
                )
            }
        }
    }

    private fun confirmPlacement() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                if (_state.value.isHost) {
                    gameRepository.updateHostCells(_state.value.gameId, _state.value.board)
                } else {
                    gameRepository.updateGuestCells(_state.value.gameId, _state.value.board)
                }
                _state.update { it.copy(ready = true, isLoading = false) }
                _effect.emit(PlacementEffect.ShowSnackbar("Расстановка сохранена, ожидаем противника"))
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
                _effect.emit(PlacementEffect.ShowSnackbar("Ошибка сохранения: ${e.message}"))
            }
        }
    }

    private fun clearErrorMessage() {
        _state.update { it.copy(errorMessage = null) }
    }

    private fun hasNoAdjacentShips(board: List<CellStatus>, positions: List<Int>): Boolean {
        for (idx in positions) {
            val cx = idx % 10
            val cy = idx / 10

            for (dy in -1..1) {
                for (dx in -1..1) {
                    val nx = cx + dx
                    val ny = cy + dy
                    if (nx in 0..9 && ny in 0..9) {
                        val nIdx = ny * 10 + nx
                        if (board[nIdx] == CellStatus.SHIP && !positions.contains(nIdx)) {
                            return false
                        }
                        if (board[nIdx] == CellStatus.SUBMARINE && !positions.contains(nIdx)) {
                            return false
                        }
                    }
                }
            }
        }
        return true
    }


    private fun undoLastShip() {
        val lastShip = _state.value.placedShips.lastOrNull() ?: return

        val currentBoard = _state.value.board.toMutableList()
        val newPlacedShips = _state.value.placedShips.dropLast(1)
        val newRemaining = _state.value.remainingShips.toMutableMap()

        newRemaining[lastShip.size] = (newRemaining[lastShip.size] ?: 0) + 1

        for (i in 0 until lastShip.size) {
            val cellX = if (lastShip.isHorizontal) lastShip.x + i else lastShip.x
            val cellY = if (lastShip.isHorizontal) lastShip.y else lastShip.y + i
            currentBoard[cellY * 10 + cellX] = CellStatus.EMPTY
        }

        _state.update {
            it.copy(
                board = currentBoard,
                placedShips = newPlacedShips,
                remainingShips = newRemaining
            )
        }
    }

    private fun clearBoard() {
        _state.update {
            it.copy(
                board = List(100) { CellStatus.EMPTY },
                placedShips = emptyList(),
                remainingShips = mapOf(1 to 4, 2 to 3, 3 to 2, 4 to 1),
                selectedShipSize = null
            )
        }
    }


    override fun onCleared() {
        super.onCleared()
        gameListener?.cancel()
    }
}