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

    val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
    val userId = prefs.getString("userId", null)

    var usernameNuevo by remember { mutableStateOf("") }
    var emailNuevo by remember { mutableStateOf("") }
    var passwordNueva by remember { mutableStateOf("") }
    var mostrarPassword by remember { mutableStateOf(false) }

    var passwordEditable by remember { mutableStateOf(false) }

    var passwordActualDialog by remember { mutableStateOf("") }
    var showPasswordCheckDialog by remember { mutableStateOf(false) }

    var passwordConfirmarDialog by remember { mutableStateOf("") }
    var showConfirmPasswordDialog by remember { mutableStateOf(false) }

    var userActual by remember { mutableStateOf<UsuarioResponse?>(null) }

    LaunchedEffect(Unit) {
        userId?.let {
            val response = apiService.getUsuario(it)
            if (response.isSuccessful) {
                val user = response.body()
                userActual = user
                usernameNuevo = user?.username ?: ""
                emailNuevo = user?.email ?: ""
            }

        }
    }

    val usernameLabel = if (userActual != null) {
        "Nuevo username (Actual: ${userActual!!.username})"
    } else {
        "Nuevo username"
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF3F51B5), Color(0xFFB0C4DE))))
            .padding(24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Text("Editar Perfil", style = MaterialTheme.typography.headlineSmall, color = Color.White)

        Spacer(modifier = Modifier.height(35.dp))

        OutlinedTextField(
            value = usernameNuevo,
            onValueChange = { usernameNuevo = it },
            label = { Text(usernameLabel) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = emailNuevo,
            onValueChange = { emailNuevo = it },
            label = { Text("Nuevo email (Actual: ${userActual?.email ?: "..."})") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = passwordNueva,
            onValueChange = { passwordNueva = it },
            label = { Text("Nueva contraseña (opcional)") },
            enabled = passwordEditable,
            visualTransformation = if (mostrarPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val icono = if (mostrarPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    modifier = Modifier.clickable { mostrarPassword = !mostrarPassword }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (!passwordEditable) {
                        showPasswordCheckDialog = true
                    }
                }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = {
            if (passwordNueva.isNotBlank()) {
                showConfirmPasswordDialog = true
            } else {
                // Sin cambiar contraseña
                coroutineScope.launch {
                    try {
                        val response = apiService.actualizarUsuario(
                            userId ?: "",
                            ActualizarUsuarioRequest(usernameNuevo, emailNuevo, "")
                        )
                        if (response.isSuccessful) {
                            Toast.makeText(context, "Datos actualizados", Toast.LENGTH_SHORT).show()
                            navController.navigate("menu")
                        } else {
                            if (response.code() == 409) {
                                Toast.makeText(context, "Email o username ya en uso", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Error al actualizar", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }) {
            Text("Guardar cambios")
        }
    }

    // Dialogo para pedir la contraseña actual
    if (showPasswordCheckDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordCheckDialog = false },
            title = { Text("Verifica tu contraseña actual") },
            text = {
                OutlinedTextField(
                    value = passwordActualDialog,
                    onValueChange = { passwordActualDialog = it },
                    label = { Text("Contraseña actual") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        try {
                            val res = apiService.verificarPasswordEditarPerfil(
                                userId ?: "",
                                VerificarPasswordRequest(password = passwordActualDialog)
                            )
                            if (res.isSuccessful && res.body()?.success == true) {
                                passwordEditable = true
                                Toast.makeText(context, "Contraseña verificada", Toast.LENGTH_SHORT).show()
                                showPasswordCheckDialog = false
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
            },
            dismissButton = {
                TextButton(onClick = { showPasswordCheckDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Dialogo para confirmar la nueva contraseña antes de guardar
    if (showConfirmPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmPasswordDialog = false },
            title = { Text("Confirmar nueva contraseña") },
            text = {
                OutlinedTextField(
                    value = passwordConfirmarDialog,
                    onValueChange = { passwordConfirmarDialog = it },
                    label = { Text("Repite nueva contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (passwordConfirmarDialog == passwordNueva) {
                        coroutineScope.launch {
                            try {
                                val response = apiService.actualizarUsuario(
                                    userId ?: "",
                                    ActualizarUsuarioRequest(usernameNuevo, emailNuevo, passwordNueva)
                                )
                                if (response.isSuccessful) {
                                    Toast.makeText(context, "Datos actualizados", Toast.LENGTH_SHORT).show()
                                    navController.navigate("menu")
                                } else {
                                    if (response.code() == 409) {
                                        Toast.makeText(context, "Email o username ya en uso", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Error al actualizar", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                        showConfirmPasswordDialog = false
                    } else {
                        Toast.makeText(context, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmPasswordDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
