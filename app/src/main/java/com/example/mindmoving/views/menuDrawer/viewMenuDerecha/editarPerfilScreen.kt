package com.example.mindmoving.views.menuDrawer.viewMenuDerecha


import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mindmoving.retrofit.ApiClient
import com.example.mindmoving.retrofit.models.user.ActualizarUsuarioRequest
import com.example.mindmoving.retrofit.models.user.UsuarioResponse
import com.example.mindmoving.retrofit.models.verificarPassword.VerificarPasswordRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
@Composable
fun EditarPerfilScreen(navController: NavHostController) {

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val apiService = ApiClient.getApiService()

    val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
    val userId = prefs.getString("userId", null)

    // Campos editables del usuario
    var usernameNuevo by remember { mutableStateOf("") }
    var emailNuevo by remember { mutableStateOf("") }
    var passwordNueva by remember { mutableStateOf("") }
    var mostrarPassword by remember { mutableStateOf(false) }
    var passwordEditable by remember { mutableStateOf(false) }

    // Diálogo para verificar contraseña actual
    var passwordActualDialog by remember { mutableStateOf("") }
    var showPasswordCheckDialog by remember { mutableStateOf(false) }

    // Diálogo para confirmar nueva contraseña
    var passwordConfirmarDialog by remember { mutableStateOf("") }
    var showConfirmPasswordDialog by remember { mutableStateOf(false) }

    // Información del usuario actual y mensajes
    var userActual by remember { mutableStateOf<UsuarioResponse?>(null) }
    var showMessageDialog by remember { mutableStateOf(false) }
    var messageText by remember { mutableStateOf("") }

    // Color de fondo según tema
    val isDark = isSystemInDarkTheme()
    val backgroundColor = if (isDark) Color(0xFF121212) else Color(0xFFF2F3FC)

    // Estados para eliminar usuario
    var showConfirmDeleteDialog by remember { mutableStateOf(false) }
    var showPasswordDeleteDialog by remember { mutableStateOf(false) }
    var passwordEliminarCuenta by remember { mutableStateOf("") }

    // Obtener datos del usuario al cargar la pantalla
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

    // Etiqueta personalizada para mostrar el username actual
    val usernameLabel = if (userActual != null) {
        "Nuevo username (Actual: ${userActual!!.username})"
    } else {
        "Nuevo username"
    }

    // Contenido principal de la pantalla
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Título
        Text("Editar Perfil", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)

        Spacer(modifier = Modifier.height(8.dp))

        // Botón para volver al menú
        IconButton(
            onClick = { navController.navigate("menu") },
            modifier = Modifier.align(Alignment.Start)
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Volver al menú",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(35.dp))

        // Campo de username
        OutlinedTextField(
            value = usernameNuevo,
            onValueChange = { usernameNuevo = it },
            label = { Text(usernameLabel) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de email
        OutlinedTextField(
            value = emailNuevo,
            onValueChange = { emailNuevo = it },
            label = { Text("Nuevo email (Actual: ${userActual?.email ?: "..."})") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de nueva contraseña (deshabilitado por defecto)
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

        // Botón para guardar cambios
        Button(onClick = {
            if (passwordNueva.isNotBlank()) {
                showConfirmPasswordDialog = true
            } else {
                coroutineScope.launch {
                    try {
                        val response = apiService.actualizarUsuario(
                            userId ?: "",
                            ActualizarUsuarioRequest(usernameNuevo, emailNuevo, "")
                        )
                        messageText = if (response.isSuccessful) {
                            navController.navigate("menu")
                            "Datos actualizados"
                        } else if (response.code() == 409) {
                            "Email o username ya en uso"
                        } else {
                            "Error al actualizar"
                        }
                        showMessageDialog = true
                    } catch (e: Exception) {
                        messageText = "Error: ${e.message}"
                        showMessageDialog = true
                    }
                }
            }
        }) {
            Text("Guardar cambios")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botón para eliminar cuenta
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { showConfirmDeleteDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
            Text("Eliminar cuenta", color = Color.White)
        }
    }

    // Diálogo para verificar contraseña actual
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
                                showPasswordCheckDialog = false
                                messageText = "Contraseña verificada"
                            } else {
                                messageText = "Contraseña incorrecta"
                            }
                            showMessageDialog = true
                        } catch (e: Exception) {
                            messageText = "Error: ${e.message}"
                            showMessageDialog = true
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

    // Diálogo para confirmar la nueva contraseña
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
                                messageText = if (response.isSuccessful) {
                                    navController.navigate("menu")
                                    "Datos actualizados"
                                } else if (response.code() == 409) {
                                    "Email o username ya en uso"
                                } else {
                                    "Error al actualizar"
                                }
                                showMessageDialog = true
                            } catch (e: Exception) {
                                messageText = "Error: ${e.message}"
                                showMessageDialog = true
                            }
                        }
                        showConfirmPasswordDialog = false
                    } else {
                        messageText = "Las contraseñas no coinciden"
                        showMessageDialog = true
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

    // Diálogo informativo
    if (showMessageDialog) {
        AlertDialog(
            onDismissRequest = { showMessageDialog = false },
            title = { Text("Información") },
            text = { Text(messageText) },
            confirmButton = {
                TextButton(onClick = { showMessageDialog = false }) {
                    Text("Aceptar")
                }
            }
        )
    }

    // Diálogo de confirmación de eliminación
    if (showConfirmDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDeleteDialog = false },
            title = { Text("¿Estás seguro?") },
            text = { Text("Esta acción eliminará tu cuenta permanentemente.") },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDeleteDialog = false
                    showPasswordDeleteDialog = true
                }) {
                    Text("Sí, eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo para ingresar contraseña antes de eliminar cuenta
    if (showPasswordDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordDeleteDialog = false },
            title = { Text("Verifica tu contraseña") },
            text = {
                OutlinedTextField(
                    value = passwordEliminarCuenta,
                    onValueChange = { passwordEliminarCuenta = it },
                    label = { Text("Contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        try {
                            val response = apiService.eliminarUsuario(
                                userId = userId ?: "",
                                password = passwordEliminarCuenta
                            )

                            if (response.isSuccessful) {
                                messageText = "Usuario eliminado correctamente"
                                showMessageDialog = true

                                coroutineScope.launch {
                                    delay(1500)
                                    val prefsEditor = prefs.edit()
                                    prefsEditor.clear().apply()
                                    navController.navigate("login") {
                                        popUpTo(0)
                                    }
                                }
                            } else {
                                messageText = "Contraseña incorrecta o error al eliminar"
                                showMessageDialog = true
                            }

                        } catch (e: Exception) {
                            messageText = "Error: ${e.message}"
                            showMessageDialog = true
                        }
                    }
                    showPasswordDeleteDialog = false
                }) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}



