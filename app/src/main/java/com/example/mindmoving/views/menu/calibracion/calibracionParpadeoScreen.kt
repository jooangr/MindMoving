package com.example.mindmoving.views.menu.calibracion

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.os.*
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import com.example.mindmoving.neuroSkyService.CustomNeuroSky
import com.neurosky.thinkgear.TGDevice
import kotlinx.coroutines.delay

@Composable
fun CalibracionParpadeoScreen(navController: NavHostController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var blinkCount by remember { mutableStateOf(0) }
    var isConnected by remember { mutableStateOf(false) }
    var isRunning by remember { mutableStateOf(false) }
    var secondsLeft by remember { mutableStateOf(30) }

    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    var neuroSky: CustomNeuroSky? by remember { mutableStateOf(null) }

    val handler = remember {
        object : Handler(context.mainLooper) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    TGDevice.MSG_BLINK -> {
                        val blinkStrength = msg.arg1
                        if (isRunning) {
                            blinkCount++
                            Log.d("MindWave", "👁️ Parpadeo detectado con fuerza $blinkStrength")
                        }
                    }
                    TGDevice.MSG_STATE_CHANGE -> {
                        isConnected = (msg.arg1 == TGDevice.STATE_CONNECTED)
                        if (isConnected) neuroSky?.start()
                    }
                }
            }
        }
    }

    fun conectarDiadema() {
        try {
            val deviceName = "MindWave Mobile"
            val device = bluetoothAdapter?.bondedDevices?.find { it.name == deviceName }

            if (device != null) {
                Log.d("MindWave", "✅ Dispositivo encontrado: ${device.name}")
                neuroSky = CustomNeuroSky(bluetoothAdapter, handler)
                neuroSky?.connectTo(device)
            } else {
                Log.e("MindWave", "❌ No se encontró la diadema $deviceName")
            }
        } catch (e: Exception) {
            Log.e("MindWave", "⚠️ Error al conectar: ${e.message}")
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) conectarDiadema()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            neuroSky?.disconnect()
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Temporizador
    LaunchedEffect(isRunning) {
        if (isRunning) {
            while (secondsLeft > 0) {
                delay(1000)
                secondsLeft--
            }
            isRunning = false
        }
    }

    // UI
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!isConnected) {
            Text("⏳ Esperando conexión con la diadema...", color = Color.Gray)

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = {
                // Guardar o procesar datos
                navController.navigate("calibracion_menu")
            }) {
                Text("Volver al Menú de Calibración")
            }

        } else {
            Text("Parpadeos detectados: $blinkCount", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            if (!isRunning) {
                Button(onClick = {
                    blinkCount = 0
                    secondsLeft = 30
                    isRunning = true
                }) {
                    Text("Iniciar conteo de parpadeos")
                }

                if (blinkCount > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        navController.navigate("menu") // ir al menú o donde continues
                    }) {
                        Text("Finalizar calibración")
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        blinkCount = 0
                    }) {
                        Text("Reiniciar")
                    }
                }
            } else {
                Text("⏳ Registrando... $secondsLeft segundos restantes")

                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    navController.navigate("calibracion_parpadeo")
                }) {
                    Text("Siguiente Juego")
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            navController.navigate("menu")
        }) {
            Text("Omitir (bton para pasar a siguiente juego sin diadema)")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigate("calibracion_menu") }) {
            Text("Volver al menú de calibración")
        }
    }
}
