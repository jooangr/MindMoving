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
import com.neurosky.thinkgear.TGDevice
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch



@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun JuegoMeditacionScreen(navController: NavHostController) {
    val colorPrimario = MaterialTheme.colorScheme.primary
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var conectado by remember { mutableStateOf(false) }
    var meditacionActual by remember { mutableStateOf(0) }
    var puntos by remember { mutableStateOf(0) }
    var juegoActivo by remember { mutableStateOf(false) }
    var progresoMeditacion by remember { mutableStateOf(0f) }

    val objetivoMeditacion = 70
    val puntosObjetivo = 20

    val neuroSky = remember {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter != null) {
            CustomNeuroSky(adapter, object : NeuroSkyListener {
                override fun onAttentionReceived(level: Int) {}
                override fun onBlinkDetected(strength: Int) {}
                override fun onSignalPoor(signal: Int) {}
                override fun onMeditationReceived(level: Int) {
                    meditacionActual = level
                    if (juegoActivo) {
                        progresoMeditacion = level / 100f
                        if (level >= objetivoMeditacion) puntos++
                    }
                }

                override fun onStateChanged(state: Int) {
                    conectado = state == TGDevice.STATE_CONNECTED
                }
            })
        } else null
    }

    LaunchedEffect(Unit) {
        val device = BluetoothAdapter.getDefaultAdapter()
            ?.bondedDevices?.firstOrNull { it.name.contains("MindWave", true) }

        if (device != null && neuroSky != null) {
            neuroSky.connectTo(device)
            delay(3000)
            if (conectado) neuroSky.start()
        }
    }

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

            Text("Meditación actual: $meditacionActual", style = MaterialTheme.typography.titleMedium)
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

            // 🧘 Visualización zen: barra de progreso
            LinearProgressIndicator(
                progress = progresoMeditacion,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(16.dp),
                color = colorPrimario,
                trackColor = Color.LightGray
            )

            Spacer(Modifier.height(48.dp))

            if (juegoActivo && puntos >= puntosObjetivo) {
                AlertDialog(
                    onDismissRequest = { juegoActivo = false },
                    title = { Text("¡Relajación alcanzada!") },
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
