package com.example.cerros_tour_app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cerros_tour_app.ui.theme.CerrostourappTheme
import com.example.cerros_tour_app.ui.viewmodel.RegisterUiState
import com.example.cerros_tour_app.ui.viewmodel.RegisterViewModel

/** Misma firma que antes: MainActivity y AppNavigation no cambian. */
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit,
    viewModel: RegisterViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onRegisterSuccess()
            viewModel.onSuccessHandled()
        }
    }

    RegisterContent(
        uiState = uiState,
        onNameChange = viewModel::onNameChange,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onSubmit = viewModel::register,
        onBackToLogin = onBackToLogin
    )
}

@Composable
fun RegisterContent(
    uiState: RegisterUiState,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBackToLogin: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        // Scroll + imePadding: con 4 campos el teclado puede tapar el botón.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Crear Cuenta",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = uiState.name,
                onValueChange = onNameChange,
                label = { Text("Nombre completo") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                enabled = !uiState.isLoading,
                isError = uiState.nameError != null,
                supportingText = uiState.nameError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

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
                supportingText = {
                    Text(uiState.passwordError ?: "Mínimo 6 caracteres")
                },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.confirmPassword,
                onValueChange = onConfirmPasswordChange,
                label = { Text("Confirmar contraseña") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                enabled = !uiState.isLoading,
                isError = uiState.confirmPasswordError != null,
                supportingText = uiState.confirmPasswordError?.let { { Text(it) } },
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
                    Text(text = "Registrarse", fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = onBackToLogin,
                enabled = !uiState.isLoading
            ) {
                Text(text = "¿Ya tienes cuenta? Inicia sesión")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    CerrostourappTheme(dynamicColor = false) {
        RegisterContent(
            uiState = RegisterUiState(
                name = "Emily Castillo",
                confirmPasswordError = "Las contraseñas no coinciden"
            ),
            onNameChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onSubmit = {},
            onBackToLogin = {}
        )
    }
}