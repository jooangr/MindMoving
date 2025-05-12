package com.example.mindmoving.views.menu.calibracion


import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.os.Handler
import android.os.Message
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.LinearProgressIndicator
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


@Composable
fun CalibracionRelajacionScreen(navController: NavHostController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var meditationLevel by remember { mutableStateOf(0) }
    var isConnected by remember { mutableStateOf(false) }
    var isRunning by remember { mutableStateOf(false) }
    var secondsLeft by remember { mutableStateOf(30) }
    val meditationData = remember { mutableStateListOf<Int>() }

    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    var neuroSky: CustomNeuroSky? by remember { mutableStateOf(null) }

    val handler = remember {
        object : Handler(context.mainLooper) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    TGDevice.MSG_MEDITATION -> {
                        val meditation = msg.arg1
                        if (meditation in 1..100) {
                            meditationLevel = meditation
                            if (isRunning) meditationData.add(meditation)
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
        } else {
            Text("Relajación: $meditationLevel", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = meditationLevel / 100f,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(12.dp),
                color = Color(0xFF81C784)
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (!isRunning) {
                Button(onClick = {
                    meditationData.clear()
                    secondsLeft = 30
                    isRunning = true
                }) {
                    Text("Empezar relajación")
                }

                if (meditationData.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        // Guardar o procesar datos
                        navController.navigate("menu")
                    }) {
                        Text("Finalizar calibración")
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { meditationData.clear() }) {
                        Text("Reiniciar")
                    }
                }
            } else {
                Text("⏳ Registrando... $secondsLeft segundos restantes")
            }
        }
    }
}
