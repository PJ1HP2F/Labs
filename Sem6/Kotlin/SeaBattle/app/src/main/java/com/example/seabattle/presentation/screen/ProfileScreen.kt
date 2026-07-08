package com.example.seabattle.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.seabattle.R
import com.example.seabattle.presentation.action.ProfileUserAction
import com.example.seabattle.presentation.viewModel.ProfileUserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profileViewModel: ProfileUserViewModel,
    onNavigateToActiveGames: () -> Unit,
    onNavigateToGame: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToAuth: () -> Unit
) {
    val state by profileViewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        profileViewModel.error.collect { error ->
            snackbarHostState.showSnackbar(error)
        }
    }
    LaunchedEffect(Unit) {
        profileViewModel.success.collect { success ->
            snackbarHostState.showSnackbar(success)
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
        } },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                return@Box
            }

            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .clickable { profileViewModel.onAction(ProfileUserAction.ToggleAvatarSelector) },
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Image(
                        painter = painterResource(id = state.avatarItem?.resId ?: R.drawable.avatar_1),
                        contentDescription = "Аватар",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = state.nickname,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = { profileViewModel.onAction(ProfileUserAction.ToggleEditingNickname) },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.edit),
                            contentDescription = "Редактировать",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onNavigateToActiveGames,
                    modifier = Modifier.width(160.dp)
                ) {
                    Text("Мои игры")
                }
                Button(
                    onClick = onNavigateToGame,
                    modifier = Modifier.width(160.dp)
                ) {
                    Text("Новая игра")
                }
                Button(
                    onClick = onNavigateToStats,
                    modifier = Modifier.width(160.dp)
                ) {
                    Text("Статистика")
                }
                Button(
                    onClick = {
                        profileViewModel.onAction(ProfileUserAction.Logout)
                        onNavigateToAuth()
                    },
                    modifier = Modifier.width(160.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Выйти")
                }
            }
        }
    }

    if (state.isEditingNickname) {
        AlertDialog(
            onDismissRequest = { profileViewModel.onAction(action = ProfileUserAction.ToggleEditingNickname) },
            title = { Text("Изменить никнейм") },
            text = {
                OutlinedTextField(
                    value = state.newNicknameField,
                    onValueChange = { profileViewModel.onAction(action = ProfileUserAction.UpdateNicknameField(it)) },
                    label = { Text("Новый никнейм") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (state.newNicknameField.isNotBlank()) {
                            profileViewModel.onAction(ProfileUserAction.UpdateNickname)
                        }
                        profileViewModel.onAction(action = ProfileUserAction.ToggleEditingNickname)
                    }
                ) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    profileViewModel.onAction(action = ProfileUserAction.ToggleEditingNickname)
                }) {
                    Text("Отмена")
                }
            }
        )
    }

    if (state.isShowAvatarSelector) {
        AlertDialog(
            onDismissRequest = { profileViewModel.onAction(ProfileUserAction.ToggleAvatarSelector) },
            title = { Text("Выберите аватар") },
            text = {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.avatarList) { avatar ->
                        Image(
                            painter = painterResource(id = avatar.resId),
                            contentDescription = "Аватар",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .clickable {
                                    profileViewModel.onAction(ProfileUserAction.UpdateAvatar(avatar.name))
                                    profileViewModel.onAction(ProfileUserAction.ToggleAvatarSelector)
                                },
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { profileViewModel.onAction(ProfileUserAction.ToggleAvatarSelector) }) {
                    Text("Закрыть")
                }
            }
        )
    }
}