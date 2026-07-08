package com.example.seabattle.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.seabattle.presentation.action.AuthAction
import com.example.seabattle.presentation.viewModel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(authViewModel: AuthViewModel) {
    val state by authViewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        authViewModel.error.collect { errorMsg ->
            snackbarHostState.showSnackbar(errorMsg)
        }
    }
    LaunchedEffect(Unit) {
        authViewModel.success.collect { successMsg ->
            snackbarHostState.showSnackbar(successMsg)
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { snackbarData ->
                Snackbar(
                    snackbarData = snackbarData,
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Статус: ${state.statusSingIn.name}")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = state.emailInput,
                        onValueChange = { authViewModel.onAction(AuthAction.UpdateEmailField(it)) },
                        label = { Text("Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.passwortInput,
                        onValueChange = { authViewModel.onAction(AuthAction.UpdatePasswordField(it)) },
                        label = { Text("Пароль") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (state.registerMode) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = state.nicknameInput,
                            onValueChange = {
                                authViewModel.onAction(
                                    AuthAction.UpdateNicknameField(
                                        it
                                    )
                                )
                            },
                            label = { Text("Никнейм") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(0.5f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = { authViewModel.onAction(AuthAction.SignInWithEmail) },
                        enabled = state.emailInput.isNotBlank() && state.passwortInput.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Вход")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (state.registerMode) {
                                if (state.nicknameInput.isNotBlank()) {
                                    authViewModel.onAction(AuthAction.SignUpWithEmail)
                                }
                            } else {
                                authViewModel.onAction(AuthAction.ToggleLoginRegisterMode(true))
                            }
                        },
                        enabled = state.emailInput.isNotBlank() && state.passwortInput.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (state.registerMode) "Зарегистрироваться" else "Регистрация")
                    }
                    if (state.registerMode) {
                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(
                            onClick = {
                                authViewModel.onAction(
                                    AuthAction.ToggleLoginRegisterMode(
                                        false
                                    )
                                )
                            }
                        ) {
                            Text("Уже есть аккаунт? Войти")
                        }
                    }
                }
            }

        }
    }
}