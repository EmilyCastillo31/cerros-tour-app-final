package com.example.cerros_tour_app.data.model

/**
 * Datos del usuario guardados en Firestore, en el documento users/{uid}.
 *
 * favoriteIds guarda los id de los tours (los mismos de TourData.kt: 1, 2, 3, 4).
 */
data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val favoriteIds: List<Int> = emptyList()
) {
    /** Iniciales para el avatar: "Emily Castillo" -> "EC". */
    val initials: String
        get() = name.trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifEmpty { "?" }
}