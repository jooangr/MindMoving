package com.example.mindmoving.views.ajustesUsuario

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mindmoving.retrofit.ApiClient
import kotlinx.coroutines.launch
import com.example.mindmoving.retrofit.models.ActualizarUsuarioRequest


@Composable
fun EditarPerfilScreen(navController: NavHostController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val apiService = ApiClient.getApiService()

    var passwordActual by remember { mutableStateOf("") }
    var usernameNuevo by remember { mutableStateOf("") }
    var emailNuevo by remember { mutableStateOf("") }
    var passwordNueva by remember { mutableStateOf("") }

    var verificado by remember { mutableStateOf(false) }

    val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
    val userId = prefs.getString("userId", null)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Editar Perfil", style = MaterialTheme.typography.headlineSmall)

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
                        val response = apiService.verificarPassword(userId ?: "", passwordActual)
                        if (response.isSuccessful && response.body()?.success == true) {
                            verificado = true
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
            OutlinedTextField(
                value = usernameNuevo,
                onValueChange = { usernameNuevo = it },
                label = { Text("Nuevo username") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )

            OutlinedTextField(
                value = emailNuevo,
                onValueChange = { emailNuevo = it },
                label = { Text("Nuevo email") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )

            OutlinedTextField(
                value = passwordNueva,
                onValueChange = { passwordNueva = it },
                label = { Text("Nueva contraseña") },
                visualTransformation = PasswordVisualTransformation(),
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
