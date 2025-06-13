package com.example.mindmoving.views.controlCoche

import android.app.Application
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindmoving.retrofit.models.PerfilCalibracion
import com.example.mindmoving.retrofit.models.SesionEEGRequest
import com.example.mindmoving.views.EEGData
import com.example.mindmoving.views.NeuroSkyManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ComandosDiademaViewModel(application: Application) : AndroidViewModel(application) {

    // --- DEPENDENCIAS ---
    private val repository: UsuarioRepository = UsuarioRepository()
    private val neuroSkyManager: NeuroSkyManager = NeuroSkyManager(application.applicationContext)

    // --- GESTIÓN DEL ESTADO ---
    private val _uiState = MutableStateFlow(ComandosDiademaState())
    val uiState = _uiState.asStateFlow()

    // --- PROPIEDADES INTERNAS DE LA SESIÓN ---
    private var sesionJob: Job? = null
    private val atencionRecogida = mutableListOf<Int>()
    private val meditacionRecogida = mutableListOf<Int>()
    private val parpadeosRecogidos = mutableListOf<Int>()
    private var comandosEjecutadosStr = ""

    // --- INICIALIZACIÓN ---
    init {
        cargarUsuarioYVerificarPerfil()
        iniciarConexionDiadema()
    }

    // --- EVENTOS DESDE LA UI ---

    fun onBotonSesionClick() {
        if (uiState.value.sesionActiva) {
            detenerSesion()
        } else {
            iniciarSesion()
        }
    }

    // --> AÑADIDO: Función para que la UI pueda manejar el aviso
    fun onIgnorarAvisoCalibracion() {
        _uiState.update { it.copy(necesitaCalibracion = false) }
    }

    // Opcional: para manejar clics manuales en el D-pad si fuera necesario
    fun onDpadClick(direction: Direction) {
        println("El usuario ha presionado manualmente: $direction")
    }

    // --- LÓGICA DE FLUJO PRINCIPAL ---

    private fun cargarUsuarioYVerificarPerfil() {
        viewModelScope.launch {
            val usuarioConPerfil = repository.getUsuarioConPerfil()
            _uiState.update { it.copy(usuario = usuarioConPerfil) }

            if (usuarioConPerfil == null || usuarioConPerfil.perfilCalibracion.isNullOrBlank() || usuarioConPerfil.perfilCalibracion.equals("ninguno", ignoreCase = true)) {
                _uiState.update { it.copy(necesitaCalibracion = true, perfilVerificado = true) }
            } else {
                _uiState.update { it.copy(necesitaCalibracion = false, perfilVerificado = true) }
            }
        }
    }


    private fun iniciarConexionDiadema() {
        neuroSkyManager.conectar()

        neuroSkyManager.connectionState
            .onEach { estado -> _uiState.update { it.copy(estadoConexion = estado) } }
            .launchIn(viewModelScope)

        neuroSkyManager.signalQuality
            .onEach { calidad -> _uiState.update { it.copy(calidadSeñal = calidad.level) } }
            .launchIn(viewModelScope)

        neuroSkyManager.eegData
            .onEach { datos ->
                _uiState.update {
                    it.copy(
                        atencionActual = datos.attention,
                        meditacionActual = datos.meditation,
                        fuerzaParpadeoActual = datos.blinkStrength
                    )
                }
                if (uiState.value.sesionActiva) {
                    procesarEEG(datos)
                }
            }
            .launchIn(viewModelScope)
    }


    private fun iniciarSesion() {
        if (sesionJob?.isActive == true) return
        sesionJob?.cancel()

        // --> AÑADIDO: Limpieza de datos de la sesión anterior
        atencionRecogida.clear()
        meditacionRecogida.clear()
        parpadeosRecogidos.clear()
        comandosEjecutadosStr = ""

        sesionJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    sesionActiva = true,
                    tiempoRestanteSeg = 120
                )
            }
            while (uiState.value.tiempoRestanteSeg > 0) {
                delay(1000)
                _uiState.update { it.copy(tiempoRestanteSeg = it.tiempoRestanteSeg - 1) }
            }
            detenerSesion()
        }
    }


    private fun detenerSesion() {
        sesionJob?.cancel()
        sesionJob = null
        _uiState.update { it.copy(sesionActiva = false) }

        // --> COMPLETADO: Ahora llamamos a la función de guardado
        guardarResultadosDeSesion()
    }


    private fun procesarEEG(datos: EEGData) {
        // --> AÑADIDO: Recopilación de datos en cada tick
        atencionRecogida.add(datos.attention)
        meditacionRecogida.add(datos.meditation)
        if (datos.blinkStrength > 0) {
            parpadeosRecogidos.add(datos.blinkStrength)
        }

        val usuario = uiState.value.usuario ?: return
        // --> MEJORA: Usamos la constante del enum en lugar de un String "hardcodeado"
        val perfil = PerfilCalibracion.values().find { it.nombre == usuario.perfilCalibracion }
            ?: PerfilCalibracion.EQUILIBRADO

        // --> MEJORA: La lógica de umbrales ahora usa los datos reales del perfil
        val umbralAtencion = perfil.valoresAtencion.media * 1.20 // Ejemplo: 20% sobre la media
        val umbralMeditacion = perfil.valoresMeditacion.media * 1.20

        var comandoGenerado: Direction? = null

        when {
            datos.attention > umbralAtencion -> comandoGenerado = Direction.UP
            // --> MEJORA: Usamos el umbral dinámico también para la meditación
            datos.meditation > umbralMeditacion -> comandoGenerado = Direction.DOWN
        }

        if (comandoGenerado != null) {
            // --> AÑADIDO: Registro del comando ejecutado
            if (comandosEjecutadosStr.isNotEmpty()) {
                comandosEjecutadosStr += ","
            }
            comandosEjecutadosStr += comandoGenerado.name

            // La lógica de resaltar el botón se mantiene igual
            if (uiState.value.comandoActivado != comandoGenerado) {
                _uiState.update { it.copy(comandoActivado = comandoGenerado) }
                viewModelScope.launch {
                    delay(500)
                    if (uiState.value.comandoActivado == comandoGenerado) {
                        _uiState.update { it.copy(comandoActivado = null) }
                    }
                }
            }
        }
    }


    // --> COMPLETADO: Función final para guardar los datos
    private fun guardarResultadosDeSesion() {
        val usuario = uiState.value.usuario
        if (usuario == null || atencionRecogida.isEmpty()) {
            Log.w("ViewModel", "Guardado cancelado: no hay usuario o no se recogieron datos.")
            return
        }

        val duracionReal = 120 - uiState.value.tiempoRestanteSeg
        val sesionRequest = SesionEEGRequest(
            usuarioId = usuario.id,
            fechaHora = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(Date()),
            duracion = duracionReal,
            valorMedioAtencion = atencionRecogida.average().toFloat(),
            valorMedioRelajacion = meditacionRecogida.average().toFloat(),
            valorMedioPestaneo = parpadeosRecogidos.average().takeIf { !it.isNaN() }?.toFloat() ?: 0f,
            comandosEjecutados = comandosEjecutadosStr.removeSuffix(",")
        )

        viewModelScope.launch {
            _uiState.update { it.copy(mensajeUsuario = "Guardando sesión...") }
            val exito = repository.guardarSesionDeJuego(sesionRequest)
            val mensaje = if (exito) "Sesión guardada con éxito" else "Error al guardar la sesión"

            _uiState.update { it.copy(mensajeUsuario = mensaje) }

            delay(3000)
            _uiState.update { it.copy(mensajeUsuario = null) }
        }
    }
}