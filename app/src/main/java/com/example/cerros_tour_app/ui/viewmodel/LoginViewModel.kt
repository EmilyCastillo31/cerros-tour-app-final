package com.example.cerros_tour_app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cerros_tour_app.data.repository.AuthRepository
import com.example.cerros_tour_app.data.repository.toFriendlyMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

class LoginViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, emailError = null, errorMessage = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null, errorMessage = null) }
    }

    fun login() {
        val state = _uiState.value
        if (state.isLoading) return

        val emailError = AuthValidators.validateEmail(state.email)
        val passwordError = AuthValidators.validatePassword(state.password)
        if (emailError != null || passwordError != null) {
            _uiState.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                repository.login(state.email, state.password)
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.toFriendlyMessage()) }
            }
        }
    }

    /** Se llama después de navegar, para que al volver a Login el formulario esté limpio. */
    fun onSuccessHandled() {
        _uiState.update { LoginUiState(email = it.email) }
    }
}