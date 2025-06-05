package com.example.mindmoving.views.menuPrincipal.attention

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Message
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
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
import androidx.navigation.NavHostController
import com.example.mindmoving.neuroSkyService.CustomNeuroSky
import com.neurosky.thinkgear.TGDevice


@Composable
fun AtencionPantalla(navController: NavHostController){
    // Accedemos al contexto actual de la app (necesario para permisos, logs, etc.)
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Estado para guardar el nivel de atención actual recibido
    var attentionLevel by remember { mutableStateOf(0) }

    // Adaptador Bluetooth del sistema
    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    // Objeto que maneja la conexión con la diadema
    var neuroSky: CustomNeuroSky? by remember { mutableStateOf(null) }

    // Handler para recibir mensajes desde la diadema (como los niveles de atención)
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
                            neuroSky?.start() // Solo cuando se conecta, empieza la lectura
                        }
                    }
                }
            }
        }
    }

    // Función para conectar con el dispositivo emparejado
    fun conectarDiadema() {
        try {
            val deviceName = "MindWave Mobile"
            val device = bluetoothAdapter?.bondedDevices?.find { it.name == deviceName }

            /**
            if (device != null) {
                Log.d("MindWave", "✅ Dispositivo encontrado: ${device.name}")
                neuroSky = CustomNeuroSky(bluetoothAdapter, listener = )
                neuroSky?.connectTo(device)
            } else {
                Log.e("MindWave", "❌ No se encontró la diadema $deviceName")
            }*/
        } catch (e: Exception) {
            Log.e("MindWave", "⚠️ Error al conectar: ${e.message}")
        }
    }

    // Efecto que se ejecuta cuando la pantalla entra en foco (ON_RESUME)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // Verifica permisos o los solicita si no están otorgados
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

        // Se agrega y elimina el observer del ciclo de vida
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            neuroSky?.disconnect() // Desconectar para liberar recursos
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // UI: Muestra un círculo que cambia de color según el nivel de atención
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.align(Alignment.Start)
        ) {
            Text("← Volver")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Canvas(modifier = Modifier.size(100.dp)) {
            drawCircle(color = getColorForAttention(attentionLevel))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Texto que informa si hay atención detectada o no
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

// Devuelve un color visual según el nivel de atención
fun getColorForAttention(level: Int): Color {
    return when {
        level < 30 -> Color.Red
        level <= 70 -> Color.Yellow
        else -> Color.Green
    }
}
