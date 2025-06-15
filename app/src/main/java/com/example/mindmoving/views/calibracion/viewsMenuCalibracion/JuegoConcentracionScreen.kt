package com.example.mindmoving.views.calibracion.viewsMenuCalibracion

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mindmoving.neuroSkyService.CustomNeuroSky
import com.example.mindmoving.neuroSkyService.NeuroSkyListener
import com.example.mindmoving.neuroSkyService.NeuroSkyManager
import com.example.mindmoving.views.controlCoche.ConnectionStatus
import com.neurosky.thinkgear.TGDevice
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


/**
 * Función interativa de entrenamiento o juego enfocado en la concentración
 *
 *  Muestra un círculo cuyo tamaño depende del nivel de atención del usuario.
 *  Si la atención supera un umbral (70), se ganan puntos.
 *  Al alcanzar 30 puntos, se muestra un mensaje de éxito.
 */@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JuegoConcentracionScreen(navController: NavHostController) {
    val colorPrimario = MaterialTheme.colorScheme.primary
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Usa el manager
    val neuroSkyManager = remember { NeuroSkyManager(context) }
    val connectionState by neuroSkyManager.connectionState.collectAsState()
    val eegData by neuroSkyManager.eegData.collectAsState()

    var radioCirculo by remember { mutableStateOf(50f) }
    var puntos by remember { mutableStateOf(0) }
    var juegoActivo by remember { mutableStateOf(false) }

    val objetivoAtencion = 70
    val radioMaximo = 300f

    // Conexión al iniciar
    LaunchedEffect(Unit) {
        neuroSkyManager.conectar()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Juego de Concentración") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            // 🟢 Estado conexión
            Text(
                text = when (connectionState) {
                    ConnectionStatus.CONECTADO -> "🔌 Estado: Conectado"
                    ConnectionStatus.CONECTANDO -> "🔄 Estado: Conectando..."
                    ConnectionStatus.DESCONECTADO -> "❌ Estado: Desconectado"
                    ConnectionStatus.ERROR -> "❌ Estado: Desconectado"
                },
                color = when (connectionState) {
                    ConnectionStatus.CONECTADO -> Color.Green
                    ConnectionStatus.CONECTANDO -> Color.Yellow
                    ConnectionStatus.DESCONECTADO -> Color.Red
                    ConnectionStatus.ERROR -> Color.Red
                },
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(16.dp))

            // Atención actual
            Text("Atención actual: ${eegData.attention}", style = MaterialTheme.typography.titleMedium)
            Text("🎯 Objetivo: ≥ $objetivoAtencion", color = Color(0xFF00C853))
            Text("⭐ Puntos: $puntos", style = MaterialTheme.typography.titleMedium)

            Spacer(Modifier.height(32.dp))

            // Botón para empezar
            if (!juegoActivo) {
                Button(onClick = {
                    puntos = 0
                    juegoActivo = true
                }) {
                    Text("Empezar juego")
                }
            }

            // Actualizar círculo si el juego está activo
            LaunchedEffect(eegData.attention, juegoActivo) {
                if (juegoActivo) {
                    val nivel = eegData.attention
                    radioCirculo = (nivel.coerceIn(0, 100) / 100f) * radioMaximo
                    if (nivel >= objetivoAtencion) puntos++
                }
            }

            Spacer(Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 64.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = colorPrimario,
                        radius = radioCirculo,
                        center = center
                    )
                }
            }

            // Resultado final
            if (juegoActivo && puntos >= 30) {
                AlertDialog(
                    onDismissRequest = { juegoActivo = false },
                    title = { Text("🎯 ¡Concentración lograda!") },
                    text = { Text("Has alcanzado $puntos puntos de concentración.") },
                    confirmButton = {
                        TextButton(onClick = {
                            puntos = 0
                            juegoActivo = false
                        }) { Text("Reiniciar") }
                    },
                    dismissButton = {
                        TextButton(onClick = { navController.popBackStack() }) { Text("Salir") }
                    }
                )
            }
        }
    }
}

