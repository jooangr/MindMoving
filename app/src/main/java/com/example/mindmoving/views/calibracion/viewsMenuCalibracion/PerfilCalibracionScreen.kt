package com.example.mindmoving.views.calibracion.viewsMenuCalibracion

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mindmoving.retrofit.ApiClient
import com.example.mindmoving.retrofit.models.user.BlinkingData
import com.example.mindmoving.retrofit.models.perfilCalibracion.PerfilCalibracion
import com.example.mindmoving.retrofit.models.perfilCalibracion.PerfilCalibracionRequest
import com.example.mindmoving.retrofit.models.perfilCalibracion.PerfilCalibracionResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Pantalla principal de gestión del perfil de calibración del usuario.
 *
 * Si el usuario ya tiene un perfil, se muestra con detalles y opciones para modificarlo.
 * Si no lo tiene, se le da la opción de crear uno o asignar uno predefinido manualmente.
 * Se conecta con la API para consultar, crear o actualizar el perfil.
 */

@Composable
fun PerfilCalibracionScreen(navController: NavHostController) {
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
    val usuarioId = sharedPrefs.getString("userId", null)

    var perfil by remember { mutableStateOf<PerfilCalibracionResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // Llama a la API para obtener el perfil del usuario cuando se inicia el Composable
    LaunchedEffect(usuarioId) {
        if (usuarioId != null) {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.getApiService().getPerfil(usuarioId)
                }
                if (response.isSuccessful) {
                    perfil = response.body()
                } else {
                    error = "No tienes un perfil de calibración creado."
                }
            } catch (e: Exception) {
                error = "Error al cargar el perfil."
            } finally {
                isLoading = false
            }
        } else {
            error = "Usuario no identificado."
            isLoading = false
        }
    }

    // UI principal según el estado
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when {
            isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }

            perfil != null -> PerfilContent(perfil!!, navController)

            else -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = error ?: "No tienes un perfil de calibración.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(onClick = { navController.navigate("calibracion_inicio") }) {
                        Text("Crear perfil")
                    }

                    // Menú para seleccionar perfil predefinido
                    val opciones = listOf("Equilibrado", "Predominantemente Atento", "Predominantemente Meditativo")
                    var perfilSeleccionado by remember { mutableStateOf(opciones.first()) }
                    var showDropdown by remember { mutableStateOf(false) }

                    Text("O elige un perfil predefinido:", style = MaterialTheme.typography.bodyMedium)

                    Box {
                        OutlinedTextField(
                            value = perfilSeleccionado,
                            onValueChange = { perfilSeleccionado = it },
                            label = { Text("Selecciona un perfil") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showDropdown = true },
                            readOnly = true
                        )

                        DropdownMenu(
                            expanded = showDropdown,
                            onDismissRequest = { showDropdown = false }
                        ) {
                            opciones.forEach { opcion ->
                                DropdownMenuItem(
                                    text = { Text(opcion) },
                                    onClick = {
                                        perfilSeleccionado = opcion
                                        showDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Asignar el perfil predefinido mediante API
                    Spacer(modifier = Modifier.height(16.dp))
                    val coroutineScope = rememberCoroutineScope()

                    Button(onClick = {
                        coroutineScope.launch {
                            try {
                                Toast.makeText(context, "Preparando request...", Toast.LENGTH_SHORT).show()

                                val perfilEnum = PerfilCalibracion.values().firstOrNull { it.nombre == perfilSeleccionado }

                                if (perfilEnum != null) {
                                    val request = PerfilCalibracionRequest(
                                        usuarioId = usuarioId ?: "",
                                        tipo = perfilEnum.nombre,
                                        valoresAtencion = perfilEnum.valoresAtencion,
                                        valoresMeditacion = perfilEnum.valoresMeditacion,
                                        alternancia = perfilEnum.alternancia,
                                        blinking = BlinkingData(30, 60)
                                    )

                                    val response = ApiClient.getApiService().crearPerfil(request)

                                    if (response.isSuccessful) {
                                        Toast.makeText(context, "Perfil asignado correctamente", Toast.LENGTH_SHORT).show()
                                        navController.navigate("perfil_calibracion")
                                    } else {
                                        Toast.makeText(context, "Error: ${response.code()}", Toast.LENGTH_LONG).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Perfil no encontrado", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Excepción: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }) {
                        Text("Asignar perfil predefinido")
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("Volver")
                    }
                }
            }
        }
    }
}

/**
 * Muestra el contenido cuando ya existe un perfil de calibración.
 * Incluye detalles del perfil y opción para actualizarlo manualmente.
 */
@Composable
fun PerfilContent(perfil: PerfilCalibracionResponse, navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Perfil de Calibración", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        Divider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

        InfoItem("Tipo", perfil.tipo)
        InfoItem("Atención", perfil.valoresAtencion?.toString() ?: "No disponible")
        InfoItem("Meditación", perfil.valoresMeditacion?.toString() ?: "No disponible")
        InfoItem("Alternancia", perfil.alternancia?.toString() ?: "No disponible")
        InfoItem("Blinking", perfil.blinking?.toString() ?: "No disponible")

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = { navController.navigate("calibracion_inicio") }) {
            Text("Editar perfil personalizadamente")
        }

        // Menú para reasignar perfil desde valores predefinidos
        val opciones = listOf("Equilibrado", "Predominantemente Atento", "Predominantemente Meditativo")
        var expanded by remember { mutableStateOf(false) }
        var seleccionNueva by remember { mutableStateOf("") }
        val context = LocalContext.current
        val scope = rememberCoroutineScope()

        Text("Asignar tipo manualmente:", style = MaterialTheme.typography.labelLarge)
        Box {
            Button(onClick = { expanded = true }) {
                Text("Seleccionar tipo")
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                opciones.forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(opcion) },
                        onClick = {
                            expanded = false
                            seleccionNueva = opcion
                            val perfilEnum = PerfilCalibracion.values().firstOrNull { it.nombre == opcion }

                            scope.launch {
                                try {
                                    val request = PerfilCalibracionRequest(
                                        usuarioId = perfil.usuarioId,
                                        tipo = perfilEnum?.nombre ?: opcion,
                                        valoresAtencion = perfilEnum?.valoresAtencion ?: perfil.valoresAtencion!!,
                                        valoresMeditacion = perfilEnum?.valoresMeditacion ?: perfil.valoresMeditacion!!,
                                        alternancia = perfilEnum?.alternancia ?: perfil.alternancia!!,
                                        blinking = BlinkingData(30, 60)
                                    )

                                    val res = ApiClient.getApiService().actualizarPerfil(request)

                                    if (res.isSuccessful) {
                                        val perfilActualizado = ApiClient.getApiService().getPerfil(perfil.usuarioId)
                                        if (perfilActualizado.isSuccessful) {
                                            val tipo = perfilActualizado.body()?.tipo
                                            val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
                                            prefs.edit().putString("perfil_tipo", tipo).apply()
                                        }

                                        navController.navigate("perfil_calibracion") {
                                            popUpTo("perfil_calibracion") { inclusive = true }
                                        }

                                        Toast.makeText(context, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Error al actualizar: ${res.code()}", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Fallo: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }
            }
        }

        TextButton(onClick = { navController.popBackStack() }) {
            Text("Volver")
        }
    }
}

/**
 * Muestra una fila con una etiqueta y su valor correspondiente, usada para mostrar los campos del perfil.
 */
@Composable
fun InfoItem(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
        Text(value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}

