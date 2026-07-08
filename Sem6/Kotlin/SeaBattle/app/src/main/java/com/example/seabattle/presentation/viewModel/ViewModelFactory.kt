package com.example.seabattle.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.seabattle.data.repository.AuthRepository
import com.example.seabattle.data.repository.GameRepository
import com.example.seabattle.data.repository.UserRepository

class AuthViewModelFactory(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
): ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(
                userRepository = userRepository,
                authRepository = authRepository
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}


class ProfileUserViewModelFactory(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
): ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(ProfileUserViewModel::class.java)) {
            return ProfileUserViewModel(
                userRepository = userRepository,
                authRepository = authRepository
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}


class ActiveGamesViewModelFactory(
    private val authRepository: AuthRepository,
    private val gameRepository: GameRepository
): ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(ActiveGamesViewModel::class.java)) {
            return ActiveGamesViewModel(
                authRepository = authRepository,
                gameRepository = gameRepository
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}


class LobbyViewModelFactory(
    private val authRepository: AuthRepository,
    private val gameRepository: GameRepository
): ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(LobbyViewModel::class.java)) {
            return LobbyViewModel(
                authRepository = authRepository,
                gameRepository = gameRepository
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}


class BattleViewModelFactory(
    private val gameRepository: GameRepository,
    private val authRepository: AuthRepository
): ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(BattleViewModel::class.java)) {
            return BattleViewModel(
                gameRepository = gameRepository,
                authRepository = authRepository
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}


class PlacementViewModelFactory(
    private val gameRepository: GameRepository,
    private val authRepository: AuthRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlacementViewModel::class.java)) {
            return PlacementViewModel(
                gameRepository = gameRepository,
                authRepository = authRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


class StatsViewModelFactory(
    private val gameRepository: GameRepository,
    private val authRepository: AuthRepository
): ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(StatsViewModel::class.java)) {
            return StatsViewModel(
                gameRepository = gameRepository,
                authRepository = authRepository
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}