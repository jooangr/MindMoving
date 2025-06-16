package com.example.mindmoving.views.calibracion.viewsMenuCalibracion

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import com.example.mindmoving.neuroSkyService.CustomNeuroSky
import com.example.mindmoving.neuroSkyService.NeuroSkyListener
import com.neurosky.thinkgear.TGDevice
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random
import androidx.compose.ui.platform.LocalLifecycleOwner

import com.example.mindmoving.neuroSkyService.NeuroSkyManager
import com.example.mindmoving.views.controlCoche.ConnectionStatus

/**
 * Pantalla de juego para entrenar el reconocimiento de parpadeos con la diadema NeuroSky.
 */

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JuegoParpadeoScreen(navController: NavHostController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val TAG = "JuegoParpadeo"

    val neuroSkyManager = remember { NeuroSkyManager(context) }
    val connectionState by neuroSkyManager.connectionState.collectAsState()
    val eegData by neuroSkyManager.eegData.collectAsState()

    var contadorParpadeos by remember { mutableStateOf(0) }
    var juegoActivo by remember { mutableStateOf(false) }
    var juegoIniciado by remember { mutableStateOf(false) }
    var tiempoRestante by remember { mutableStateOf(10) }
    var mostrarDialogo by remember { mutableStateOf(false) }
    var posicionPunto by remember { mutableStateOf(Offset(300f, 500f)) }
    val random = remember { Random(System.currentTimeMillis()) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val connectGranted = permissions[Manifest.permission.BLUETOOTH_CONNECT] == true
        val scanGranted = permissions[Manifest.permission.BLUETOOTH_SCAN] == true
        if (connectGranted && scanGranted) {
            neuroSkyManager.conectar()
        } else {
            Toast.makeText(context, "Permisos Bluetooth necesarios", Toast.LENGTH_SHORT).show()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val hasConnect = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == android.content.pm.PackageManager.PERMISSION_GRANTED
                    val hasScan = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == android.content.pm.PackageManager.PERMISSION_GRANTED
                    if (hasConnect && hasScan) {
                        neuroSkyManager.conectar()
                    } else {
                        permissionLauncher.launch(arrayOf(
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_SCAN
                        ))
                    }
                } else {
                    neuroSkyManager.conectar()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            neuroSkyManager.desconectar()
        }
    }

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

    LaunchedEffect(eegData.blinkStrength) {
        if (juegoActivo && eegData.blinkStrength > 40) {
            Log.d(TAG, "👁️ Blink detectado: ${eegData.blinkStrength}")
            contadorParpadeos++
            posicionPunto = Offset(
                random.nextFloat() * 800f,
                random.nextFloat() * 1500f
            )
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
            val colorPrimario = MaterialTheme.colorScheme.primary

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
                Text("Estado: ${connectionState.name}", color = if (connectionState == ConnectionStatus.CONECTADO) Color.Green else Color.Red)
                Text("Tiempo restante: $tiempoRestante s")
                Text("Parpadeos: $contadorParpadeos")
            }

            if (!juegoIniciado) {
                Button(
                    onClick = {
                        if (connectionState != ConnectionStatus.CONECTADO) {
                            Toast.makeText(context, "⚠️ Diadema no conectada", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
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
}
