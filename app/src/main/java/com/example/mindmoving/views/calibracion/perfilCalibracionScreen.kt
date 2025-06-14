package com.example.mindmoving.views.calibracion

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
import com.example.mindmoving.retrofit.models.PerfilCalibracionResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun PerfilCalibracionScreen(navController: NavHostController) {
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
    val usuarioId = sharedPrefs.getString("userId", null)

    var perfil by remember { mutableStateOf<PerfilCalibracionResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

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

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when {
            isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            perfil != null -> PerfilContent(perfil!!, navController)
            else -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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

                    val opciones = listOf(
                        "Equilibrado",
                        "Predominantemente Atento",
                        "Predominantemente Meditativo"
                    )

                    var perfilSeleccionado by remember { mutableStateOf(opciones.first()) }
                    var showDropdown by remember { mutableStateOf(false) }

                    Text("O elige un perfil predefinido:", style = MaterialTheme.typography.bodyMedium)

                    Box {
                        OutlinedTextField(
                            value = perfilSeleccionado,
                            onValueChange = { perfilSeleccionado = it },
                            label = { Text("Selecciona un perfil") },
                            modifier = Modifier.fillMaxWidth().clickable { showDropdown = true },
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

                    Spacer(modifier = Modifier.height(16.dp))
                    val coroutineScope = rememberCoroutineScope()
                    Button(onClick = {
                        coroutineScope.launch {
                            try {
                                val response = ApiClient.getApiService().crearPerfilPredefinido(
                                    id = usuarioId ?: "",
                                    body = mapOf("tipo" to perfilSeleccionado)
                                )

                                if (response.isSuccessful) {
                                    navController.navigate("perfil_calibracion") // o recarga pantalla
                                } else {
                                    Toast.makeText(context, "Error: ${response.code()}", Toast.LENGTH_LONG).show()
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

@Composable
fun PerfilContent(perfil: PerfilCalibracionResponse, navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Perfil de Calibración",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )

        Divider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

        InfoItem(label = "Tipo", value = perfil.tipo)
        InfoItem(label = "Atención", value = perfil.valoresAtencion?.toString() ?: "No disponible")
        InfoItem(label = "Meditación", value = perfil.valoresMeditacion?.toString() ?: "No disponible")
        InfoItem(label = "Alternancia", value = perfil.alternancia?.toString() ?: "No disponible")
        InfoItem(label = "Blinking", value = perfil.blinking?.toString() ?: "No disponible")



        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = { navController.navigate("calibracion_inicio") }) {
            Text("Editar perfil personalizadamente")
        }

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
                            scope.launch {
                                try {
                                    val res = ApiClient.getApiService().actualizarTipoPerfil(
                                        userId = perfil.usuarioId,
                                        body = mapOf("tipo" to opcion)
                                    )
                                    if (res.isSuccessful) {
                                        Toast.makeText(context, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Error al actualizar", Toast.LENGTH_SHORT).show()
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

@Composable
fun InfoItem(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
