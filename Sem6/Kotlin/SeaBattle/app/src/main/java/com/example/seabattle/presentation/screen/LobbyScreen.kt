package com.example.seabattle.presentation.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.seabattle.presentation.action.LobbyAction
import com.example.seabattle.presentation.viewModel.LobbyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(
    viewModel: LobbyViewModel,
    onNavigateToPlacement: (gameId: String, isHost: Boolean) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.error.collect { error ->
            snackbarHostState.showSnackbar(error)
        }
    }
    LaunchedEffect(Unit) {
        viewModel.success.collect { success ->
            snackbarHostState.showSnackbar(success)
        }
    }

    LaunchedEffect(state.navigateToPlacement) {
        if (state.navigateToPlacement && state.gameIdForPlacement != null) {
            viewModel.onAction(action = LobbyAction.ResetNavigation)
            onNavigateToPlacement(state.gameIdForPlacement!!, state.isHost)
        }
    }

    LaunchedEffect(state.gameIdToCopy) {
        state.gameIdToCopy?.let { id ->
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Game ID", id)
            clipboard.setPrimaryClip(clip)
            snackbarHostState.showSnackbar("ID игры скопирован: $id")
            viewModel.onAction(LobbyAction.CopyGameId)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) { snackbarData ->
            Snackbar(
                snackbarData = snackbarData,
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface,
            )
        } }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding()
                )
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                return@Box
            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Создать игру", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Button(
                            onClick = { viewModel.onAction(LobbyAction.CreateGame) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Создать")
                        }
                        if (state.createdGameId != null) {
                            Text("ID игры: ${state.createdGameId}", fontSize = 14.sp)
                            Button(
                                onClick = { viewModel.onAction(LobbyAction.CopyGameId) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Скопировать ID")
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Присоединиться", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = state.gameIdInput,
                            onValueChange = { viewModel.onAction(LobbyAction.UpdateGameIdInput(it)) },
                            label = { Text("ID игры (4 символа)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = { viewModel.onAction(LobbyAction.JoinToGame(state.gameIdInput)) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = state.gameIdInput.length == 4
                        ) {
                            Text("Присоединиться")
                        }
                    }
                }
            }
        }
    }
}