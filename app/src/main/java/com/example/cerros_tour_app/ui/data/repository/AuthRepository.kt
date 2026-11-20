package com.example.cerros_tour_app.data.repository

import com.example.cerros_tour_app.data.model.UserProfile
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Único punto de acceso a Firebase Authentication y Cloud Firestore
 * para la sesión y el perfil del usuario.
 */
class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private fun userDoc(uid: String) = db.collection(USERS_COLLECTION).document(uid)

    val currentUserId: String?
        get() = auth.currentUser?.uid

    /** true si Firebase ya tiene una sesión guardada en el dispositivo. */
    fun isLoggedIn(): Boolean = auth.currentUser != null

    suspend fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email.trim(), password).awaitTask()
    }

    /**
     * Crea la cuenta en Authentication, guarda el nombre en el perfil de Auth
     * y crea el documento users/{uid} en Firestore.
     */
    suspend fun register(name: String, email: String, password: String) {
        val result = auth.createUserWithEmailAndPassword(email.trim(), password).awaitTask()
        val user = result.user ?: throw IllegalStateException("No se pudo crear el usuario.")

        val profileUpdate = UserProfileChangeRequest.Builder()
            .setDisplayName(name.trim())
            .build()
        user.updateProfile(profileUpdate).awaitTask()

        val data = mapOf(
            "name" to name.trim(),
            "email" to (user.email ?: email.trim()),
            "favorites" to emptyList<Int>(),
            "createdAt" to FieldValue.serverTimestamp()
        )
        userDoc(user.uid).set(data).awaitTask()
    }

    fun logout() {
        auth.signOut()
    }

    /**
     * Escucha en tiempo real el documento del usuario.
     * Si el documento no existe todavía, devuelve los datos básicos de Auth.
     */
    fun observeProfile(): Flow<UserProfile> = callbackFlow {
        val user = auth.currentUser
            ?: throw IllegalStateException("No hay una sesión activa.")

        val registration = userDoc(user.uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val profile = if (snapshot != null && snapshot.exists()) {
                snapshot.toUserProfile(user)
            } else {
                UserProfile(
                    uid = user.uid,
                    name = user.displayName.orEmpty(),
                    email = user.email.orEmpty()
                )
            }
            trySend(profile)
        }
        awaitClose { registration.remove() }
    }

    /**
     * Agrega o quita un tour de favoritos usando el id de TourData.kt.
     * Firestore guarda los números como Long, por eso se convierte antes de escribir.
     */
    suspend fun setFavorite(tourId: Int, isFavorite: Boolean) {
        val uid = currentUserId ?: throw IllegalStateException("No hay una sesión activa.")
        val value = tourId.toLong()
        val change = if (isFavorite) {
            FieldValue.arrayUnion(value)
        } else {
            FieldValue.arrayRemove(value)
        }
        userDoc(uid).set(mapOf("favorites" to change), SetOptions.merge()).awaitTask()
    }

    private fun DocumentSnapshot.toUserProfile(fallback: FirebaseUser) = UserProfile(
        uid = id,
        name = getString("name") ?: fallback.displayName.orEmpty(),
        email = getString("email") ?: fallback.email.orEmpty(),
        favoriteIds = (get("favorites") as? List<*>)
            ?.mapNotNull { (it as? Number)?.toInt() }
            ?.distinct()
            ?: emptyList()
    )

    companion object {
        const val USERS_COLLECTION = "users"
    }
}

/** Convierte una Task de Firebase en una función suspend (sin dependencias extra). */
suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { continuation ->
    addOnCompleteListener { task ->
        if (task.isSuccessful) {
            continuation.resume(task.result)
        } else {
            continuation.resumeWithException(
                task.exception ?: Exception("La operación fue cancelada.")
            )
        }
    }
}

/** Traduce los errores de Firebase a mensajes claros en español. */
fun Throwable.toFriendlyMessage(): String = when (this) {
    // WeakPassword hereda de InvalidCredentials: debe ir primero.
    is FirebaseAuthWeakPasswordException -> "La contraseña es muy débil (mínimo 6 caracteres)."
    is FirebaseAuthUserCollisionException -> "Ya existe una cuenta con ese correo."
    is FirebaseAuthInvalidUserException -> "No existe una cuenta con ese correo o fue deshabilitada."
    is FirebaseAuthInvalidCredentialsException -> "Correo o contraseña incorrectos."
    is FirebaseNetworkException -> "Sin conexión. Revisa tu internet e inténtalo de nuevo."
    is FirebaseTooManyRequestsException -> "Demasiados intentos. Espera un momento e inténtalo de nuevo."
    is FirebaseFirestoreException -> when (code) {
        FirebaseFirestoreException.Code.PERMISSION_DENIED ->
            "Sin permiso para acceder a los datos. Revisa las reglas de Firestore."
        FirebaseFirestoreException.Code.UNAVAILABLE ->
            "Firestore no está disponible. Revisa tu conexión."
        else -> "Error al acceder a los datos: ${message ?: code.name}"
    }
    else -> message ?: "Ocurrió un error inesperado."
}