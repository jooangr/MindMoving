package com.example.mindmoving.views.controlCoche

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindmoving.neuroSkyService.EEGData
import com.example.mindmoving.neuroSkyService.NeuroSkyManager
import com.example.mindmoving.retrofit.models.perfilCalibracion.PerfilCalibracion
import com.example.mindmoving.retrofit.models.sesionesEGG.SesionEEGRequest
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
        escucharEventosDeLaDiadema()
    }

    // --- EVENTOS DESDE LA UI ---

    // --> NUEVA FUNCIÓN PÚBLICA: La UI llamará a esto para iniciar la conexión.
    fun onConectarDiademaClick() {
        // Evitamos múltiples intentos de conexión si ya se está conectando.
        if (uiState.value.estadoConexion == ConnectionStatus.CONECTANDO) return

        neuroSkyManager.conectar()
    }

    fun onBotonSesionClick() {

        if (uiState.value.estadoConexion != ConnectionStatus.CONECTADO) {
            _uiState.update { it.copy(mensajeUsuario = "La diadema no está conectada.") }
            return
        }

        if (uiState.value.sesionActiva) {
            detenerSesion()
        } else {
            iniciarSesion()
        }
    }

    //La UI la llamará cuando el usuario confirme que quiere guardar.
    fun onConfirmarGuardarSesion() {
        // Ocultamos el diálogo primero
        _uiState.update { it.copy(mostrarDialogoGuardar = false) }
        // Y luego llamamos a la lógica de guardado que ya teníamos
        guardarResultadosDeSesion()
    }

    fun onCancelarGuardarSesion() {
        // Simplemente ocultamos el diálogo. No se hace nada más.
        _uiState.update { it.copy(mostrarDialogoGuardar = false) }
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


    private fun escucharEventosDeLaDiadema() {

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

        // En lugar de guardar directamente, ahora actualizamos el estado
        // para que la UI muestre el diálogo de confirmación.
        _uiState.update {
            it.copy(
                sesionActiva = false,
                mostrarDialogoGuardar = true // <-- ¡LA CLAVE DEL CAMBIO!
            )
        }
    }


    /*
    private fun procesarEEG(datos: EEGData) {

        // --> AÑADIDO: Recopilación de datos en cada tick
        atencionRecogida.add(datos.attention)
        meditacionRecogida.add(datos.meditation)
        if (datos.blinkStrength > 0) {
            parpadeosRecogidos.add(datos.blinkStrength)
        }

        val usuario = uiState.value.usuario ?: return
        // 1. Obtener el perfil de calibración o usar uno por defecto.
        val perfilUsuario = PerfilCalibracion.values().find { it.nombre == uiState.value.usuario?.perfilCalibracion }
            ?: PerfilCalibracion.EQUILIBRADO // Si no hay perfil, usamos 'EQUILIBRADO' como predeterminado.

        // 2. Calcular los umbrales dinámicos.
        // Un comando se activa si el valor actual supera la media de calibración en un 0%.
        // Puedes ajustar este multiplicador (1.25) para hacer el juego más fácil o difícil.
        val umbralAtencion = (perfilUsuario.valoresAtencion.media * 1.20).toInt()
        val umbralMeditacion = (perfilUsuario.valoresMeditacion.media * 1.20).toInt()

        // Para los parpadeos, usamos rangos más fijos como razonamos.
        val umbralParpadeoSuave_Min = 20
        val umbralParpadeoSuave_Max = 60
        val umbralParpadeoFuerte = 75

        // 3. Determinar qué comando se activa (usando una estructura `when` para exclusividad).
        var comandoGenerado: Direction? = null

        when {
            // Prioridad 1: Parpadeo Fuerte
            datos.blinkStrength > umbralParpadeoFuerte -> {
                comandoGenerado = Direction.CENTER // Asignamos el parpadeo fuerte al botón central
            }
            // Prioridad 2: Atención
            datos.attention > umbralAtencion -> {
                comandoGenerado = Direction.UP
            }
            // Prioridad 3: Meditación
            datos.meditation > umbralMeditacion -> {
                comandoGenerado = Direction.DOWN
            }
            // Prioridad 4: Parpadeo Suave
            datos.blinkStrength in umbralParpadeoSuave_Min..umbralParpadeoSuave_Max -> {
                comandoGenerado = Direction.RIGHT
            }
        }

        // 4. Actualizar el estado para que la UI reaccione.
        // Esta lógica ya la tenías y es correcta: activa el comando y lo desactiva tras 1000ms.
        if (comandoGenerado != null) {
            if (uiState.value.comandoActivado != comandoGenerado) {
                _uiState.update { it.copy(comandoActivado = comandoGenerado) }

                // Registramos el comando ejecutado (lógica que ya tenías)
                if (comandosEjecutadosStr.isNotEmpty()) {
                    comandosEjecutadosStr += ","
                }
                comandosEjecutadosStr += comandoGenerado.name

                viewModelScope.launch {
                    delay(1000) // Duración de la "presión" visual del botón
                    if (uiState.value.comandoActivado == comandoGenerado) {
                        _uiState.update { it.copy(comandoActivado = null) }
                    }
                }
            }
        }
    }
*/

    private fun procesarEEG(datos: EEGData) {
        if (!uiState.value.sesionActiva) return

        atencionRecogida.add(datos.attention)
        meditacionRecogida.add(datos.meditation)
        if (datos.blinkStrength > 0) {
            parpadeosRecogidos.add(datos.blinkStrength)
        }

        val perfilUsuario = PerfilCalibracion.values().find { it.nombre == uiState.value.usuario?.perfilCalibracion }
            ?: PerfilCalibracion.EQUILIBRADO

        // --> CAMBIO 1: Hacemos el multiplicador más bajo para pruebas (1.10 = 10% por encima)
        val umbralAtencion = (perfilUsuario.valoresAtencion.media * 1.10).toInt()
        val umbralMeditacion = (perfilUsuario.valoresMeditacion.media * 1.10).toInt()

        val umbralParpadeoSuave_Min = 20
        val umbralParpadeoSuave_Max = 60
        val umbralParpadeoFuerte = 75

        var comandoGenerado: Direction? = null

        // --> CAMBIO 2: Añadimos LOGS para ver los valores en tiempo real
        Log.d("EEG_Debug", "Datos -> Atención: ${datos.attention} (Umbral: $umbralAtencion) | Meditación: ${datos.meditation} (Umbral: $umbralMeditacion) | Parpadeo: ${datos.blinkStrength}")

        when {
            datos.blinkStrength > umbralParpadeoFuerte -> {
                comandoGenerado = Direction.CENTER
            }
            datos.attention > umbralAtencion -> {
                comandoGenerado = Direction.UP
            }
            datos.meditation > umbralMeditacion -> {
                comandoGenerado = Direction.DOWN
            }
            datos.blinkStrength in umbralParpadeoSuave_Min..umbralParpadeoSuave_Max -> {
                comandoGenerado = Direction.RIGHT
            }
        }

        if (comandoGenerado != null) {
            // --> CAMBIO 3: Log para confirmar que un comando se ha generado
            Log.i("EEG_Debug", "¡COMANDO GENERADO: $comandoGenerado!")

            // Tu lógica para actualizar el estado se queda igual
            if (uiState.value.comandoActivado != comandoGenerado) {
                _uiState.update { it.copy(comandoActivado = comandoGenerado) }

                if (comandosEjecutadosStr.isNotEmpty()) {
                    comandosEjecutadosStr += ","
                }
                comandosEjecutadosStr += comandoGenerado.name

                viewModelScope.launch {
                    // --> CAMBIO 4: Reducimos el delay para que la respuesta sea más rápida en pruebas
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