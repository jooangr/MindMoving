package com.example.mindmoving.views.login

import androidx.compose.material3.OutlinedTextFieldDefaults
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.example.mindmoving.retrofit.models.RegisterRequest
import kotlinx.coroutines.launch
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import com.example.mindmoving.R
import com.example.mindmoving.ui.theme.AppTypography


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavHostController) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    val apiService = ApiClient.getApiService()

    // Error y avisos
    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Error en el registro", color = Color.Black) },
            text = { Text(dialogMessage, color = Color.Black) },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Aceptar", color = MaterialTheme.colorScheme.primary)
                }
            },
            containerColor = Color.White
        )
    }

    // Fondo degradado como en login
    Box(
        modifier = Modifier.fillMaxSize().padding(),
    ) {
        Image(
            painter = painterResource(id = R.drawable.fondo_5),
            contentDescription = "Fondo de pantalla",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Crear cuenta",
                style = AppTypography.titleMedium.copy(color = Color.White),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Campo: Nombre de usuario
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                placeholder = {
                    Text("Nombre de usuario", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                },
                shape = RoundedCornerShape(40),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = Color.White,

                    focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                    disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                    errorContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                ),
                textStyle = AppTypography.bodySmall.copy(color = Color.White),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            // Campo: Correo electrónico
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = {
                    Text("Correo electrónico", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                },
                shape = RoundedCornerShape(40),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color(0xFF1F1A33).copy(alpha = 0.4f),
                    unfocusedContainerColor = Color(0xFF1F1A33).copy(alpha = 0.4f),
                    disabledContainerColor = Color(0xFF1F1A33).copy(alpha = 0.4f),
                    errorContainerColor = Color(0xFF1F1A33).copy(alpha = 0.4f),
                    cursorColor = Color.White
                ),
                textStyle = AppTypography.bodySmall.copy(color = Color.White),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            // Campo: Contraseña
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = {
                    Text("Contraseña", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                },
                shape = RoundedCornerShape(40),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color(0xFF1F1A33).copy(alpha = 0.4f),
                    unfocusedContainerColor = Color(0xFF1F1A33).copy(alpha = 0.4f),
                    disabledContainerColor = Color(0xFF1F1A33).copy(alpha = 0.4f),
                    errorContainerColor = Color(0xFF1F1A33).copy(alpha = 0.4f),
                    cursorColor = Color.White
                ),
                textStyle = AppTypography.bodySmall.copy(color = Color.White),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Botón de registro con degradado como en login
            Button(
                onClick = {
                    coroutineScope.launch {
                        try {
                            val response = apiService.registerUser(
                                RegisterRequest(username.trim(), email.trim(), password.trim())
                            )
                            if (response.isSuccessful) {
                                navController.navigate("login") {
                                    popUpTo("register") { inclusive = true }
                                }
                            } else {
                                val errorBody = response.errorBody()?.string()
                                Log.e("Registro", "Error: $errorBody")
                                dialogMessage = when {
                                    errorBody?.contains("Correo ya registrado") == true -> "Ese correo ya está en uso"
                                    errorBody?.contains("Nombre de usuario ya registrado") == true -> "Ese nombre de usuario ya está en uso"
                                    else -> "Ocurrió un error. Intenta nuevamente"
                                }
                                showDialog = true
                            }
                        } catch (e: Exception) {
                            dialogMessage = "Error de red: ${e.message}"
                            showDialog = true
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(40),
                contentPadding = PaddingValues(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                            ),
                            shape = RoundedCornerShape(40)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Registrarse", color = Color.White, style = AppTypography.bodyMedium)
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
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                    style = AppTypography.bodySmall
                )
            }
        }
    }
}



@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewRegisterScreen() {
    val navController = rememberNavController()
    RegisterScreen(navController = navController)
}