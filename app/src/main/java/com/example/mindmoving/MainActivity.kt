package com.example.mindmoving

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.example.mindmoving.ui.theme.MindMovingTheme
import com.github.pwittchen.neurosky.library.NeuroSky
import com.github.pwittchen.neurosky.library.listener.DeviceMessageListener

class MainActivity : ComponentActivity(), DeviceMessageListener {

    private var neuroSky: CustomNeuroSky? = null

    private var attentionLevel by mutableStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // PERMISOS ANDROID 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                ),
                1
            )
        }


        // MOSTRAR TODOS LOS DISPOSITIVOS EMPAREJADOS
        val deviceName = "MindWave Mobile"
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val device = bluetoothAdapter?.bondedDevices?.find { it.name == "MindWave Mobile" }

        if (device != null) {
            val handler = object : Handler(mainLooper) {
                override fun handleMessage(msg: Message) {
                    onMessageReceived(msg) // tu función ya definida
                }
            }

            val customNeuroSky = CustomNeuroSky(bluetoothAdapter, handler)
            customNeuroSky.connectTo(device)

            neuroSky = customNeuroSky // para usar luego en disconnect()
        } else {
            Log.e("MindWave", "No se encontró el dispositivo MindWave Mobile")
        }





        // UI
        setContent {
            MindMovingTheme {
                MainScreen(attentionLevel)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        (neuroSky as? CustomNeuroSky)?.disconnect()

    }

    override fun onMessageReceived(message: Message) {
        if (message.what == 4) {
            val value = message.arg1
            Log.d("MindWave", "Nivel de atención recibido: $value")
            if (value in 0..100) {
                attentionLevel = value
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1 &&
            grantResults.isNotEmpty() &&
            grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            recreate()
        }
    }
}

@Composable
fun MainScreen(attentionLevel: Int) {
    val pointColor = getColorForAttention(attentionLevel)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Canvas(modifier = Modifier.size(100.dp)) {
            drawCircle(color = pointColor)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Atención: $attentionLevel",
            style = MaterialTheme.typography.headlineSmall
        )
    }
}

fun getColorForAttention(level: Int): Color {
    return when {
        level < 30 -> Color.Red
        level in 30..70 -> Color.Yellow
        else -> Color.Green
    }
}
