package com.example.mindmoving.views

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.mindmoving.neuroSkyService.CustomNeuroSky
import com.neurosky.thinkgear.TGDevice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.mindmoving.neuroSkyService.NeuroSkyListener
import com.example.mindmoving.views.controlCoche.ConnectionStatus
import kotlinx.coroutines.flow.update


// Contiene los datos EEG recibidos en un instante
data class EEGData(
    val attention: Int = 0,
    val meditation: Int = 0,
    val blinkStrength: Int = 0
)

// Representa la calidad de la señal
data class SignalQuality(
    val level: Int = 200, // 0 (excelente) a 200 (sin contacto)
    val description: String = "Sin Contacto"
)

private const val TAG = "NeuroSkyManager"

class NeuroSkyManager(private val context: Context){

    // --- 1. Flows para emitir datos al exterior ---
    // Son privados y mutables para control interno
    private val _connectionState = MutableStateFlow(ConnectionStatus.DESCONECTADO)
    private val _eegData = MutableStateFlow(EEGData())
    private val _signalQuality = MutableStateFlow(SignalQuality())

    // Son públicos e inmutables para que el ViewModel los observe
    val connectionState = _connectionState.asStateFlow()
    val eegData = _eegData.asStateFlow()
    val signalQuality = _signalQuality.asStateFlow()

    // --- 2. Propiedades de NeuroSky ---
    private var neuroSky: CustomNeuroSky? = null
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    // --- 3. El Listener ---
    // Esta es la parte más importante. Escucha los eventos de la diadema y los emite a través de nuestros Flows.
    private val neuroSkyListener = object : NeuroSkyListener {

        override fun onStateChanged(state: Int) {
            val newState = when (state) {
                TGDevice.STATE_CONNECTING -> ConnectionStatus.CONECTANDO
                TGDevice.STATE_CONNECTED -> {
                    neuroSky?.start() // Inicia la recolección de datos al conectar
                    ConnectionStatus.CONECTADO
                }
                else -> ConnectionStatus.DESCONECTADO
            }
            _connectionState.value = newState
            Log.i(TAG, "🔄 Estado conexión: $newState")
        }

        override fun onSignalPoor(signal: Int) {
            val quality = SignalQuality(
                level = signal,
                description = getSignalQualityDescription(signal)
            )
            _signalQuality.value = quality
            Log.d(TAG, "📡 Nivel señal: $signal (${quality.description})")
        }

        override fun onAttentionReceived(level: Int) {
            // Actualizamos el valor de atención, manteniendo los demás
            _eegData.update { it.copy(attention = level) }
            Log.d(TAG, "🧠 Atención: $level")
        }

        override fun onMeditationReceived(level: Int) {
            _eegData.update { it.copy(meditation = level) }
            Log.d(TAG, "🧘 Meditación: $level")
        }

        override fun onBlinkDetected(strength: Int) {
            _eegData.update { it.copy(blinkStrength = strength) }
            Log.d(TAG, "👁️ Parpadeo detectado: $strength")
        }

    }

    // --- 4. Inicialización ---
    init {
        if (bluetoothAdapter != null) {
            // Simplemente creamos nuestra clase CustomNeuroSky pasándole el adaptador y NUESTRO listener.
            // CustomNeuroSky se encargará del Handler internamente.
            neuroSky = CustomNeuroSky(bluetoothAdapter, neuroSkyListener)
        } else {
            Log.e(TAG, "❌ Este dispositivo no soporta Bluetooth")
        }
    }

    // --- 5. Métodos Públicos (la API del Manager) ---

    @SuppressLint("MissingPermission")
    fun conectar() {
        if (bluetoothAdapter?.isEnabled == false) {
            Log.e(TAG, "❌ Bluetooth está desactivado.")
            // Aquí podrías emitir un evento de error si quisieras
            return
        }

        if (_connectionState.value == ConnectionStatus.CONECTADO || _connectionState.value == ConnectionStatus.CONECTANDO) {
            Log.w(TAG, "⚠️ Ya se está conectando o ya está conectado.")
            return
        }

        val mindWaveDevice = bluetoothAdapter?.bondedDevices?.firstOrNull {
            it.name.contains("MindWave", ignoreCase = true)
        }

        if (mindWaveDevice != null) {
            _connectionState.value = ConnectionStatus.CONECTANDO
            neuroSky?.connectTo(mindWaveDevice)
        } else {
            Log.e(TAG, "❌ Dispositivo MindWave no encontrado en los dispositivos emparejados.")
            // Podrías emitir un error
        }

    }

    fun desconectar() {
        neuroSky?.disconnect()
        _connectionState.value = ConnectionStatus.DESCONECTADO
        Log.i(TAG, "🔌 Diadema desconectada.")
    }


}



// --- 6. Funciones de Ayuda ---
// Mueve aquí la función que traduce el nivel de señal a texto
private fun getSignalQualityDescription(signal: Int): String {
    return when {
        signal == 0 -> "Excelente"
        signal in 1..50 -> "Buena"
        signal in 51..100 -> "Aceptable"
        signal in 101..150 -> "Débil"
        signal in 151..199 -> "Muy débil"
        else -> "Sin Contacto / No colocada"
    }
}