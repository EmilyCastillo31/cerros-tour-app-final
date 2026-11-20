package com.example.cerros_tour_app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cerros_tour_app.data.model.UserProfile
import com.example.cerros_tour_app.data.repository.AuthRepository
import com.example.cerros_tour_app.data.repository.toFriendlyMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = true,
    val profile: UserProfile? = null,
    val errorMessage: String? = null
)

class ProfileViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var profileJob: Job? = null

    /** Empieza (o reinicia) la escucha en tiempo real del perfil del usuario actual. */
    fun loadProfile() {
        profileJob?.cancel()
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        profileJob = viewModelScope.launch {
            repository.observeProfile()
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.toFriendlyMessage()) }
                }
                .collect { profile ->
                    _uiState.update { ProfileUiState(isLoading = false, profile = profile) }
                }
        }
    }

    fun removeFavorite(tourId: Int) {
        viewModelScope.launch {
            try {
                repository.setFavorite(tourId, isFavorite = false)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.toFriendlyMessage()) }
            }
        }
    }

    /** Detiene el listener ANTES de cerrar sesión para evitar errores PERMISSION_DENIED. */
    fun logout() {
        profileJob?.cancel()
        profileJob = null
        repository.logout()
        _uiState.value = ProfileUiState()
    }
}