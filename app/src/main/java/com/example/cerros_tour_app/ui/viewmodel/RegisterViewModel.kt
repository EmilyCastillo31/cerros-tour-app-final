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

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

class RegisterViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value, nameError = null, errorMessage = null) }
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, emailError = null, errorMessage = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null, errorMessage = null) }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update {
            it.copy(confirmPassword = value, confirmPasswordError = null, errorMessage = null)
        }
    }

    fun register() {
        val state = _uiState.value
        if (state.isLoading) return

        val nameError = AuthValidators.validateName(state.name)
        val emailError = AuthValidators.validateEmail(state.email)
        val passwordError = AuthValidators.validatePassword(state.password)
        val confirmError = AuthValidators.validateConfirmPassword(state.password, state.confirmPassword)

        if (listOf(nameError, emailError, passwordError, confirmError).any { it != null }) {
            _uiState.update {
                it.copy(
                    nameError = nameError,
                    emailError = emailError,
                    passwordError = passwordError,
                    confirmPasswordError = confirmError
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                repository.register(state.name, state.email, state.password)
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.toFriendlyMessage()) }
            }
        }
    }

    fun onSuccessHandled() {
        _uiState.value = RegisterUiState()
    }
}