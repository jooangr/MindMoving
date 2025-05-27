package com.example.mindmoving.views.menuDrawer.viewMenuDerecha

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.mindmoving.retrofit.ApiClient
import com.example.mindmoving.retrofit.models.ActualizarUsuarioRequest
import com.example.mindmoving.retrofit.models.UsuarioResponse
import com.example.mindmoving.retrofit.models.VerificarPasswordRequest
import kotlinx.coroutines.launch

@Composable
fun EditarPerfilScreen(navController: NavHostController) {

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val apiService = ApiClient.getApiService()

    var passwordActual by remember { mutableStateOf("") }
    var usernameNuevo by remember { mutableStateOf("") }
    var emailNuevo by remember { mutableStateOf("") }
    var passwordNueva by remember { mutableStateOf("") }
    var mostrarPassword by remember { mutableStateOf(false) }

    var verificado by remember { mutableStateOf(false) }

    val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
    val userId = prefs.getString("userId", null)


    //Guardar varibales locales
    var userActual by remember { mutableStateOf<UsuarioResponse?>(null) }
    var emailActual by remember { mutableStateOf<UsuarioResponse?>(null) }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF3F51B5), Color(0xFFB0C4DE))
                )
            )
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Text("Editar Perfil", style = MaterialTheme.typography.headlineSmall, color = Color.White)

        if (!verificado) {
            OutlinedTextField(
                value = passwordActual,
                onValueChange = { passwordActual = it },
                label = { Text("Introduce tu contraseña actual") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )

            Button(onClick = {
                coroutineScope.launch {
                    try {
                        val response = apiService.verificarPasswordEditarPerfil(
                            userId ?: "",
                            VerificarPasswordRequest(password = passwordActual)
                        )

                        if (response.isSuccessful && response.body()?.success == true) {
                            verificado = true

                            val userResponse = apiService.getUsuario(userId ?: "")
                            if (userResponse.isSuccessful) {
                                val user = userResponse.body()
                                userActual = user
                                emailActual = (user?.email ?: "") as UsuarioResponse?
                                usernameNuevo = user?.username ?: ""
                                emailNuevo = user?.email ?: ""
                            }
                        } else {
                            Toast.makeText(context, "Contraseña incorrecta", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }) {
                Text("Verificar")
            }

        } else {
            Spacer(modifier = Modifier.height(35.dp))
            // Usuario
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text(
                    text = "Nuevo username (Actual: ${userActual?.username ?: "..."})",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
                )

                OutlinedTextField(
                    value = usernameNuevo,
                    onValueChange = { usernameNuevo = it },
                    placeholder = { Text("Escribe tu nuevo username") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(29.dp))

// Email
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text(
                    text = "Nuevo email (Actual: ${userActual?.email ?: "..."})",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
                )
                OutlinedTextField(
                    value = emailNuevo,
                    onValueChange = { emailNuevo = it },
                    placeholder = { Text("Escribe tu nuevo email") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(29.dp))

            OutlinedTextField(
                value = passwordNueva,
                onValueChange = { passwordNueva = it },
                label = { Text("Nueva contraseña)") },
                visualTransformation = if (mostrarPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icono = if (mostrarPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                    Icon(
                        imageVector = icono,
                        contentDescription = null,
                        modifier = Modifier.clickable { mostrarPassword = !mostrarPassword }
                    )
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )

            Button(onClick = {
                coroutineScope.launch {
                    try {
                        val updateResponse = apiService.actualizarUsuario(
                            userId = userId ?: "",
                            request = ActualizarUsuarioRequest(
                                username = usernameNuevo,
                                email = emailNuevo,
                                password = passwordNueva
                            )
                        )

                        if (updateResponse.isSuccessful) {
                            Toast.makeText(context, "Datos actualizados", Toast.LENGTH_SHORT).show()
                            navController.navigate("menu")
                        } else {
                            Toast.makeText(context, "Error al actualizar", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }) {
                Text("Guardar cambios")
            }
        }
    }
}
