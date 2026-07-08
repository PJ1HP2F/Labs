package com.example.seabattle.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.seabattle.data.entity.game.AuthStatus
import com.example.seabattle.presentation.screen.ActiveGamesScreen
import com.example.seabattle.presentation.screen.AuthScreen
import com.example.seabattle.presentation.screen.BattleScreen
import com.example.seabattle.presentation.screen.LobbyScreen
import com.example.seabattle.presentation.screen.PlacementScreen
import com.example.seabattle.presentation.screen.ProfileScreen
import com.example.seabattle.presentation.screen.StatsScreen
import com.example.seabattle.presentation.viewModel.ActiveGamesViewModel
import com.example.seabattle.presentation.viewModel.ActiveGamesViewModelFactory
import com.example.seabattle.presentation.viewModel.AuthViewModel
import com.example.seabattle.presentation.viewModel.AuthViewModelFactory
import com.example.seabattle.presentation.viewModel.BattleViewModel
import com.example.seabattle.presentation.viewModel.BattleViewModelFactory
import com.example.seabattle.presentation.viewModel.LobbyViewModel
import com.example.seabattle.presentation.viewModel.LobbyViewModelFactory
import com.example.seabattle.presentation.viewModel.PlacementViewModel
import com.example.seabattle.presentation.viewModel.PlacementViewModelFactory
import com.example.seabattle.presentation.viewModel.ProfileUserViewModel
import com.example.seabattle.presentation.viewModel.ProfileUserViewModelFactory
import com.example.seabattle.presentation.viewModel.StatsViewModel
import com.example.seabattle.presentation.viewModel.StatsViewModelFactory
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

@Composable
fun NavigationScreen(
    authViewModelFactory: AuthViewModelFactory,
    profileViewModelFactory: ProfileUserViewModelFactory,
    activeGamesViewModelFactory: ActiveGamesViewModelFactory,
    lobbyViewModelFactory: LobbyViewModelFactory,
    placementViewModelFactory: PlacementViewModelFactory,
    battleViewModelFactory: BattleViewModelFactory,
    statsViewModelFactory: StatsViewModelFactory
) {
    val navController = rememberNavController()

    val authViewModel: AuthViewModel = viewModel(factory = authViewModelFactory)
    val authState by authViewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        snapshotFlow { authState.statusSingIn }
            .filter { it != AuthStatus.LOADING }
            .distinctUntilChanged()
            .collect { status ->
                when (status) {
                    AuthStatus.AUTHENTICATED -> navController.navigate("profile") { popUpTo("auth") { inclusive = true } }
                    AuthStatus.UNAUTHENTICATED -> navController.navigate("auth") { popUpTo(0) }
                    else -> { }
                }
            }
    }

    NavHost(
        navController = navController,
        startDestination = "auth"
    ) {
        composable("auth") {
            AuthScreen(authViewModel = authViewModel)
        }

        composable("profile") {
            val profileViewModel: ProfileUserViewModel = viewModel(factory = profileViewModelFactory)
            ProfileScreen(
                profileViewModel = profileViewModel,
                onNavigateToActiveGames = { navController.navigate("active_games") },
                onNavigateToGame = { navController.navigate("lobby") },
                onNavigateToStats = { navController.navigate("stats") },
                onNavigateToAuth = { navController.navigate("auth") { popUpTo(0) } }
            )
        }

        composable("active_games") {
            val activeGamesViewModel: ActiveGamesViewModel = viewModel(factory = activeGamesViewModelFactory)
            ActiveGamesScreen(
                viewModel = activeGamesViewModel,
                onNavigateToGame = { gameId, isHost ->
                    navController.navigate("battle/$gameId") {
                        popUpTo("profile")
                    }
                },
                onNavigateToPlacement = { gameId, isHost ->
                    navController.navigate("placement/$gameId/$isHost") {
                        popUpTo("profile")
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("lobby") {
            val lobbyViewModel: LobbyViewModel = viewModel(factory = lobbyViewModelFactory)
            LobbyScreen(
                viewModel = lobbyViewModel,
                onNavigateToPlacement = { gameId, isHost ->
                    navController.navigate("placement/$gameId/$isHost") {
                        popUpTo("lobby") { inclusive = true }
                    }
                }
            )
        }

        composable("placement/{gameId}/{isHost}") { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId") ?: return@composable
            val isHost = backStackEntry.arguments?.getString("isHost")?.toBoolean() ?: false
            val placementViewModel: PlacementViewModel = viewModel(factory = placementViewModelFactory)
            PlacementScreen(
                viewModel = placementViewModel,
                gameId = gameId,
                isHost = isHost,
                onNavigateToBattle = { 
                    navController.navigate("battle/$gameId") {
                        popUpTo("profile")
                    }
                }
            )
        }

        composable("battle/{gameId}") { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId") ?: return@composable
            val battleViewModel: BattleViewModel = viewModel(factory = battleViewModelFactory)
            BattleScreen(
                battleViewModel = battleViewModel,
                gameId = gameId,
                onNavigateToProfile = { navController.popBackStack("profile", inclusive = false) }
            )
        }

        composable("stats") {
            val statsViewModel: StatsViewModel = viewModel(factory = statsViewModelFactory)
            StatsScreen(
                statsViewModel = statsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}