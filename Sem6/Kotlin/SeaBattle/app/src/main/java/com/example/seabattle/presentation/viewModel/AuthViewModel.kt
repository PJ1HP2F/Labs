package com.example.seabattle.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seabattle.data.entity.game.AuthStatus
import com.example.seabattle.data.entity.user.ProfileUser
import com.example.seabattle.data.repository.AuthRepository
import com.example.seabattle.data.repository.UserRepository
import com.example.seabattle.presentation.action.AuthAction
import com.example.seabattle.presentation.state.AuthState
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state = _state.asStateFlow()

    private val _error = MutableSharedFlow<String>()
    val error: SharedFlow<String> = _error.asSharedFlow()

    private val _success = MutableSharedFlow<String>()
    val success: SharedFlow<String> = _success.asSharedFlow()

    init {
        authRepository.addAuthStateListener { user ->
            if (user != null) {
                setAuthUser(user)
            } else {
                setUnAuthUser()
            }
        }
    }

    fun onAction(action: AuthAction) {
        when (action) {
            is AuthAction.SignInWithEmail -> signInWithEmail()
            is AuthAction.SignUpWithEmail -> signUpWithEmail()
            AuthAction.SignOut -> signOut()
            is AuthAction.ToggleLoginRegisterMode -> toggleRegisterMode(action.newMode)
            is AuthAction.UpdateEmailField -> updateEmailField(action.email)
            is AuthAction.UpdateNicknameField -> updateNickname(action.nickname)
            is AuthAction.UpdatePasswordField -> updatePasswordField(action.password)
        }
    }

    private fun toggleRegisterMode(newMode: Boolean) {
        _state.update {
            it.copy(
                registerMode = newMode
            )
        }
    }

    private fun updateEmailField(newEmail: String) {
        _state.update {
            it.copy(
                emailInput = newEmail
            )
        }
    }

    private fun updatePasswordField(newPassword: String) {
        _state.update {
            it.copy(
                passwortInput = newPassword
            )
        }
    }

    private fun updateNickname(newNickname: String) {
        _state.update {
            it.copy(
                nicknameInput = newNickname
            )
        }
    }

    private fun setLoadingStatus() {
        _state.update {
            it.copy(
                statusSingIn = AuthStatus.LOADING
            )
        }
    }

    private fun setAuthUser(user: FirebaseUser) {
        _state.update {
            it.copy(
                statusSingIn = AuthStatus.AUTHENTICATED,
                userFirebase = user
            )
        }
    }

    private fun setUnAuthUser() {
        _state.update {
            it.copy(
                statusSingIn = AuthStatus.UNAUTHENTICATED,
                userFirebase = null
            )
        }
    }

    private fun signInWithEmail() {
        setLoadingStatus()

        val currState = _state.value
        viewModelScope.launch {
            try {
                val user = authRepository.signInWithEmail(
                    email = currState.emailInput,
                    password = currState.passwortInput
                )

                setAuthUser(user)
                _success.emit("Успешный вход")
            } catch (e: Exception) {
                _error.emit("Ошибка входа: ${e.message}")
                setUnAuthUser()
            }
        }
    }

    private fun signUpWithEmail() {
        setLoadingStatus()

        val currState = _state.value
        viewModelScope.launch {
            try {
                val user = authRepository.signUpWithEmail(
                    email = currState.emailInput,
                    password = currState.passwortInput
                )

                val profile = ProfileUser(
                    uid = user.uid,
                    nickname = currState.nicknameInput,
                    avatarName = null,
                    countGames = 0,
                    countWins = 0
                )
                userRepository.createUserProfile(profile)

                setAuthUser(user)
                _success.emit("Успешная регистрация")
            } catch (e: Exception) {
                _error.emit("Ошибка регистрации: ${e.message}")
                setUnAuthUser()
            }
        }
    }

    private fun signOut() {
        authRepository.signOut()
        _state.value = AuthState()
    }
}