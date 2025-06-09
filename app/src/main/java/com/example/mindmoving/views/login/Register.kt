package com.example.mindmoving.views.login


import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.compose.material3.Button
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.mindmoving.retrofit.ApiClient
import com.example.mindmoving.retrofit.models.RegisterRequest
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(navController: NavHostController) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    val apiService = ApiClient.getApiService()

    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text("Error en el registro", color = Color.Black)
            },
            text = {
                Text(dialogMessage, color = Color.Black)
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Aceptar", color = Color(0xFF4CAF50))
                }
            },
            containerColor = Color.White
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF3F2B96),
                        Color(0xFF5C258D),
                        Color(0xFF6A0572)
                    )
                )
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Crear cuenta",
                fontSize = 28.sp,
                color = Color.White,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Nombre de usuario", color = Color(0xFFCCCCCC)) },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.Gray,
                    textColor = Color.White,
                    cursorColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico", color = Color(0xFFCCCCCC)) },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.Gray,
                    textColor = Color.White,
                    cursorColor = Color.White,
                    focusedLabelColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña", color = Color(0xFFCCCCCC)) },
                visualTransformation = PasswordVisualTransformation(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.Gray,
                    textColor = Color.White,
                    cursorColor = Color.White,
                    focusedLabelColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        try {
                            val response = apiService.registerUser(
                                RegisterRequest(
                                    username = username,
                                    email = email,
                                    password = password
                                )
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
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50))
            ) {
                Text("Registrarse", color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = {
                navController.navigate("login") {
                    popUpTo("register") { inclusive = true }
                }
            }) {
                Text("¿Ya tienes cuenta? Inicia sesión", color = Color.LightGray)
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