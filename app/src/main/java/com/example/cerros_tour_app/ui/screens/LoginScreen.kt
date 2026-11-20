package com.example.cerros_tour_app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cerros_tour_app.ui.theme.CerrostourappTheme
import com.example.cerros_tour_app.ui.viewmodel.LoginUiState
import com.example.cerros_tour_app.ui.viewmodel.LoginViewModel

/**
 * Misma firma que antes: MainActivity y AppNavigation no cambian.
 * onLoginClick ahora solo se llama cuando Firebase confirma el inicio de sesión.
 */
@Composable
fun LoginScreen(
    onLoginClick: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onLoginClick()
            viewModel.onSuccessHandled()
        }
    }

    LoginContent(
        uiState = uiState,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onSubmit = viewModel::login,
        onNavigateToRegister = onNavigateToRegister
    )
}

/** Parte visual sin Firebase: es la que se usa en el Preview. */
@Composable
fun LoginContent(
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Cerros Tour",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Descubre los miradores de los cerros orientales",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = uiState.email,
                onValueChange = onEmailChange,
                label = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                enabled = !uiState.isLoading,
                isError = uiState.emailError != null,
                supportingText = uiState.emailError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                enabled = !uiState.isLoading,
                isError = uiState.passwordError != null,
                supportingText = uiState.passwordError?.let { { Text(it) } },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { onSubmit() })
            )

            uiState.errorMessage?.let { message ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onSubmit,
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(text = "Iniciar Sesión", fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = onNavigateToRegister,
                enabled = !uiState.isLoading
            ) {
                Text(text = "¿No tienes cuenta? Regístrate aquí")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    CerrostourappTheme(dynamicColor = false) {
        LoginContent(
            uiState = LoginUiState(email = "correo@udistrital.edu.co"),
            onEmailChange = {},
            onPasswordChange = {},
            onSubmit = {},
            onNavigateToRegister = {}
        )
    }
}

@Preview(showBackground = true, name = "Login con errores")
@Composable
fun LoginScreenErrorPreview() {
    CerrostourappTheme(dynamicColor = false) {
        LoginContent(
            uiState = LoginUiState(
                email = "correo-malo",
                emailError = "El correo no es válido",
                passwordError = "Ingresa tu contraseña",
                errorMessage = "Correo o contraseña incorrectos."
            ),
            onEmailChange = {},
            onPasswordChange = {},
            onSubmit = {},
            onNavigateToRegister = {}
        )
    }
}