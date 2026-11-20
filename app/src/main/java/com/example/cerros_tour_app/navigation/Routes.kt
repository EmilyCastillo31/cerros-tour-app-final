package com.example.cerros_tour_app.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.cerros_tour_app.data.tours
import com.example.cerros_tour_app.ui.screens.ActiveReservationsScreen
import com.example.cerros_tour_app.ui.screens.BookingScreen
import com.example.cerros_tour_app.ui.screens.ConfirmationScreen
import com.example.cerros_tour_app.ui.screens.DetailScreen
import com.example.cerros_tour_app.ui.screens.HomeScreen
import com.example.cerros_tour_app.ui.screens.LoginScreen
import com.example.cerros_tour_app.ui.screens.ProfileScreen
import com.example.cerros_tour_app.ui.screens.RegisterScreen
import com.example.cerros_tour_app.ui.viewmodel.FavoritesViewModel

/** Rutas de toda la app en un solo lugar. */
object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val DETAIL = "detail/{tourId}"
    const val BOOKING = "booking"
    const val CONFIRMATION = "confirmation"
    const val RESERVATIONS = "reservations"
    const val PROFILE = "profile"

    fun detail(tourId: Int) = "detail/$tourId"
}

/**
 * Navegación unificada del equipo:
 * Login/Registro -> Catálogo -> Detalle -> Reserva -> Confirmación -> Mis reservas,
 * y Perfil desde el catálogo.
 *
 * @param startDestination LOGIN si no hay sesión, HOME si Firebase ya tiene un usuario.
 */
@Composable
fun AppNavigation(
    startDestination: String = Routes.LOGIN,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        // LOGIN
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginClick = { navController.goToHomeClearingAuth() },
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) }
            )
        }

        // REGISTRO
        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = { navController.goToHomeClearingAuth() },
                onBackToLogin = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Routes.LOGIN)
                    }
                }
            )
        }

        // CATÁLOGO
        composable(Routes.HOME) {
            HomeScreen(
                onTourClick = { tourId -> navController.navigate(Routes.detail(tourId)) },
                onProfileClick = { navController.navigate(Routes.PROFILE) }
            )
        }

        // DETALLE
        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("tourId") { type = NavType.IntType })
        ) { backStackEntry ->
            val tourId = backStackEntry.arguments?.getInt("tourId")
            val selectedTour = tours.find { it.id == tourId }

            if (selectedTour != null) {
                // [ROL 1] Favoritos sincronizados con Firestore
                val favoritesViewModel: FavoritesViewModel = viewModel()
                val favoriteIds by favoritesViewModel.favoriteIds.collectAsState()
                val message by favoritesViewModel.message.collectAsState()
                val context = LocalContext.current

                LaunchedEffect(message) {
                    message?.let {
                        Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                        favoritesViewModel.messageShown()
                    }
                }

                DetailScreen(
                    tour = selectedTour,
                    onBack = { navController.popBackStack() },
                    isFavorite = selectedTour.id in favoriteIds,
                    onToggleFavorite = { favoritesViewModel.toggleFavorite(selectedTour.id) },
                    onBookClick = { navController.navigate(Routes.BOOKING) }
                )
            }
        }

        // RESERVA
        composable(Routes.BOOKING) {
            BookingScreen(
                onContinue = { navController.navigate(Routes.CONFIRMATION) },
                onViewReservations = { navController.navigate(Routes.RESERVATIONS) }
            )
        }

        // CONFIRMACIÓN
        composable(Routes.CONFIRMATION) {
            ConfirmationScreen(
                onSeeReservations = {
                    // No volver a la confirmación con el botón atrás
                    navController.navigate(Routes.RESERVATIONS) {
                        popUpTo(Routes.HOME)
                    }
                },
                onNewBooking = {
                    navController.navigate(Routes.BOOKING) {
                        popUpTo(Routes.HOME)
                    }
                }
            )
        }

        // MIS RESERVAS
        composable(Routes.RESERVATIONS) {
            ActiveReservationsScreen(
                onNewBooking = {
                    navController.navigate(Routes.BOOKING) {
                        popUpTo(Routes.HOME)
                    }
                },
                onBack = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Routes.HOME)
                    }
                }
            )
        }

        // [ROL 1] PERFIL
        composable(Routes.PROFILE) {
            ProfileScreen(
                onLogoutClick = {
                    // Borra todo el historial: con "atrás" no se puede volver a la app sin sesión
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                },
                onTourClick = { tourId -> navController.navigate(Routes.detail(tourId)) },
                onBack = { navController.popBackStack() }
            )
        }
    }
}

/** Entra al catálogo y saca Login/Registro del historial. */
private fun NavHostController.goToHomeClearingAuth() {
    navigate(Routes.HOME) {
        popUpTo(graph.id) { inclusive = true }
    }
}