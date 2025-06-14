package com.example.mindmoving.views.calibracion

import android.content.Context
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

    Surface(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            perfil != null -> PerfilContent(perfil!!, navController)
            else -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = error ?: "No tienes un perfil de calibración.")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { navController.navigate("calibracion_inicio") }) {
                        Text("Crear perfil")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
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
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text("Tipo de Perfil: ${perfil.tipo}", style = MaterialTheme.typography.titleLarge)
        Text("Atención: ${perfil.valoresAtencion}")
        Text("Meditación: ${perfil.valoresMeditacion}")
        Text("Alternancia: ${perfil.alternancia}")
        Text("Blinking: ${perfil.blinking}")

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { navController.navigate("calibracion_inicio") },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Editar perfil")
        }

        TextButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Volver")
        }
    }
}