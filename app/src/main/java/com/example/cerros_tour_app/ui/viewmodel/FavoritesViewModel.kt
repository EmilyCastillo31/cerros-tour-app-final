package com.example.cerros_tour_app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cerros_tour_app.data.repository.AuthRepository
import com.example.cerros_tour_app.data.repository.toFriendlyMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Favoritos para la pantalla de detalle: dice si un tour es favorito y permite marcarlo o desmarcarlo.
 * Escucha Firestore en tiempo real, así que el perfil y el detalle siempre coinciden.
 */
class FavoritesViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    val favoriteIds: StateFlow<Set<Int>> = repository.observeProfile()
        .map { it.favoriteIds.toSet() }
        .catch { e ->
            _message.value = e.toFriendlyMessage()
            emit(emptySet())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    fun toggleFavorite(tourId: Int) {
        val isFavorite = tourId in favoriteIds.value
        viewModelScope.launch {
            try {
                repository.setFavorite(tourId, !isFavorite)
                _message.value = if (isFavorite) "Quitado de favoritos" else "Agregado a favoritos"
            } catch (e: Exception) {
                _message.value = e.toFriendlyMessage()
            }
        }
    }

    fun messageShown() {
        _message.value = null
    }
}