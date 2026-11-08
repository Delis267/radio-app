package com.iu.radio_app.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.annotation.StringRes
import com.iu.radio_app.R
import com.iu.radio_app.data.auth.AuthRepository
import com.iu.radio_app.data.auth.InvalidCredentialsException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel
    @Inject
    constructor(
        private val authRepository: AuthRepository,
    ) : ViewModel() {
        private val state = MutableStateFlow(LoginUiState())
        val uiState: StateFlow<LoginUiState> = state.asStateFlow()

        init {
            viewModelScope.launch {
                authRepository.currentUser.collect { user ->
                    state.update { it.copy(signedInAs = user?.email) }
                }
            }
        }

        fun onEmailChange(email: String) = state.update { it.copy(email = email, errorMessage = null) }

        fun onPasswordChange(password: String) = state.update { it.copy(password = password, errorMessage = null) }

        fun signIn() {
            val current = state.value
            if (!current.canSubmit) return
            state.update { it.copy(isLoading = true, errorMessage = null) }
            viewModelScope.launch {
                authRepository
                    .signIn(current.email.trim(), current.password)
                    .onSuccess { state.update { it.copy(isLoading = false, password = "") } }
                    .onFailure { error -> state.update { it.copy(isLoading = false, errorMessage = error.toMessage()) } }
            }
        }

        fun signOut() {
            viewModelScope.launch { authRepository.signOut() }
        }

        @StringRes
        private fun Throwable.toMessage(): Int =
            when (this) {
                is InvalidCredentialsException -> R.string.login_error_invalid_credentials
                else -> R.string.login_error_unknown
            }
    }
