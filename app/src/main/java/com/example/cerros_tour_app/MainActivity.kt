package com.example.cerros_tour_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.cerros_tour_app.data.repository.AuthRepository
import com.example.cerros_tour_app.navigation.AppNavigation
import com.example.cerros_tour_app.navigation.Routes
import com.example.cerros_tour_app.ui.theme.CerrostourappTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // [ROL 1] Si Firebase ya tiene una sesión guardada, se entra directo al catálogo.
        val startDestination = if (AuthRepository().isLoggedIn()) Routes.HOME else Routes.LOGIN

        setContent {
            CerrostourappTheme(dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(startDestination = startDestination)
                }
            }
        }
    }
}