package com.example.mindmoving.views.calibracion.guiada

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat.getStateDescription
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.mindmoving.neuroSkyService.CustomNeuroSky
import com.example.mindmoving.neuroSkyService.NeuroSkyListener
import com.example.mindmoving.retrofit.models.ValoresEEG
import com.neurosky.thinkgear.TGDevice
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "FaseEnfoqueScreen"

@Composable
fun FaseEnfoqueScreen(
    navController: NavHostController,
    onFaseCompletada: (ValoresEEG) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    // Estados de la UI
    var tiempoRestante by remember { mutableStateOf(15) }
    val attentionValues = remember { mutableStateListOf<Int>() }

    // Estados de conexión
    var conectado by remember { mutableStateOf(false) }
    var estadoConexion by remember { mutableStateOf("Desconectado") }
    var intentandoConectar by remember { mutableStateOf(false) }

    // Estados de datos EEG
    var atencionActual by remember { mutableStateOf(0) }
    var meditacionActual by remember { mutableStateOf(0) }
    var signalLevel by remember { mutableStateOf(200) } // Iniciar como sin señal
    var datosRecogidos by remember { mutableStateOf<ValoresEEG?>(null) }
    var faseIniciada by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    // Función para obtener descripción del estado
    fun getStateDescription(state: Int): String {
        return when (state) {
            TGDevice.STATE_IDLE -> "Inactivo"
            TGDevice.STATE_CONNECTING -> "Conectando"
            TGDevice.STATE_CONNECTED -> "Conectado"
            TGDevice.STATE_NOT_FOUND -> "No encontrado"
            TGDevice.STATE_NOT_PAIRED -> "No emparejado"
            TGDevice.STATE_DISCONNECTED -> "Desconectado"
            else -> "Estado desconocido ($state)"
        }
    }

    // Función para obtener descripción de calidad de señal
    fun getSignalQualityDescription(signal: Int): String {
        return when {
            signal == 0 -> "Excelente"
            signal in 1..50 -> "Buena"
            signal in 51..100 -> "Aceptable"
            signal in 101..150 -> "Débil"
            signal in 151..199 -> "Muy débil"
            signal >= 200 -> "Sin contacto/No colocada"
            else -> "Desconocida"
        }
    }
    // Crear instancia de NeuroSky solo una vez
    val neuroSky = remember {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter == null) {
            Log.e(TAG, "❌ BluetoothAdapter es null - dispositivo no soporta Bluetooth")
            null
        } else {
            Log.d(TAG, "✅ BluetoothAdapter creado correctamente")
            CustomNeuroSky(adapter, object : NeuroSkyListener {
                override fun onAttentionReceived(level: Int) {
                    Log.d(TAG, "🧠 Atención recibida: $level")
                    if (level in 1..100) {
                        atencionActual = level
                        if (faseIniciada) {
                            attentionValues.add(level)
                            Log.d(TAG, "📊 Valor de atención agregado: $level (Total: ${attentionValues.size})")
                        }
                    } else {
                        Log.w(TAG, "⚠️ Valor de atención inválido: $level")
                    }
                }

                override fun onBlinkDetected(strength: Int) {
                    Log.d(TAG, "👁️ Parpadeo detectado con fuerza: $strength")
                }

                override fun onMeditationReceived(level: Int) {
                    Log.d(TAG, "🧘 Meditación recibida: $level")
                    if (level in 1..100) {
                        meditacionActual = level
                    }
                }

                override fun onSignalPoor(signal: Int) {
                    signalLevel = signal
                    Log.d(TAG, "📡 Nivel de señal actualizado: $signal - ${getSignalQualityDescription(signal)}")
                }

                override fun onStateChanged(state: Int) {
                    val previousConnected = conectado
                    conectado = state == TGDevice.STATE_CONNECTED
                    estadoConexion = getStateDescription(state)

                    Log.i(TAG, "🔄 Estado de conexión cambiado: $state ($estadoConexion)")

                    if (conectado != previousConnected) {
                        Log.i(TAG, "📊 Cambio de estado de conexión: $previousConnected -> $conectado")

                        if (conectado) {
                            Log.i(TAG, "✅ Diadema conectada exitosamente")
                            scope.launch {
                                snackbarHostState.showSnackbar("✅ Diadema conectada")
                            }
                            intentandoConectar = false
                        } else {
                            Log.w(TAG, "❌ Diadema desconectada")
                            if (faseIniciada) {
                                Log.w(TAG, "⚠️ Pausando calibración por desconexión")
                                faseIniciada = false
                                scope.launch {
                                    snackbarHostState.showSnackbar("❌ Diadema desconectada. Calibración pausada.")
                                }
                            }
                        }
                    }
                }
            })
        }
    }

    // Función para iniciar conexión
    fun iniciarConexion() {
        if (neuroSky == null) {
            Log.e(TAG, "❌ NeuroSky es null - no se puede conectar")
            scope.launch {
                snackbarHostState.showSnackbar("❌ Error: Bluetooth no disponible")
            }
            return
        }

        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if (bluetoothAdapter?.isEnabled != true) {
            Log.e(TAG, "❌ Bluetooth no está habilitado")
            scope.launch {
                snackbarHostState.showSnackbar("❌ Bluetooth no está habilitado")
            }
            return
        }

        intentandoConectar = true
        Log.i(TAG, "🔍 Iniciando búsqueda de dispositivos MindWave...")

        try {
            val bondedDevices = bluetoothAdapter.bondedDevices
            Log.d(TAG, "📱 Dispositivos emparejados encontrados: ${bondedDevices?.size ?: 0}")

            bondedDevices?.forEach { device ->
                Log.d(TAG, "🔍 Dispositivo encontrado: ${device.name} (${device.address})")
            }

            val mindWaveDevice = bondedDevices?.firstOrNull { device ->
                val name = device.name
                Log.d(TAG, "🔍 Verificando dispositivo: $name")
                name?.contains("MindWave", ignoreCase = true) == true
            }

            if (mindWaveDevice != null) {
                Log.i(TAG, "✅ MindWave encontrada: ${mindWaveDevice.name} (${mindWaveDevice.address})")

                scope.launch {
                    try {
                        neuroSky.connectTo(mindWaveDevice)
                        Log.d(TAG, "🔌 Comando de conexión enviado")

                        // Esperar un poco para que se establezca la conexión
                        delay(3000)

                        if (conectado) {
                            Log.i(TAG, "📡 Iniciando transmisión de datos...")
                            neuroSky.start()
                            Log.d(TAG, "✅ Transmisión iniciada")
                        } else {
                            Log.w(TAG, "⚠️ No se pudo establecer conexión después de 3 segundos")
                            intentandoConectar = false
                            snackbarHostState.showSnackbar("⚠️ Tiempo de conexión agotado")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Error durante la conexión: ${e.message}", e)
                        intentandoConectar = false
                        snackbarHostState.showSnackbar("❌ Error de conexión: ${e.message}")
                    }
                }
            } else {
                Log.e(TAG, "❌ No se encontró ningún dispositivo MindWave emparejado")
                intentandoConectar = false
                scope.launch {
                    snackbarHostState.showSnackbar("❌ MindWave no encontrada. Asegúrate de que esté emparejada.")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error al buscar dispositivos: ${e.message}", e)
            intentandoConectar = false
            scope.launch {
                snackbarHostState.showSnackbar("❌ Error al buscar dispositivos")
            }
        }
    }

    // Función para iniciar recogida de datos
    fun iniciarRecogida() {
        if (!conectado) {
            Log.w(TAG, "⚠️ No se puede iniciar recogida: diadema no conectada")
            scope.launch {
                snackbarHostState.showSnackbar("⚠️ Conecta la diadema primero")
            }
            return
        }

        if (signalLevel >= 150) {
            Log.w(TAG, "⚠️ No se puede iniciar recogida: señal muy débil ($signalLevel)")
            scope.launch {
                snackbarHostState.showSnackbar("⚠️ Mejora la señal antes de continuar")
            }
            return
        }

        Log.i(TAG, "🚀 Iniciando recogida de datos de atención...")
        datosRecogidos = null
        tiempoRestante = 15
        attentionValues.clear()
        faseIniciada = true

        scope.launch {
            Log.d(TAG, "⏱️ Contador de tiempo iniciado (15 segundos)")

            while (tiempoRestante > 0 && conectado && faseIniciada) {
                delay(1000)
                tiempoRestante--
                Log.d(TAG, "⏰ Tiempo restante: $tiempoRestante | Valores recogidos: ${attentionValues.size}")
            }

            if (!conectado) {
                Log.w(TAG, "❌ Recogida cancelada por desconexión")
                faseIniciada = false
                return@launch
            }

            if (!faseIniciada) {
                Log.w(TAG, "❌ Recogida cancelada manualmente")
                return@launch
            }

            Log.i(TAG, "✅ Tiempo completado. Procesando datos...")
            Log.d(TAG, "📊 Valores recogidos: ${attentionValues.toList()}")

            if (attentionValues.isEmpty()) {
                Log.e(TAG, "❌ No se recogieron valores de atención")
                snackbarHostState.showSnackbar("❌ No se recogieron datos. Inténtalo de nuevo.")
                faseIniciada = false
                return@launch
            }

            val media = attentionValues.average().toInt()
            val max = attentionValues.maxOrNull() ?: 0
            val min = attentionValues.minOrNull() ?: 0
            val variabilidad = if (attentionValues.size > 1) {
                attentionValues.zipWithNext { a, b -> kotlin.math.abs(a - b) }.average().toFloat()
            } else {
                0f
            }

            val datos = ValoresEEG(media, max, min, variabilidad)
            Log.i(TAG, "📈 Datos procesados - Media: $media, Max: $max, Min: $min, Variabilidad: $variabilidad")

            datosRecogidos = datos
            onFaseCompletada(datos)
            faseIniciada = false

            snackbarHostState.showSnackbar("✅ Recogida completada exitosamente")
        }
    }

    // Manejo de permisos
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        Log.d(TAG, "📋 Resultado de permisos: $permissions")

        val bluetoothConnectGranted = permissions[Manifest.permission.BLUETOOTH_CONNECT] == true
        val bluetoothScanGranted = permissions[Manifest.permission.BLUETOOTH_SCAN] == true

        if (bluetoothConnectGranted && bluetoothScanGranted) {
            Log.i(TAG, "✅ Permisos de Bluetooth concedidos")
            iniciarConexion()
        } else {
            Log.e(TAG, "❌ Permisos de Bluetooth denegados")
            scope.launch {
                snackbarHostState.showSnackbar("❌ Se necesitan permisos de Bluetooth")
            }
        }
    }

    // Efecto del ciclo de vida
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            Log.d(TAG, "🔄 Lifecycle event: $event")

            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    Log.d(TAG, "🔁 ON_RESUME - Verificando estado de conexión")

                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                        Log.d(TAG, "📱 Android < S - No se necesitan permisos especiales")
                        if (!conectado && !intentandoConectar) {
                            iniciarConexion()
                        }
                    } else {
                        val hasBluetoothConnect = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.BLUETOOTH_CONNECT
                        ) == PackageManager.PERMISSION_GRANTED

                        val hasBluetoothScan = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.BLUETOOTH_SCAN
                        ) == PackageManager.PERMISSION_GRANTED

                        Log.d(TAG, "📋 Permisos - CONNECT: $hasBluetoothConnect, SCAN: $hasBluetoothScan")

                        if (hasBluetoothConnect && hasBluetoothScan) {
                            if (!conectado && !intentandoConectar) {
                                iniciarConexion()
                            }
                        } else {
                            Log.w(TAG, "⚠️ Solicitando permisos de Bluetooth...")
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.BLUETOOTH_CONNECT,
                                    Manifest.permission.BLUETOOTH_SCAN
                                )
                            )
                        }
                    }
                }
                Lifecycle.Event.ON_PAUSE -> {
                    Log.d(TAG, "⏸️ ON_PAUSE")
                }
                Lifecycle.Event.ON_DESTROY -> {
                    Log.d(TAG, "💀 ON_DESTROY")
                }
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            Log.d(TAG, "🧹 Limpiando recursos...")
            lifecycleOwner.lifecycle.removeObserver(observer)
            neuroSky?.disconnect()
        }
    }

    // UI
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF101020))
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                // Estado de conexión y señal
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Estado: $estadoConexion",
                            color = if (conectado) Color.Green else Color.Red,
                            style = MaterialTheme.typography.bodyLarge
                        )

                        if (conectado) {
                            val signalColor = when {
                                signalLevel == 0 -> Color.Green
                                signalLevel in 1..100 -> Color.Yellow
                                signalLevel > 100 -> Color.Red
                                else -> Color.Gray
                            }

                            Text(
                                text = "Señal: ${getSignalQualityDescription(signalLevel)} ($signalLevel)",
                                color = signalColor,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Text(
                                text = "Atención: $atencionActual | Meditación: $meditacionActual",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                Text(
                    "FASE DE ENFOQUE",
                    color = Color.Cyan,
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(Modifier.height(16.dp))

                when {
                    intentandoConectar -> {
                        Text("🔌 Conectando con la diadema...", color = Color.Gray)
                        Spacer(Modifier.height(16.dp))
                        CircularProgressIndicator(color = Color.Cyan)
                    }

                    !conectado -> {
                        Text("❌ Diadema no conectada", color = Color.Red)
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { iniciarConexion() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan)
                        ) {
                            Text("🔄 Reintentar conexión", color = Color.Black)
                        }
                    }

                    !faseIniciada && datosRecogidos == null -> {
                        Text("Pulsa el orbe para empezar", color = Color.LightGray)
                        Spacer(Modifier.height(32.dp))

                        val orbeTamaño by animateDpAsState(
                            targetValue = (80 + atencionActual).coerceIn(80, 160).dp,
                            label = "Animación orbe"
                        )

                        Box(
                            modifier = Modifier
                                .size(orbeTamaño)
                                .background(Color.Cyan, shape = MaterialTheme.shapes.extraLarge)
                                .clickable(enabled = signalLevel < 150) {
                                    iniciarRecogida()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("▶", color = Color.Black, style = MaterialTheme.typography.headlineMedium)
                        }

                        if (signalLevel >= 150) {
                            Spacer(Modifier.height(16.dp))
                            Text("⚠️ Mejora la colocación de la diadema", color = Color.Yellow)
                        }
                    }

                    faseIniciada -> {
                        Spacer(Modifier.height(16.dp))
                        CircularProgressIndicator(
                            progress = tiempoRestante / 15f,
                            color = Color.Cyan,
                            modifier = Modifier.size(100.dp),
                            strokeWidth = 8.dp
                        )
                        Spacer(Modifier.height(16.dp))
                        Text("Tiempo restante: $tiempoRestante s", color = Color.White)
                        Text("Datos recogidos: ${attentionValues.size}", color = Color.Gray)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Atención actual: $atencionActual",
                            color = if (atencionActual > 60) Color.Green else Color.Red,
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = {
                                faseIniciada = false
                                Log.i(TAG, "🛑 Recogida detenida manualmente")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Text("⏹️ Detener", color = Color.White)
                        }
                    }
                }

                datosRecogidos?.let { datos ->
                    Spacer(Modifier.height(24.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("📊 Datos recogidos:", color = Color.White, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))
                            Text("Media: ${datos.media}", color = Color.Cyan)
                            Text("Máximo: ${datos.maximo}", color = Color.Cyan)
                            Text("Mínimo: ${datos.minimo}", color = Color.Cyan)
                            Text("Variabilidad: ${"%.2f".format(datos.variabilidad)}", color = Color.Cyan)
                            Text("Muestras: ${attentionValues.size}", color = Color.Gray)
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(
                            onClick = {
                                Log.i(TAG, "🔄 Reiniciando recogida de datos")
                                iniciarRecogida()
                            }
                        ) {
                            Text("🔄 Repetir")
                        }

                        Button(
                            onClick = {
                                Log.i(TAG, "➡️ Continuando a siguiente fase")
                                navController.navigate("fase_relajacion")
                            }
                        ) {
                            Text("➡️ Continuar")
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewFaseEnfoqueScreen() {
    FaseEnfoqueScreen(navController = rememberNavController())
}