package com.example.mindmoving.views

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Message
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.mindmoving.neuroSkyService.CustomNeuroSky
import com.neurosky.thinkgear.TGDevice

@Composable
fun AtencionPantalla() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var attentionLevel by remember { mutableStateOf(0) }

    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    var neuroSky: CustomNeuroSky? by remember { mutableStateOf(null) }

    val handler = remember {
        object : Handler(context.mainLooper) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    TGDevice.MSG_ATTENTION -> {
                        val attention = msg.arg1
                        if (attention in 1..100) {
                            Log.d("MindWave", "🧠 Atención: $attention")
                            attentionLevel = attention
                        }
                    }
                    TGDevice.MSG_STATE_CHANGE -> {
                        if (msg.arg1 == TGDevice.STATE_CONNECTED) {
                            Log.d("MindWave", "✅ Diadema conectada. Iniciando lectura de datos...")
                            neuroSky?.start()
                        }
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


    // Pedimos permisos y conectamos cuando la pantalla se muestra
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                    (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED)
                ) {
                    conectarDiadema()
                } else {
                    ActivityCompat.requestPermissions(
                        context as android.app.Activity,
                        arrayOf(
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_SCAN
                        ),
                        1
                    )
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            neuroSky?.disconnect()
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Canvas(modifier = Modifier.size(100.dp)) {
            drawCircle(color = getColorForAttention(attentionLevel))
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (attentionLevel == 0) {
            Text(
                text = "Esperando señal de la diadema...",
                color = Color.Gray,
                style = MaterialTheme.typography.headlineSmall
            )
        } else {
            Text(
                text = "Atención: $attentionLevel",
                style = MaterialTheme.typography.headlineSmall
            )
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
