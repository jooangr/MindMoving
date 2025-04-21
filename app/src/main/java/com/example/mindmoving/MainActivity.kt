package com.example.mindmoving

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import android.widget.Toast
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
import androidx.core.content.ContextCompat
import com.example.mindmoving.ui.theme.MindMovingTheme

class MainActivity : ComponentActivity() {

    private var neuroSky: CustomNeuroSky? = null
    private var attentionLevel by mutableStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Pedir permisos en Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                ),
                1
            )
        } else {
            conectarDiadema()
        }

        setContent {
            MindMovingTheme {
                MainScreen(attentionLevel)
            }
        }
    }

    private fun conectarDiadema() {
        val deviceName = "MindWave Mobile"
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        val device = bluetoothAdapter?.bondedDevices?.find { it.name == deviceName }

        if (device != null) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            Log.d("MindWave", "✅ Dispositivo emparejado encontrado: ${device.name}")

            val handler = object : Handler(mainLooper) {
                override fun handleMessage(msg: Message) {
                    when (msg.what) {
                        4 -> {
                            val level = msg.arg1
                            if (level in 0..100) {
                                attentionLevel = level
                                Log.d("MindWave", "Nivel de atención: $level")
                            }
                        }
                        else -> Log.d("MindWave", "Mensaje recibido: ${msg.what}")
                    }
                }
            }

            neuroSky = CustomNeuroSky(bluetoothAdapter, handler)
            neuroSky?.connectTo(device)

        } else {
            Log.e("MindWave", "❌ No se encontró el dispositivo $deviceName")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        neuroSky?.disconnect()
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1 && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            conectarDiadema()
        } else {
            Toast.makeText(this, "Se necesitan permisos de Bluetooth para conectar.", Toast.LENGTH_LONG).show()
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
        level <= 70 -> Color.Yellow
        else -> Color.Green
    }
}
