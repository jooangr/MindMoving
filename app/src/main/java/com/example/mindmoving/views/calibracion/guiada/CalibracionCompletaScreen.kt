package com.example.mindmoving.views.calibracion.guiada

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.mindmoving.neuroSkyService.CustomNeuroSky
import com.example.mindmoving.neuroSkyService.NeuroSkyListener
import com.example.mindmoving.retrofit.ApiClient
import com.example.mindmoving.retrofit.models.perfilCalibracion.PerfilCalibracion
import com.example.mindmoving.retrofit.models.perfilCalibracion.PerfilCalibracionRequest
import com.example.mindmoving.retrofit.models.sesionesEGG.SesionEEGRequest
import com.example.mindmoving.retrofit.models.user.BlinkingData
import com.example.mindmoving.retrofit.models.user.Usuario
import com.example.mindmoving.retrofit.models.user.ValoresEEG
import com.example.mindmoving.utils.SessionManager
import com.google.gson.Gson
import com.neurosky.thinkgear.TGDevice
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "FaseCalibracionCompleta"

@Composable
fun CalibracionCompletaScreen(navController: NavHostController) {

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val perfilJson = prefs.getString("perfil_completo", null)

        if (!perfilJson.isNullOrBlank()) {
            try {
                val usuario = Gson().fromJson(perfilJson, Usuario::class.java)
                SessionManager.usuarioActual = usuario
                Log.d("CALIBRACION", "✅ Usuario restaurado desde SharedPreferences: ${usuario.id}")
            } catch (e: Exception) {
                Log.e("CALIBRACION", "❌ Error al deserializar usuario: ${e.message}")
            }
        } else {
            Log.e("CALIBRACION", "❌ No se encontró perfil_completo en SharedPreferences")
        }
    }


    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var conectado by remember { mutableStateOf(false) }
    var intentandoConectar by remember { mutableStateOf(false) }
    var estadoConexion by remember { mutableStateOf("Desconectado") }
    var signalLevel by remember { mutableStateOf(200) }
    var estadoDiadema by remember { mutableStateOf("Sin conexión") }

    val atencion = remember { mutableStateListOf<Int>() }
    val meditacion = remember { mutableStateListOf<Int>() }
    val parpadeos = remember { mutableStateListOf<Int>() }

    var tiempoRestante by remember { mutableStateOf(15) }
    var faseActual by remember { mutableStateOf("ENFOQUE") }
    var recogiendoDatos by remember { mutableStateOf(false) }
    var datosListos by remember { mutableStateOf(false) }
    var mensajeInstruccion by remember { mutableStateOf("Prepárate para enfocarte") }
    var tiempoInicioSesion by remember { mutableStateOf<Long?>(null) }
    var sesionEEGGenerada by remember { mutableStateOf<SesionEEGRequest?>(null) } //TODO joan: pa que lo veas

    var atencionActual by remember { mutableStateOf(0) }
    var meditacionActual by remember { mutableStateOf(0) }

    var resultadoAtencion by remember { mutableStateOf<ValoresEEG?>(null) }
    var resultadoMeditacion by remember { mutableStateOf<ValoresEEG?>(null) }
    var resultadoParpadeo by remember { mutableStateOf<BlinkingData?>(null) }

    var sesionEnviada by remember { mutableStateOf(false) }

    //PerfilCalibracion
    var mostrarPerfilAsignado by remember { mutableStateOf(false) }
    var perfilAsignadoNombre by remember { mutableStateOf("") }



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

    // Función para obtener color de señal
    fun getSignalColor(signal: Int): Color {
        return when {
            signal == 0 -> Color.Green
            signal in 1..50 -> Color(0xFFFFA500)
            signal in 51..100 -> Color(0xFFFFA500) // Naranja
            signal in 101..150 -> Color.Red
            signal in 151..199 -> Color.Red
            signal >= 200 -> Color.Red
            else -> Color.Gray
        }
    }

    val neuroSky = remember {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter != null) {
            CustomNeuroSky(adapter, object : NeuroSkyListener {
                override fun onAttentionReceived(level: Int) {
                    atencionActual = level
                    if (recogiendoDatos && faseActual == "ENFOQUE") atencion.add(level)
                    Log.d(TAG, "🧠 Atención: $level")
                }
                override fun onMeditationReceived(level: Int) {
                    meditacionActual = level
                    if (recogiendoDatos && faseActual == "RELAJACION") meditacion.add(level)
                    Log.d(TAG, "🧘 Meditación: $level")
                }
                override fun onBlinkDetected(strength: Int) {
                    if (recogiendoDatos && faseActual == "RELAJACION") parpadeos.add(strength)
                    Log.d(TAG, "👁️ Parpadeo detectado: $strength")
                }
                override fun onSignalPoor(signal: Int) {
                    signalLevel = signal
                    estadoDiadema = getSignalQualityDescription(signal)
                    Log.d(TAG, "📡 Nivel señal actualizado: $signal ($estadoDiadema)")
                }

                override fun onStateChanged(state: Int) {
                    conectado = state == TGDevice.STATE_CONNECTED
                    estadoConexion = when (state) {
                        TGDevice.STATE_CONNECTED -> "Conectado"
                        TGDevice.STATE_CONNECTING -> "Conectando"
                        TGDevice.STATE_DISCONNECTED -> "Desconectado"
                        else -> "Estado: $state"
                    }
                    Log.i(TAG, "🔄 Estado conexión: $estadoConexion")
                }
            })
        } else null
    }

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

    fun iniciarConexion() {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter?.isEnabled == true) {
            val device = adapter.bondedDevices.firstOrNull { it.name.contains("MindWave", true) }
            device?.let {
                intentandoConectar = true
                neuroSky?.connectTo(it)
                scope.launch {
                    delay(3000)
                    if (conectado) neuroSky?.start()
                    intentandoConectar = false
                    Log.i(TAG, "✅ Diadema conectada")
                }
            } ?: run {
                scope.launch { snackbarHostState.showSnackbar("❌ MindWave no encontrada") }
            }
        } else {
            scope.launch { snackbarHostState.showSnackbar("❌ Activa el Bluetooth") }
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

    fun iniciarFase(nombreFase: String) {
        if (signalLevel >= 150) {
            scope.launch { snackbarHostState.showSnackbar("⚠️ Señal débil. Mejora la colocación de la diadema.") }
            return
        }

        faseActual = nombreFase
        recogiendoDatos = true
        tiempoRestante = 15
        mensajeInstruccion = if (nombreFase == "ENFOQUE") "Concéntrate" else "Relájate y parpadea cuando se indique"
        if (nombreFase == "ENFOQUE") tiempoInicioSesion = System.currentTimeMillis()
        Log.i(TAG, "🚀 Iniciando fase: $nombreFase")
        scope.launch {
            while (tiempoRestante > 0 && recogiendoDatos && conectado) {
                delay(1000)
                tiempoRestante--
                if (faseActual == "RELAJACION" && tiempoRestante % 5 == 0) mensajeInstruccion = "¡Parpadea fuerte ahora!"
                else if (faseActual == "RELAJACION") mensajeInstruccion = "Relájate..."
                Log.d(TAG, "⏳ Tiempo restante: $tiempoRestante")
            }
            recogiendoDatos = false
            if (faseActual == "ENFOQUE") iniciarFase("RELAJACION")
            else datosListos = true
        }
    }

    //Hace los calculos necesarios para guardar los datos
    fun calcularEEG(valores: List<Int>): ValoresEEG {
        val media = valores.average().toInt()
        val max = valores.maxOrNull() ?: 0
        val min = valores.minOrNull() ?: 0
        val variabilidad = if (valores.size > 1) valores.zipWithNext { a, b -> kotlin.math.abs(a - b) }.average().toFloat() else 0f
        return ValoresEEG(media, max, min, variabilidad)
    }


    fun crearSesionEEG(): SesionEEGRequest {
        val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val perfilJson = prefs.getString("perfil_completo", null)

        val idUsuario = if (!perfilJson.isNullOrBlank()) {
            try {
                val usuario = Gson().fromJson(perfilJson, Usuario::class.java)
                usuario.id
            } catch (e: Exception) {
                ""
            }
        } else ""

        val duracion = ((System.currentTimeMillis() - (tiempoInicioSesion ?: System.currentTimeMillis())) / 1000).toInt()

        return SesionEEGRequest(
            usuarioId = idUsuario,
            fechaHora = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(Date()),
            duracion = duracion,
            valorMedioAtencion = calcularEEG(atencion).media.toFloat(),
            valorMedioRelajacion = calcularEEG(meditacion).media.toFloat(),
            valorMedioPestaneo = if (parpadeos.isNotEmpty()) parpadeos.average().toFloat() else 0f,
            comandosEjecutados = "Calibración completa"
        )
    }

    //Calculo para determinar perfil de calibracion del usuario con los datos recogidos
    fun determinarPerfil(at: ValoresEEG, med: ValoresEEG): PerfilCalibracion {
        return PerfilCalibracion.values().minByOrNull { perfil ->
            val distAt = kotlin.math.abs(perfil.valoresAtencion.media - at.media)
            val distMed = kotlin.math.abs(perfil.valoresMeditacion.media - med.media)
            distAt + distMed
        } ?: PerfilCalibracion.EQUILIBRADO
    }

    //TODO: joan esta es la más importante, guarda los datos en los objetos, debes aprovecharlos para mandarlos al server
    fun guardarDatos() {
        val usuario = SessionManager.usuarioActual

        Log.e("CALIBRACION", "🧠 ID del usuario en sesión: ${usuario?.id}")
        Log.e("CALIBRACION", "🧠 Usuario completo: $usuario")

        if (usuario == null || usuario.id.isBlank()) {
            Log.e("CALIBRACION", "❌ No se puede guardar: ID de usuario vacío")
            Toast.makeText(context, "Error: ID de usuario no válido", Toast.LENGTH_LONG).show()
            return
        }

        if (atencion.size < 5 || meditacion.size < 5) {
            scope.launch {
                snackbarHostState.showSnackbar("⚠️ No hay suficientes datos para calcular la calibración.")
            }
            return
        }

        val datosAt = calcularEEG(atencion)
        val datosMed = calcularEEG(meditacion)
        val datosBlink = BlinkingData(parpadeos.average().toInt(), 50)
        val perfilSeleccionado = determinarPerfil(datosAt, datosMed)

        perfilAsignadoNombre = perfilSeleccionado.nombre
        mostrarPerfilAsignado = true




        // Actualizar el usuario globalmente con datos de calibración
        val usuarioCompleto = Usuario(
            id = usuario.id,
            username = usuario.username,
            email = usuario.email,
            password = "", // o guárdalo si lo tienes
            perfilCalibracion = perfilSeleccionado.nombre,
            valoresAtencion = datosAt,
            valoresMeditacion = datosMed,
            blinking = datosBlink,
            alternancia = perfilSeleccionado.alternancia
        )

        SessionManager.usuarioActual = usuarioCompleto

//TODO TENGO Q VER SI ESTO SE GUARDA O ALGO POR Q CREO RECORDAR Q NO APARECIA PARAPDEO Y ALTERNENCIA
        // Crear sesión EEG
        sesionEEGGenerada = crearSesionEEG()

        Log.i(TAG, "✅ Datos guardados en usuario:")
        Log.i(TAG, "   Usuario: ${usuario.username}")
        Log.i(TAG, "   Perfil: ${perfilSeleccionado.nombre}")
        Log.i(TAG, "   Atención: $datosAt")
        Log.i(TAG, "   Meditación: $datosMed")
        Log.i(TAG, "   Parpadeos: $datosBlink")
        Log.i(TAG, "   Alternancia: ${perfilSeleccionado.alternancia}")

        scope.launch {
            snackbarHostState.showSnackbar("✅ Datos guardados y sesión generada")
        }

        val perfilRequest = PerfilCalibracionRequest(
            usuarioId = usuario.id,
            tipo = perfilSeleccionado.nombre,
            valoresAtencion = datosAt,
            valoresMeditacion = datosMed,
            alternancia = perfilSeleccionado.alternancia,
            blinking = datosBlink
        )

        scope.launch {
            try {
                val response = ApiClient.getApiService().crearPerfil(perfilRequest)
                if (response.isSuccessful) {
                    snackbarHostState.showSnackbar("✅ Perfil creado correctamente: ${perfilSeleccionado.nombre}")
                } else {
                    snackbarHostState.showSnackbar("❌ Error al crear perfil: ${response.code()}")
                }
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("⚠️ Error red al crear perfil: ${e.localizedMessage}")
            }
        }

    }


    // UI
    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Spacer(Modifier.height(35.dp))
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surface)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("🧠 Calibración NeuroSky", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(16.dp))

            Text("Estado: $estadoConexion",
                color = when {
                    conectado -> Color.Green
                    intentandoConectar -> Color(0xFFFFA500)
                    else -> Color.Red
                }
            )

            Text("Señal: ${getSignalQualityDescription(signalLevel)} ($signalLevel)",
                color = getSignalColor(signalLevel)
            )

            Spacer(Modifier.height(8.dp))
            Text("Atención actual: $atencionActual | Meditación: $meditacionActual", color = MaterialTheme.colorScheme.onSurface)

            Spacer(Modifier.height(16.dp))

            if (!conectado && !intentandoConectar) {
                Button(onClick = { iniciarConexion() }) {
                    Text("Conectar")
                }
            }

            if (conectado && !recogiendoDatos && !datosListos) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Button(
                        onClick = { iniciarFase("ENFOQUE") },
                        enabled = signalLevel < 150
                    ) {
                        Text("Iniciar calibración")
                    }

                    if (signalLevel >= 150) {
                        Spacer(Modifier.height(8.dp))
                        Text("⚠️ Mejora la colocación de la diadema",
                            color = Color.Yellow)
                    }
                }
            }

            if (recogiendoDatos) {
                Text("Fase: $faseActual", color = MaterialTheme.colorScheme.primary)
                Text(mensajeInstruccion, color = MaterialTheme.colorScheme.secondary)
                Text("Tiempo restante: $tiempoRestante s", color = MaterialTheme.colorScheme.onSurface)
            }

            if (datosListos && resultadoAtencion == null) {
                resultadoAtencion = calcularEEG(atencion)
                resultadoMeditacion = calcularEEG(meditacion)
                resultadoParpadeo = BlinkingData(parpadeos.average().toInt(), 50)
            }

            if (datosListos) {
                Spacer(Modifier.height(8.dp))

                Button(onClick = {
                    val usuarioSesion = SessionManager.usuarioActual

                    if (usuarioSesion == null || usuarioSesion.id.isBlank()) {
                        scope.launch {
                            snackbarHostState.showSnackbar("❌ Error: No hay usuario en sesión o ID vacío.")
                        }
                        return@Button
                    }

                    guardarDatos()
                    resultadoAtencion = null
                    resultadoMeditacion = null
                    resultadoParpadeo = null
                }) {
                    Text("💾 Guardar datos")
                }

                Spacer(Modifier.height(8.dp))
                Button(onClick = {
                    atencion.clear()
                    meditacion.clear()
                    parpadeos.clear()
                    datosListos = false
                    sesionEEGGenerada = null
                }) { Text("🔄 Repetir calibración") }
            }

            resultadoAtencion?.let { at ->
                resultadoMeditacion?.let { med ->
                    resultadoParpadeo?.let { blink ->
                        Card(Modifier.padding(16.dp), colors = cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                            Column(Modifier.padding(16.dp)) {
                                Text("📊 Resultados actuales:", color = MaterialTheme.colorScheme.primary)
                                Text("Atención media: ${at.media}", color = MaterialTheme.colorScheme.primary)
                                Text("Variabilidad de atención: ${at.variabilidad}", color = Color.Green)
                                Text("Meditación media: ${med.media}", color = MaterialTheme.colorScheme.secondary)
                                Text("Variabilidad de meditación: ${med.variabilidad}", color = Color.Blue)
                                Text("Parpadeo promedio: ${blink.fuerzaPromedio}", color = MaterialTheme.colorScheme.tertiary)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Card(Modifier.padding(16.dp), colors = cardColors(contentColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(16.dp)) {
                    SessionManager.usuarioActual?.let { user ->
                        Text("👤 Usuario: ${user.username}", color = MaterialTheme.colorScheme.primary)
                        Text("Perfil: ${user.perfilCalibracion}", color = MaterialTheme.colorScheme.primary)
                        Text("Atención media: ${user.valoresAtencion.media}", color = MaterialTheme.colorScheme.primary)
                        Text("Variabilidad de atención: ${user.valoresAtencion.variabilidad}", color = MaterialTheme.colorScheme.primary)
                        Text("Meditación media: ${user.valoresMeditacion.media}", color = MaterialTheme.colorScheme.primary)
                        Text("Variabilidad de meditación: ${user.valoresMeditacion.variabilidad}", color = MaterialTheme.colorScheme.primary)
                        Text("Parpadeo promedio: ${user.blinking.fuerzaPromedio}", color = MaterialTheme.colorScheme.primary)
                    }

                }
            }

            sesionEEGGenerada?.let {
                Card(Modifier.padding(16.dp), colors = CardDefaults.cardColors(contentColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("📄 Sesión EEG generada:", color = MaterialTheme.colorScheme.primary)
                        Text("Duración: ${it.duracion}s", color = Color.LightGray)
                        Text("Atención: ${"%.1f".format(it.valorMedioAtencion)}", color = Color.Cyan)
                        Text("Relajación: ${"%.1f".format(it.valorMedioRelajacion)}", color = Color.Blue)
                        Text("Parpadeo: ${"%.1f".format(it.valorMedioPestaneo)}", color = Color.Yellow)
                    }
                }
            }

            if (sesionEEGGenerada != null && !sesionEnviada) {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                val response = ApiClient.getApiService().crearSesionEEG(sesionEEGGenerada!!)
                                if (response.isSuccessful) {
                                    sesionEnviada = true
                                    snackbarHostState.showSnackbar("✅ Sesión enviada correctamente al servidor")
                                } else {
                                    snackbarHostState.showSnackbar("❌ Error al guardar sesión: ${response.code()}")
                                }
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("⚠️ Error de red: ${e.localizedMessage}")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(16.dp)
                ) {
                    Text("🚀 Enviar sesión al servidor")
                }
            }

            Spacer(Modifier.height(32.dp))

            if (mostrarPerfilAsignado) {
                AlertDialog(
                    onDismissRequest = { mostrarPerfilAsignado = false },
                    title = { Text("🎯 Perfil asignado") },
                    text = { Text("Se te ha asignado el perfil: $perfilAsignadoNombre") },
                    confirmButton = {
                        TextButton(onClick = { mostrarPerfilAsignado = false }) {
                            Text("Aceptar")
                        }
                    }
                )
            }
        }
    }
}



@Composable
@Preview
fun PreviewFaseEnfoqueScreen() {
    CalibracionCompletaScreen(navController = rememberNavController())
}