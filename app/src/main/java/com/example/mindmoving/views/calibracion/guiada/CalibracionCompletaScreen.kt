package com.example.mindmoving.views.calibracion.guiada

import android.bluetooth.BluetoothAdapter
import android.util.Log
import androidx.compose.foundation.background
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.mindmoving.neuroSkyService.CustomNeuroSky
import com.example.mindmoving.neuroSkyService.NeuroSkyListener
import com.example.mindmoving.retrofit.models.*
import com.neurosky.thinkgear.TGDevice
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "FaseCalibracionCompleta"

@Composable
fun CalibracionCompletaScreen(navController: NavHostController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var conectado by remember { mutableStateOf(false) }
    var intentandoConectar by remember { mutableStateOf(false) }
    var estadoConexion by remember { mutableStateOf("Desconectado") }
    var signalLevel by remember { mutableStateOf(200) }
    var estadoDiadema by remember { mutableStateOf("Sin conexión") }

    var usuario by remember {
        mutableStateOf(
            Usuario(1, "usuario_calibracion", "test@test.com", "1234", "",
                ValoresEEG(0,0,0,0f), ValoresEEG(0,0,0,0f), BlinkingData(0,50), AlternanciaData(0,0))
        )
    }

    val atencion = remember { mutableStateListOf<Int>() }
    val meditacion = remember { mutableStateListOf<Int>() }
    val parpadeos = remember { mutableStateListOf<Int>() }

    var tiempoRestante by remember { mutableStateOf(15) }
    var faseActual by remember { mutableStateOf("ENFOQUE") }
    var recogiendoDatos by remember { mutableStateOf(false) }
    var datosListos by remember { mutableStateOf(false) }
    var mensajeInstruccion by remember { mutableStateOf("Prepárate para enfocarte") }
    var tiempoInicioSesion by remember { mutableStateOf<Long?>(null) }
    var sesionEEGGenerada by remember { mutableStateOf<SesionEEGRequest?>(null) }

    var atencionActual by remember { mutableStateOf(0) }
    var meditacionActual by remember { mutableStateOf(0) }

    var resultadoAtencion by remember { mutableStateOf<ValoresEEG?>(null) }
    var resultadoMeditacion by remember { mutableStateOf<ValoresEEG?>(null) }
    var resultadoParpadeo by remember { mutableStateOf<BlinkingData?>(null) }


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
                    estadoDiadema = when {
                        signal == 0 -> "📡 Señal excelente"
                        signal in 1..50 -> "📡 Señal buena"
                        signal in 51..100 -> "⚠️ Señal aceptable"
                        signal in 101..150 -> "⚠️ Señal débil"
                        signal in 151..199 -> "🚫 Señal muy débil"
                        signal >= 200 -> "❌ Diadema mal colocada o sin contacto"
                        else -> "❓ Estado desconocido"
                    }
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

    fun iniciarFase(nombreFase: String) {
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

    fun calcularEEG(valores: List<Int>): ValoresEEG {
        val media = valores.average().toInt()
        val max = valores.maxOrNull() ?: 0
        val min = valores.minOrNull() ?: 0
        val variabilidad = if (valores.size > 1) valores.zipWithNext { a, b -> kotlin.math.abs(a - b) }.average().toFloat() else 0f
        return ValoresEEG(media, max, min, variabilidad)
    }

    fun crearSesionEEG(): SesionEEGRequest {
        val duracion = ((System.currentTimeMillis() - (tiempoInicioSesion ?: System.currentTimeMillis())) / 1000).toInt()
        return SesionEEGRequest(
            usuarioId = usuario.id.toString(),
            fechaHora = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(Date()),
            duracion = duracion,
            valorMedioAtencion = calcularEEG(atencion).media.toFloat(),
            valorMedioRelajacion = calcularEEG(meditacion).media.toFloat(),
            valorMedioPestaneo = if (parpadeos.isNotEmpty()) parpadeos.average().toFloat() else 0f,
            comandosEjecutados = "Calibración completa"
        )
    }
    fun determinarPerfil(at: ValoresEEG, med: ValoresEEG): PerfilCalibracion {
        return PerfilCalibracion.values().minByOrNull { perfil ->
            val distAt = kotlin.math.abs(perfil.valoresAtencion.media - at.media)
            val distMed = kotlin.math.abs(perfil.valoresMeditacion.media - med.media)
            distAt + distMed
        } ?: PerfilCalibracion.EQUILIBRADO
    }

    fun guardarDatos() {
        val datosAt = calcularEEG(atencion)
        val datosMed = calcularEEG(meditacion)
        val datosBlink = BlinkingData(parpadeos.average().toInt(), 50)

        val perfilSeleccionado = determinarPerfil(datosAt, datosMed)

        usuario = usuario.copy(
            perfilCalibracion = perfilSeleccionado.nombre,
            valoresAtencion = datosAt,
            valoresMeditacion = datosMed,
            blinking = datosBlink,
            alternancia = perfilSeleccionado.alternancia
        )

        sesionEEGGenerada = crearSesionEEG()

        Log.i(TAG, "✅ Datos guardados en usuario:")
        Log.i(TAG, "   Perfil: ${perfilSeleccionado.nombre}")
        Log.i(TAG, "   Atención: $datosAt")
        Log.i(TAG, "   Meditación: $datosMed")
        Log.i(TAG, "   Parpadeos: $datosBlink")
        Log.i(TAG, "   Alternancia: ${perfilSeleccionado.alternancia}")

        scope.launch { snackbarHostState.showSnackbar("✅ Datos guardados y sesión generada") }
    }


    // UI
    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).background(Color(0xFF101020)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("🧠 Calibración NeuroSky", color = Color.Cyan, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))
            Text("Estado: $estadoConexion", color = if (conectado) Color.Green else Color.Red)
            Text("Estado diadema: $estadoDiadema", color = Color.Yellow)
            Text("Señal: $signalLevel", color = Color.LightGray)
            Spacer(Modifier.height(8.dp))
            Text("Atención actual: $atencionActual | Meditación: $meditacionActual", color = Color.White)
            Spacer(Modifier.height(16.dp))

            if (!conectado && !intentandoConectar) Button(onClick = { iniciarConexion() }) { Text("Conectar") }
            if (conectado && !recogiendoDatos && !datosListos) Button(onClick = { iniciarFase("ENFOQUE") }) { Text("Iniciar calibración") }

            if (recogiendoDatos) {
                Text("Fase: $faseActual", color = Color.White)
                Text(mensajeInstruccion, color = Color.Yellow)
                Text("Tiempo restante: $tiempoRestante s", color = Color.White)
            }

            if (datosListos && resultadoAtencion == null) {
                resultadoAtencion = calcularEEG(atencion)
                resultadoMeditacion = calcularEEG(meditacion)
                resultadoParpadeo = BlinkingData(parpadeos.average().toInt(), 50)
            }

            if (datosListos) {
                Spacer(Modifier.height(8.dp))
                Button(onClick = {
                    guardarDatos()
                    resultadoAtencion = null
                    resultadoMeditacion = null
                    resultadoParpadeo = null
                }) { Text("💾 Guardar datos") }

                Spacer(Modifier.height(8.dp))
                Button(onClick = {
                    atencion.clear(); meditacion.clear(); parpadeos.clear()
                    datosListos = false; sesionEEGGenerada = null
                }) { Text("🔄 Repetir calibración") }
            }

            resultadoAtencion?.let { at ->
                resultadoMeditacion?.let { med ->
                    resultadoParpadeo?.let { blink ->
                        Card(Modifier.padding(16.dp), colors = CardDefaults.cardColors(Color(0xFF1E1E2E))) {
                            Column(Modifier.padding(16.dp)) {
                                Text("📊 Resultados actuales:", color = Color.White)
                                Text("Atención media: ${at.media}", color = Color.Green)
                                Text("Meditación media: ${med.media}", color = Color.Blue)
                                Text("Parpadeo promedio: ${blink.fuerzaPromedio}", color = Color.Yellow)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Card(Modifier.padding(16.dp), colors = CardDefaults.cardColors(Color(0xFF2A2A3E))) {
                Column(Modifier.padding(16.dp)) {
                    Text("👤 Usuario: ${usuario.username}", color = Color.Cyan)
                    Text("Perfil: ${usuario.perfilCalibracion}", color = Color.White)
                    Text("Atención media: ${usuario.valoresAtencion.media}", color = Color.Green)
                    Text("Meditación media: ${usuario.valoresMeditacion.media}", color = Color.Blue)
                    Text("Parpadeo promedio: ${usuario.blinking.fuerzaPromedio}", color = Color.Yellow)
                }
            }

            sesionEEGGenerada?.let {
                Card(Modifier.padding(16.dp), colors = CardDefaults.cardColors(Color(0xFF0F3460))) {
                    Column(Modifier.padding(16.dp)) {
                        Text("📄 Sesión EEG generada:", color = Color.White)
                        Text("Duración: ${it.duracion}s", color = Color.LightGray)
                        Text("Atención: ${"%.1f".format(it.valorMedioAtencion)}", color = Color.Cyan)
                        Text("Relajación: ${"%.1f".format(it.valorMedioRelajacion)}", color = Color.Blue)
                        Text("Parpadeo: ${"%.1f".format(it.valorMedioPestaneo)}", color = Color.Yellow)
                    }
                }
            }
        }
    }
}

@Composable
@Preview
fun PreviewFaseEnfoqueScreen() {
    CalibracionCompletaScreen(navController = rememberNavController())
}