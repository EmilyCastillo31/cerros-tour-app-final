package com.example.cerros_tour_app.ui.viewmodel

/**
 * Validaciones de los formularios. Devuelven el mensaje de error o null si el campo es válido.
 * Usa Regex en lugar de android.util.Patterns para poder probarlas con JUnit sin emulador.
 */
object AuthValidators {

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    const val MIN_PASSWORD_LENGTH = 6
    const val MIN_NAME_LENGTH = 3

    fun validateEmail(email: String): String? = when {
        email.isBlank() -> "Ingresa tu correo electrónico"
        !EMAIL_REGEX.matches(email.trim()) -> "El correo no es válido"
        else -> null
    }

    fun validatePassword(password: String): String? = when {
        password.isBlank() -> "Ingresa tu contraseña"
        password.length < MIN_PASSWORD_LENGTH -> "Mínimo $MIN_PASSWORD_LENGTH caracteres"
        else -> null
    }

    fun validateName(name: String): String? = when {
        name.isBlank() -> "Ingresa tu nombre completo"
        name.trim().length < MIN_NAME_LENGTH -> "El nombre es muy corto"
        else -> null
    }

    fun validateConfirmPassword(password: String, confirm: String): String? = when {
        confirm.isBlank() -> "Confirma tu contraseña"
        password != confirm -> "Las contraseñas no coinciden"
        else -> null
    }
}