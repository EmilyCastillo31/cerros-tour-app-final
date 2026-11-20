package com.example.cerros_tour_app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cerros_tour_app.data.Tour
import com.example.cerros_tour_app.data.model.UserProfile
import com.example.cerros_tour_app.data.tours
import com.example.cerros_tour_app.ui.theme.CerrostourappTheme
import com.example.cerros_tour_app.ui.viewmodel.ProfileUiState
import com.example.cerros_tour_app.ui.viewmodel.ProfileViewModel

/**
 * Perfil del usuario con datos reales de Firebase.
 * - onLogoutClick: se llama después de cerrar la sesión en Firebase.
 * - onTourClick: abre el detalle de un tour favorito.
 * - onBack: si se pasa, muestra el botón "Volver".
 */
@Composable
fun ProfileScreen(
    onLogoutClick: () -> Unit,
    onTourClick: (Int) -> Unit = {},
    onBack: (() -> Unit)? = null,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Se recarga cada vez que la pantalla entra en composición (por ejemplo, tras cambiar de cuenta).
    LaunchedEffect(Unit) { viewModel.loadProfile() }

    ProfileContent(
        uiState = uiState,
        onRetry = viewModel::loadProfile,
        onTourClick = onTourClick,
        onRemoveFavorite = viewModel::removeFavorite,
        onBack = onBack,
        onLogoutConfirmed = {
            viewModel.logout()
            onLogoutClick()
        }
    )
}

@Composable
fun ProfileContent(
    uiState: ProfileUiState,
    onRetry: () -> Unit,
    onTourClick: (Int) -> Unit,
    onRemoveFavorite: (Int) -> Unit,
    onBack: (() -> Unit)?,
    onLogoutConfirmed: () -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (onBack != null) {
                TextButton(onClick = onBack, modifier = Modifier.padding(start = 8.dp, top = 8.dp)) {
                    Text("← Volver")
                }
            }

            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                uiState.profile == null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = uiState.errorMessage ?: "No se pudo cargar tu perfil.",
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onRetry, shape = RoundedCornerShape(12.dp)) {
                            Text("Reintentar")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { showLogoutDialog = true }) {
                            Text("Cerrar Sesión", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                else -> {
                    ProfileDetails(
                        profile = uiState.profile,
                        errorMessage = uiState.errorMessage,
                        onTourClick = onTourClick,
                        onRemoveFavorite = onRemoveFavorite,
                        onLogoutClick = { showLogoutDialog = true }
                    )
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Seguro que quieres salir de tu cuenta?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onLogoutConfirmed()
                }) {
                    Text("Salir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun ProfileDetails(
    profile: UserProfile,
    errorMessage: String?,
    onTourClick: (Int) -> Unit,
    onRemoveFavorite: (Int) -> Unit,
    onLogoutClick: () -> Unit
) {
    // Cruza los id guardados en Firestore con el catálogo local (TourData.kt).
    val favoriteTours = tours.filter { it.id in profile.favoriteIds }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = profile.initials,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = profile.name.ifBlank { "Viajero de los cerros" },
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = profile.email,
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Tours favoritos (${favoriteTours.size})",
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (favoriteTours.isEmpty()) {
                    Text(
                        text = "Aún no tienes tours favoritos. Abre un destino del catálogo y toca ♡ para guardarlo.",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                } else {
                    favoriteTours.forEach { tour ->
                        FavoriteTourRow(
                            tour = tour,
                            onClick = { onTourClick(tour.id) },
                            onRemove = { onRemoveFavorite(tour.id) }
                        )
                    }
                }
            }
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(
            onClick = onLogoutClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Text(text = "Cerrar Sesión", fontSize = 16.sp)
        }
    }
}

@Composable
private fun FavoriteTourRow(
    tour: Tour,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(tour.image),
            contentDescription = tour.name,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = tour.name, fontWeight = FontWeight.SemiBold)
            Text(text = tour.description, fontSize = 12.sp, color = Color.Gray)
        }
        TextButton(onClick = onRemove) {
            Text("Quitar", fontSize = 13.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    CerrostourappTheme(dynamicColor = false) {
        ProfileContent(
            uiState = ProfileUiState(
                isLoading = false,
                profile = UserProfile(
                    uid = "demo",
                    name = "Emily Castillo",
                    email = "escastillop@udistrital.edu.co",
                    favoriteIds = listOf(1, 3)
                )
            ),
            onRetry = {},
            onTourClick = {},
            onRemoveFavorite = {},
            onBack = {},
            onLogoutConfirmed = {}
        )
    }
}

@Preview(showBackground = true, name = "Perfil sin favoritos")
@Composable
fun ProfileScreenEmptyPreview() {
    CerrostourappTheme(dynamicColor = false) {
        ProfileContent(
            uiState = ProfileUiState(
                isLoading = false,
                profile = UserProfile(name = "Nuevo Usuario", email = "nuevo@correo.com")
            ),
            onRetry = {},
            onTourClick = {},
            onRemoveFavorite = {},
            onBack = null,
            onLogoutConfirmed = {}
        )
    }
}