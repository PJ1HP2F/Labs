package com.example.seabattle.data.repository

import com.example.seabattle.data.entity.game.CellStatus
import com.example.seabattle.data.entity.game.ConfigGame
import com.example.seabattle.data.entity.game.Game
import com.example.seabattle.data.entity.game.GameStatus
import com.example.seabattle.data.entity.game.MoveResult
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.random.Random
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class GameRepository(
    private val firestore: FirebaseFirestore
) {
    private val gameIdAlphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    private val gameIdLength = 4
    private val gameIdMaxAttempts = 30

    suspend fun createGame(hostId: String): String {
        val gameId = generateUniqueGameId()
        val game = Game(
            gameId = gameId,
            hostId = hostId,
            currentTurnId = hostId,
            status = GameStatus.WAITING,
        )
        firestore.collection("games").document(gameId).set(game).await()
        return gameId
    }

    suspend fun getActiveGamesForUser(userId: String): List<Game> {
        val games = firestore.collection("games")
            .whereIn("status", listOf(GameStatus.WAITING, GameStatus.ACTIVE))
            .get()
            .await()
            .toObjects(Game::class.java)
        return games.filter { it.hostId == userId || it.guestId == userId }
    }

    suspend fun joinAsGuest(gameId: String, guestId: String): Boolean {
        return suspendCoroutine { continuation ->
            val gameRef = firestore.collection("games").document(gameId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(gameRef)
                val game = snapshot.toObject(Game::class.java)
                if (game?.status == GameStatus.WAITING && game.guestId == null) {
                    transaction.update(gameRef, "guestId", guestId)
                    transaction.update(gameRef, "status", GameStatus.ACTIVE)
                    true
                } else {
                    false
                }
            }.addOnSuccessListener { success ->
                continuation.resume(success)
            }.addOnFailureListener { e ->
                continuation.resumeWithException(e)
            }
        }
    }

    suspend fun updateHostCells(gameId: String, cells: List<CellStatus>) {
        firestore.collection("games").document(gameId)
            .update("hostCells", cells, "hostReady", true)
            .await()
    }

    suspend fun updateGuestCells(gameId: String, cells: List<CellStatus>) {
        firestore.collection("games").document(gameId)
            .update("guestCells", cells, "guestReady", true)
            .await()
    }

    fun listenGame(gameId: String): Flow<Game?> = callbackFlow {
        val listener = firestore.collection("games").document(gameId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val game = snapshot?.toObject(Game::class.java)
                trySend(game)
            }

        awaitClose { listener.remove() }
    }

    suspend fun makeMove(
        gameId: String,
        playerId: String,
        targetPlayerId: String,
        x: Int,
        y: Int
    ): MoveResult {
        val gameRef = firestore.collection("games").document(gameId)
        return firestore.runTransaction { transaction ->
            val snapshot = transaction.get(gameRef)
            val game = snapshot.toObject(Game::class.java) ?: return@runTransaction MoveResult.INVALID

            if (game.currentTurnId != playerId) return@runTransaction MoveResult.INVALID

            val isTargetHost = targetPlayerId == game.hostId
            val boardField = if (isTargetHost) "hostCells" else "guestCells"
            val board = (if (isTargetHost) game.hostCells else game.guestCells).toMutableList()

            val index = y * ConfigGame.BOARD_SIZE + x
            val cell = board[index]

            val result = when (cell) {
                CellStatus.EMPTY -> {
                    board[index] = CellStatus.MISS
                    MoveResult.MISS
                }
                CellStatus.SUBMARINE -> {
                    board[index] = CellStatus.SUBMARINE_REVEALED
                    MoveResult.MISS
                }
                CellStatus.SUBMARINE_REVEALED -> {
                    board[index] = CellStatus.HURT
                    val sunk = isShipDestroyed(board, x, y, ConfigGame.BOARD_SIZE)
                    if (sunk) {
                        markShipAsDestroyed(board, x, y, ConfigGame.BOARD_SIZE)
                        MoveResult.DESTROYED
                    } else {
                        MoveResult.HIT
                    }
                }
                CellStatus.SHIP -> {
                    board[index] = CellStatus.HURT
                    val sunk = isShipDestroyed(board, x, y, ConfigGame.BOARD_SIZE)
                    if (sunk) {
                        markShipAsDestroyed(board, x, y, ConfigGame.BOARD_SIZE)
                        MoveResult.DESTROYED
                    } else {
                        MoveResult.HIT
                    }
                }
                else -> MoveResult.INVALID
            }

            transaction.update(gameRef, boardField, board)

            if (result == MoveResult.MISS) {
                transaction.update(gameRef, "currentTurnId", targetPlayerId)
            }

            val allSunk = isAllShipsDestroyed(board)
            if (allSunk) {
                transaction.update(gameRef, "status", GameStatus.FINISHED)
                transaction.update(gameRef, "winnerId", playerId)
            }

            result
        }.await()
    }

    private suspend fun generateUniqueGameId(): String {
        repeat(gameIdMaxAttempts) {
            val candidate = generateGameId()
            val exists = firestore.collection("games").document(candidate).get().await().exists()
            if (!exists) {
                return candidate
            }
        }
        throw IllegalStateException("Не удалось сгенерировать уникальный ID игры")
    }

    private fun generateGameId(): String = buildString(gameIdLength) {
        repeat(gameIdLength) {
            append(gameIdAlphabet[Random.nextInt(gameIdAlphabet.length)])
        }
    }

    private fun findShipIndices(board: List<CellStatus>, x: Int, y: Int, boardSize: Int): List<Int> {
        val shipIndices = mutableListOf<Int>()
        fun isShipCell(status: CellStatus): Boolean {
            return status == CellStatus.SHIP ||
                status == CellStatus.HURT ||
                status == CellStatus.SUBMARINE ||
                status == CellStatus.SUBMARINE_REVEALED
        }
        var left = x
        while (left >= 0 && isShipCell(board[y * boardSize + left])) {
            left--
        }
        var right = x
        while (right < boardSize && isShipCell(board[y * boardSize + right])) {
            right++
        }

        if (right - left - 1 > 1) {
            for (cx in left + 1 until right) {
                shipIndices.add(y * boardSize + cx)
            }
            return shipIndices
        }

        var top = y
        while (top >= 0 && isShipCell(board[top * boardSize + x])) {
            top--
        }
        var bottom = y
        while (bottom < boardSize && isShipCell(board[bottom * boardSize + x])) {
            bottom++
        }
        for (cy in top + 1 until bottom) {
            shipIndices.add(cy * boardSize + x)
        }
        return shipIndices
    }

    suspend fun getGames(): List<Game> {
        return firestore.collection("games")
            .whereEqualTo("status", GameStatus.FINISHED)
            .get()
            .await()
            .toObjects(Game::class.java)
    }

    private fun isShipDestroyed(board: List<CellStatus>, x: Int, y: Int, boardSize: Int): Boolean {
        val indices = findShipIndices(board, x, y, boardSize)
        return indices.all { board[it] == CellStatus.HURT || board[it] == CellStatus.DESTROYED }
    }

    private fun markShipAsDestroyed(board: MutableList<CellStatus>, x: Int, y: Int, boardSize: Int) {
        val indices = findShipIndices(board, x, y, boardSize)
        for (idx in indices) {
            board[idx] = CellStatus.DESTROYED
        }
        
        for (idx in indices) {
            val cx = idx % boardSize
            val cy = idx / boardSize
            
            for (dy in -1..1) {
                for (dx in -1..1) {
                    val nx = cx + dx
                    val ny = cy + dy
                    if (nx in 0 until boardSize && ny in 0 until boardSize) {
                        val nIdx = ny * boardSize + nx
                        if (board[nIdx] == CellStatus.EMPTY) {
                            board[nIdx] = CellStatus.MISS
                        }
                    }
                }
            }
        }
    }

    private fun isAllShipsDestroyed(board: List<CellStatus>): Boolean {
        return !board.contains(CellStatus.SHIP) &&
            !board.contains(CellStatus.SUBMARINE) &&
            !board.contains(CellStatus.SUBMARINE_REVEALED)
    }
}