package com.example.mindmoving.views.login

import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.mindmoving.retrofit.ApiClient
import com.example.mindmoving.retrofit.models.login_register.RegisterRequest
import kotlinx.coroutines.launch
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.sp
import com.example.mindmoving.ui.theme.AppTheme
import com.example.mindmoving.ui.theme.AppTypography


// Pantalla principal de registro. Se fuerza el tema oscuro para mantener coherencia visual.
@Composable
fun RegisterScreen(navController: NavHostController) {
    AppTheme(darkTheme = true) {
        RegisterContent(navController)
    }
}

@Composable
fun RegisterContent(navController: NavHostController) {

    // Estados para almacenar el input del usuario
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    val apiService = ApiClient.getApiService()

    // Estados para el manejo del diálogo de error
    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Degradado vertical como fondo
    val gradientBackground = Brush.verticalGradient(
        colors = listOf(Color(0xFF0A0A23), Color(0xFF1A1A40))
    )

    // Diálogo de carga (idéntico al del login)
    if (isLoading) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Registrando cuenta...", color = MaterialTheme.colorScheme.primary) },
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Por favor espera...", color = MaterialTheme.colorScheme.onSurface)
                }
            },
            confirmButton = {},
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // Diálogo de error
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Error en el registro", color = MaterialTheme.colorScheme.onSurface) },
            text = { Text(dialogMessage, color = MaterialTheme.colorScheme.onSurface) },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Aceptar", color = MaterialTheme.colorScheme.onSurface)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // Contenedor principal de la interfaz de registro
    Box(
        modifier = Modifier.fillMaxSize().background(gradientBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Título
            Text(
                text = "Crear cuenta",
                color = Color.White,
                style = AppTypography.titleMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Campo de nombre de usuario
            inputField(username, { username = it }, "Nombre de usuario",
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )

            // Campo de email
            inputField(email, { email = it }, "Correo electrónico",
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )

            // Campo de contraseña personalizado
            PasswordFieldR(
                value = password,
                onValueChange = { password = it },
                placeholder = "Contraseña",
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Botón para enviar el formulario de registro
            Button(
                onClick = {
                    coroutineScope.launch {
                        isLoading = true
                        try {
                            if (username.isBlank() || email.isBlank() || password.isBlank()) {
                                dialogMessage = "Completa todos los campos"
                                showDialog = true
                                isLoading = false
                                return@launch
                            }

                            val response = apiService.registerUser(
                                RegisterRequest(username.trim(), email.trim(), password.trim())
                            )

                            if (response.isSuccessful) {
                                isLoading = false
                                navController.navigate("login") {
                                    popUpTo("register") { inclusive = true }
                                }
                            } else {
                                val errorBody = response.errorBody()?.string()
                                dialogMessage = when {
                                    errorBody?.contains("Correo ya registrado") == true -> "Ese correo ya está en uso"
                                    errorBody?.contains("Nombre de usuario ya registrado") == true -> "Ese nombre de usuario ya está en uso"
                                    else -> "Ocurrió un error. Intenta nuevamente"
                                }
                                showDialog = true
                                isLoading = false
                            }
                        } catch (e: Exception) {
                            dialogMessage = "Error de red: ${e.message}"
                            showDialog = true
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(40),
                contentPadding = PaddingValues(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            ),
                            shape = RoundedCornerShape(40)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Registrarse",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = AppTypography.bodyMedium,
                        fontSize = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = {
                navController.navigate("login") {
                    popUpTo("register") { inclusive = true }
                }
            }) {
                Text(
                    "¿Ya tienes cuenta? Inicia sesión",
                    color = MaterialTheme.colorScheme.primary,
                    style = AppTypography.bodySmall
                )
            }
        }
    }
}


// Vista previa en diseño (para editores tipo Android Studio)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewRegisterScreen() {
    val navController = rememberNavController()
    RegisterScreen(navController = navController)
}

// Campo de contraseña reutilizable con visibilidad activable
@Composable
fun PasswordFieldR(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                fontSize = 16.sp
            )
        },
        shape = RoundedCornerShape(40),
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
            errorContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
            disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent
        ),
        textStyle = AppTypography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface),
        modifier = modifier
    )
}

// Campo de texto reutilizable para inputs como email y username
@Composable
fun inputField(
    value: String,
    onChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        placeholder = {
            Text(placeholder, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        },
        shape = RoundedCornerShape(40),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
            disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
            errorContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent
        ),
        textStyle = AppTypography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface),
        singleLine = true,
        modifier = modifier
    )
}
