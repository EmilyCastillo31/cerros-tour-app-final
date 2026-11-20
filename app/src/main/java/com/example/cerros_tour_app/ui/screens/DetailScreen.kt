package com.example.cerros_tour_app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.cerros_tour_app.data.Tour

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    tour: Tour,
    onBack: () -> Unit,
    isFavorite: Boolean = false,              // [ROL 1] favoritos
    onToggleFavorite: (() -> Unit)? = null,   // [ROL 1] favoritos
    onBookClick: (() -> Unit)? = null         // botón para ir a la reserva
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tour.name) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Volver")
                    }
                },
                actions = {
                    // [ROL 1] Marcar o desmarcar como favorito
                    if (onToggleFavorite != null) {
                        TextButton(onClick = onToggleFavorite) {
                            Text(
                                text = if (isFavorite) "♥" else "♡",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {
                Image(
                    painter = painterResource(tour.image),
                    contentDescription = tour.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp),
                    contentScale = ContentScale.Crop
                )
            }

            item {
                Text(
                    text = tour.name,
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            item {
                Text(
                    text = "Sobre este destino",
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = tour.detailedDescription,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            item {
                Text(
                    text = "Galería de imágenes",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            item {
                LazyRow(
                    horizontalArrangement =
                        Arrangement.spacedBy(12.dp)
                ) {
                    items(tour.gallery) { image ->

                        Image(
                            painter = painterResource(image),
                            contentDescription = "Imagen del destino",
                            modifier = Modifier
                                .width(220.dp)
                                .height(150.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Puntos de interés",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            items(tour.pointsOfInterest) { point ->

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "📍 $point",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            if (onBookClick != null) {
                item {
                    Button(
                        onClick = onBookClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Reservar este recorrido")
                    }
                }
            }
        }
    }
}