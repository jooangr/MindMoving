package com.example.mindmoving.views.calibracion.viewsMenuCalibracion

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.graphics.Rect
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mindmoving.neuroSkyService.CustomNeuroSky
import com.example.mindmoving.neuroSkyService.NeuroSkyListener
import com.neurosky.thinkgear.TGDevice
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Pantalla de juego para entrenar el reconocimiento de parpadeos con la diadema NeuroSky.
 */
@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JuegoParpadeoScreen(navController: NavHostController) {
    val colorPrimario = MaterialTheme.colorScheme.primary

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var conectado by remember { mutableStateOf(false) }
    var contadorParpadeos by remember { mutableStateOf(0) }
    var posicionPunto by remember { mutableStateOf(Offset(300f, 500f)) }

    var tiempoRestante by remember { mutableStateOf(10) }
    var juegoActivo by remember { mutableStateOf(false) }
    var mostrarDialogo by remember { mutableStateOf(false) }
    var juegoIniciado by remember { mutableStateOf(false) }

    val random = remember { Random(System.currentTimeMillis()) }

    // NeuroSky setup
    val neuroSky = remember {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter != null) {
            CustomNeuroSky(adapter, object : NeuroSkyListener {
                override fun onBlinkDetected(strength: Int) {
                    if (juegoActivo && strength > 40) {
                        contadorParpadeos++
                        posicionPunto = Offset(
                            random.nextFloat() * 800f,
                            random.nextFloat() * 1500f
                        )
                    }
                }

                override fun onAttentionReceived(level: Int) {}
                override fun onMeditationReceived(level: Int) {}
                override fun onSignalPoor(signal: Int) {}
                override fun onStateChanged(state: Int) {
                    conectado = state == TGDevice.STATE_CONNECTED
                }
            })
        } else null
    }

    // Conexión al iniciar
    LaunchedEffect(Unit) {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        val device = adapter?.bondedDevices?.firstOrNull { it.name.contains("MindWave", true) }
        if (device != null && neuroSky != null) {
            neuroSky.connectTo(device)
            delay(3000)
            if (conectado) neuroSky.start()
        }
    }

    // Temporizador
    LaunchedEffect(juegoActivo) {
        if (juegoActivo) {
            tiempoRestante = 10
            while (tiempoRestante > 0) {
                delay(1000)
                tiempoRestante--
            }
            juegoActivo = false
            mostrarDialogo = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Juego de Parpadeo") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (juegoActivo) {
                    drawCircle(
                        color = colorPrimario,
                        radius = 50f,
                        center = posicionPunto
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Tiempo restante: $tiempoRestante s", style = MaterialTheme.typography.titleMedium)
                Text("Parpadeos: $contadorParpadeos", style = MaterialTheme.typography.titleMedium)
            }

            if (!juegoIniciado) {
                Button(
                    onClick = {
                        contadorParpadeos = 0
                        posicionPunto = Offset(300f, 500f)
                        juegoActivo = true
                        juegoIniciado = true
                    },
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Text("🎮 Empezar Juego")
                }
            }
        }

        if (mostrarDialogo) {
            AlertDialog(
                onDismissRequest = { },
                title = { Text("⏱️ Juego terminado") },
                text = { Text("Detectaste $contadorParpadeos parpadeos en 10 segundos.") },
                confirmButton = {
                    TextButton(onClick = {
                        contadorParpadeos = 0
                        juegoActivo = false
                        juegoIniciado = false
                        mostrarDialogo = false
                        posicionPunto = Offset(300f, 500f)
                    }) {
                        Text("🔁 Reiniciar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Text("Salir")
                    }
                }
            )
        }
    }
}
