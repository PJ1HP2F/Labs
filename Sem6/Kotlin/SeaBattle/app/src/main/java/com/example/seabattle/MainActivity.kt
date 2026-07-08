package com.example.seabattle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.seabattle.presentation.navigation.NavigationScreen
import com.example.seabattle.presentation.viewModel.ActiveGamesViewModelFactory
import com.example.seabattle.presentation.viewModel.AuthViewModelFactory
import com.example.seabattle.presentation.viewModel.BattleViewModelFactory
import com.example.seabattle.presentation.viewModel.LobbyViewModelFactory
import com.example.seabattle.presentation.viewModel.PlacementViewModelFactory
import com.example.seabattle.presentation.viewModel.ProfileUserViewModelFactory
import com.example.seabattle.presentation.viewModel.StatsViewModelFactory
import com.example.seabattle.ui.theme.SeaBattleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as SeaBattleApplication

        val authViewModelFactory = AuthViewModelFactory(
            authRepository = app.authRepository,
            userRepository = app.userRepository
        )
        val profileViewModelFactory = ProfileUserViewModelFactory(
            authRepository = app.authRepository,
            userRepository = app.userRepository
        )
        val activeGamesViewModelFactory = ActiveGamesViewModelFactory(
            authRepository = app.authRepository,
            gameRepository = app.gameRepository
        )
        val lobbyViewModelFactory = LobbyViewModelFactory(
            authRepository = app.authRepository,
            gameRepository = app.gameRepository
        )
        val placementViewModelFactory = PlacementViewModelFactory(
            gameRepository = app.gameRepository,
            authRepository = app.authRepository
        )
        val battleViewModelFactory = BattleViewModelFactory(
            gameRepository = app.gameRepository,
            authRepository = app.authRepository
        )
        val statsViewModelFactory = StatsViewModelFactory(
            gameRepository = app.gameRepository,
            authRepository = app.authRepository
        )

        setContent {
            SeaBattleTheme {
                NavigationScreen(
                    authViewModelFactory = authViewModelFactory,
                    profileViewModelFactory = profileViewModelFactory,
                    activeGamesViewModelFactory = activeGamesViewModelFactory,
                    lobbyViewModelFactory = lobbyViewModelFactory,
                    placementViewModelFactory = placementViewModelFactory,
                    battleViewModelFactory = battleViewModelFactory,
                    statsViewModelFactory = statsViewModelFactory
                )
            }
        }
    }
}
