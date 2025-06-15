package com.example.mindmoving.views.calibracion.viewsMenuCalibracion

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
 * Pantalla de juego para entrenar la meditación
 *
 * Muestra una barra de progreso que refleja el nivel de meditación en tiempo real.
 * Si el nivel supera un umbral (70), se ganan puntos.
 * Al alcanzar 20 puntos, se muestra un mensaje de éxito.

 */
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun JuegoMeditacionScreen(navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val TAG = "JuegoMeditacion"

    val neuroSkyManager = remember { NeuroSkyManager(context) }

    val connectionState by neuroSkyManager.connectionState.collectAsState()
    val eegData by neuroSkyManager.eegData.collectAsState()

    var puntos by remember { mutableStateOf(0) }
    var juegoActivo by remember { mutableStateOf(false) }
    var progresoMeditacion by remember { mutableStateOf(0f) }

    val objetivoMeditacion = 70
    val puntosObjetivo = 20

    // ⏳ Iniciar conexión y transmisión al iniciar Composable
    LaunchedEffect(Unit) {
        neuroSkyManager.conectar()
    }

    // 🎮 Lógica del juego basada en el valor de meditación
    LaunchedEffect(eegData.meditation, juegoActivo) {
        Log.d(TAG, "🧘 Meditación actual: ${eegData.meditation}")

        if (juegoActivo) {
            progresoMeditacion = eegData.meditation / 100f
            if (eegData.meditation >= objetivoMeditacion) {
                puntos++
            }
        }
    }

    // 🖼 UI
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Juego de Meditación") },
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

            Text(
                text = when (connectionState) {
                    ConnectionStatus.CONECTADO -> "🔌 Estado: Conectado"
                    ConnectionStatus.CONECTANDO -> "🔄 Estado: Conectando..."
                    ConnectionStatus.DESCONECTADO -> "❌ Estado: Desconectado"
                    ConnectionStatus.ERROR ->" ❌ Estado: Desconectado"
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

            Text("🧘 Meditación: ${eegData.meditation}", style = MaterialTheme.typography.titleMedium)
            Text("🎯 Objetivo: ≥ $objetivoMeditacion", color = Color(0xFF00C853))
            Text("⭐ Puntos: $puntos", style = MaterialTheme.typography.titleMedium)

            Spacer(Modifier.height(32.dp))

            if (!juegoActivo) {
                Button(onClick = {
                    juegoActivo = true
                    puntos = 0
                }) {
                    Text("Empezar juego")
                }
            }

            Spacer(Modifier.height(32.dp))

            LinearProgressIndicator(
                progress = progresoMeditacion,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(16.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color.LightGray
            )

            Spacer(Modifier.height(48.dp))

            if (juegoActivo && puntos >= puntosObjetivo) {
                AlertDialog(
                    onDismissRequest = { juegoActivo = false },
                    title = { Text("🎉 ¡Relajación alcanzada!") },
                    text = { Text("Has acumulado $puntos puntos de meditación profunda.") },
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
