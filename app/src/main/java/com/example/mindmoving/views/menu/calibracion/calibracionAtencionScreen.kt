package com.example.mindmoving.views.menu.calibracion

import android.bluetooth.BluetoothAdapter
import android.os.Handler
import android.os.Message
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size

@Composable
fun CalibracionAtencionScreen(navController: NavHostController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var attentionLevel by remember { mutableStateOf(0) }
    var isConnected by remember { mutableStateOf(false) }
    var isRunning by remember { mutableStateOf(false) }
    var secondsLeft by remember { mutableStateOf(30) }
    val attentionData = remember { mutableStateListOf<Int>() }

    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    var neuroSky: CustomNeuroSky? by remember { mutableStateOf(null) }

    val handler = remember {
        object : Handler(context.mainLooper) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    TGDevice.MSG_ATTENTION -> {
                        val attention = msg.arg1
                        if (attention in 1..100) {
                            attentionLevel = attention
                            if (isRunning) attentionData.add(attention)
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

    LaunchedEffect(isRunning) {
        if (isRunning) {
            while (secondsLeft > 0) {
                delay(1000)
                secondsLeft--
            }
            isRunning = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!isConnected) {
            Text("⏳ Esperando conexión con la diadema...", color = Color.Gray)
        } else {
            // 🔴 Punto que cambia de color según nivel de atención
            Canvas(modifier = Modifier.size(100.dp)) {
                drawCircle(color = getColorForAttention(attentionLevel))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Nivel de Atención: $attentionLevel", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            if (!isRunning) {
                Button(onClick = {
                    attentionData.clear()
                    secondsLeft = 30
                    isRunning = true
                }) {
                    Text("Empezar calibración")
                }

                if (attentionData.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        navController.navigate("calibracion_relajacion") // segundo juego
                    }) {
                        Text("Continuar")
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { attentionData.clear() }) {
                        Text("Reiniciar")
                    }
                }

                // 🛑 Botón para saltar calibración
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    navController.navigate("calibracion_relajacion")
                }) {
                    Text("Siguiente Juego")
                }

            } else {
                Text("⏳ Registrando... $secondsLeft segundos restantes")


            }

        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            navController.navigate("calibracion_relajacion")
        }) {
            Text("Omitir (bton para pasar a siguiente juego sin diadema)")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            navController.navigate("calibracion_menu")
        }) {
            Text("Volver Al Menu de Calibración")
        }
    }

}

fun getColorForAttention(level: Int): Color {
    return when {
        level < 30 -> Color.Red
        level <= 70 -> Color.Yellow
        else -> Color.Green
    }
}


