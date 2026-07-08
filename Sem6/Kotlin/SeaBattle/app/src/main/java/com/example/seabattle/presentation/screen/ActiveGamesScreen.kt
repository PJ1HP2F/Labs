package com.example.seabattle.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.seabattle.data.entity.game.Game
import com.example.seabattle.data.entity.game.GameStatus
import com.example.seabattle.presentation.viewModel.ActiveGamesViewModel

@Composable
fun ActiveGamesScreen(
    viewModel: ActiveGamesViewModel,
    onNavigateToGame: (gameId: String, isHost: Boolean) -> Unit,
    onNavigateToPlacement: (gameId: String, isHost: Boolean) -> Unit,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(

    )
    { paddingValues ->
        if (state.activeGamesList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Нет активных игр")
                    Button(onClick = onNavigateBack) { Text("Создать новую") }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
            ) {
                items(state.activeGamesList) { game ->
                    GameCard(
                        game = game,
                        currentUserId = state.currentUserId,
                        onContinue = {
                            val isHost = game.hostId == state.currentUserId
                            val isReady = if (isHost) game.hostReady else game.guestReady
                            if (isReady) {
                                onNavigateToGame(game.gameId, isHost)
                            } else {
                                onNavigateToPlacement(game.gameId, isHost)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun GameCard(game: Game, currentUserId: String?, onContinue: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("ID: ${game.gameId.take(8)}...", fontWeight = FontWeight.Bold)
            Text("Статус: ${game.status.name}")
            Text("Ваша роль: ${if (game.hostId == currentUserId) "Хост" else "Гость"}")
            if (game.status == GameStatus.WAITING && game.guestId == null && game.hostId == currentUserId) {
                Text("Ожидание подключения соперника...", color = Color.Yellow)
            }
            Button(onClick = onContinue) { Text("Продолжить") }
        }
    }
}
