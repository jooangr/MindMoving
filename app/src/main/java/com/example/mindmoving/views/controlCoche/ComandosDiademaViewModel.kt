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


            // --- CÁLCULO DE UMBRALES PARA LA UI ---
            // Obtenemos el perfil o el de por defecto, igual que en procesarEEG
            val perfil = PerfilCalibracion.values().find { it.nombre == usuarioConPerfil?.perfilCalibracion }
                ?: PerfilCalibracion.EQUILIBRADO

            // Calculamos los umbrales numéricos
            val umbralAtencionUI = (perfil.valoresAtencion.media * 1.10).toInt()
            val umbralMeditacionUI = (perfil.valoresMeditacion.media * 1.10).toInt()

            // Creamos el objeto para el State
            val nuevosUmbrales = UmbralesUI(
                atencion = umbralAtencionUI,
                meditacion = umbralMeditacionUI
                // Los de parpadeo se quedan con su texto por defecto
            )

            // Actualizamos el estado con el usuario Y los umbrales calculados
            _uiState.update {
                it.copy(
                    usuario = usuarioConPerfil,
                    umbrales = nuevosUmbrales
                )
            }

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

    // --> AÑADIDO: Nuevas propiedades para la lógica de control
    private var ultimoComandoMovimiento: Direction? = null
    private var ultimoTiempoParpadeo: Long = 0L
    private val INTERVALO_DOBLE_PARPADEO = 1500 // 1.5 segundos


    // Umbral para parpadeos
    private val UMBRAL_PARPADEO_NORMAL = 45
    private val UMBRAL_PARPADEO_FUERTE = 80
    private val INTERVALO_MIN_DOBLE_PARPADEO = 200L // 0.2 segundos
    private val INTERVALO_MAX_DOBLE_PARPADEO = 1500L // 1.5 segundos
    private var ultimoTiempoParpadeoNormal: Long = 0L


    /**
     * FUNCIÓN PARA PROCESAR LOS DATOS EEG RECIBIDOS
     * Contiene toda la lógica del proceso de datos para activar los botones según los principios establecidos.
     */
    private fun procesarEEG(datos: EEGData) {
        if (!uiState.value.sesionActiva) return

        // Recopilación de datos
        atencionRecogida.add(datos.attention)
        meditacionRecogida.add(datos.meditation)
        if (datos.blinkStrength > 0) {
            parpadeosRecogidos.add(datos.blinkStrength)
        }

        // --- Definición de Umbrales ---
        val perfilUsuario = PerfilCalibracion.values().find { it.nombre == uiState.value.usuario?.perfilCalibracion }
            ?: PerfilCalibracion.EQUILIBRADO
        val umbralAtencion = (perfilUsuario.valoresAtencion.media * 1.10).toInt()
        val umbralMeditacion = (perfilUsuario.valoresMeditacion.media * 1.10).toInt()


        var comandoGenerado: Direction? = null

        // --- Inicialización de variables para este tick ---
        var comandoMovimiento: Direction? = null
        var comandoDireccion: Direction? = null
        val ahora = System.currentTimeMillis()


        // --- LÓGICA DE PRIORIDADES Y GRUPOS ---

        // ** PRIORIDAD MÁXIMA: Freno de Mano (CENTER) **
        // Condición: Dos parpadeos fuertes en el intervalo de tiempo correcto.
        val esDobleParpadeoFuerte = datos.blinkStrength > UMBRAL_PARPADEO_FUERTE &&
                (ahora - ultimoTiempoParpadeoNormal > INTERVALO_MIN_DOBLE_PARPADEO) &&
                (ahora - ultimoTiempoParpadeoNormal < INTERVALO_MAX_DOBLE_PARPADEO)

        if (esDobleParpadeoFuerte) {
            comandoMovimiento = Direction.CENTER
            // Reseteamos el tiempo para que esta acción sea un pulso único
            ultimoTiempoParpadeoNormal = 0L

        } else {
            // ** Si no estamos frenando, evaluamos los otros comandos **

            // ** GRUPO A: Dirección (Izquierda/Derecha) - Lógica de Pulsos **
            if (datos.blinkStrength in UMBRAL_PARPADEO_NORMAL until UMBRAL_PARPADEO_FUERTE) {
                val tiempoDesdeUltimoParpadeo = ahora - ultimoTiempoParpadeoNormal

                // Condición para DOBLE PARPADEO (LEFT)
                if (tiempoDesdeUltimoParpadeo in (INTERVALO_MIN_DOBLE_PARPADEO + 1) until INTERVALO_MAX_DOBLE_PARPADEO) {
                    comandoDireccion = Direction.LEFT
                    ultimoTiempoParpadeoNormal = 0L // Reseteamos para que no se active en cadena
                }
                // Condición para PARPADEO ÚNICO (RIGHT) - con debounce
                else if (tiempoDesdeUltimoParpadeo > INTERVALO_MAX_DOBLE_PARPADEO) {
                    comandoDireccion = Direction.RIGHT
                    ultimoTiempoParpadeoNormal = ahora // Guardamos el tiempo para un posible segundo parpadeo
                }
            }

            // ** GRUPO B: Movimiento (Arriba/Abajo) - Lógica Sostenida **
            when {
                datos.attention > umbralAtencion -> comandoMovimiento = Direction.UP
                datos.meditation > umbralMeditacion -> comandoMovimiento = Direction.DOWN
                else -> comandoMovimiento = null // Se desactiva si no se cumple ninguna condición
            }
        }



        //Añadimos LOGS para ver los valores en tiempo real
        Log.d(
            "EEG_Debug",
            "Datos -> Atención: ${datos.attention} (Umbral: $umbralAtencion) | Meditación: ${datos.meditation} (Umbral: $umbralMeditacion) | Parpadeo: ${datos.blinkStrength}"
        )

        // Actualizamos la memoria del último comando de movimiento
        //ultimoComandoMovimiento = comandoMovimientoActual

        // --- ACTUALIZACIÓN DEL ESTADO DE LA UI ---

        // Modelo de activación mixto:
        // Los comandos de parpadeo (dirección/centro) son pulsos.
        // Los comandos de estado mental (movimiento) son sostenidos.

        // 1. Actualizar estado de movimiento sostenido
        if (comandoMovimiento != uiState.value.comandoMovimientoActivado) {
            _uiState.update { it.copy(comandoMovimientoActivado = comandoMovimiento) }
            comandoMovimiento?.let { registrarComando(it) }
        }

        // 2. Actualizar estado de dirección con un pulso
        if (comandoDireccion != null) {
            _uiState.update { it.copy(comandoDireccionActivado = comandoDireccion) }
            registrarComando(comandoDireccion)

            // Lanzamos un reseteo visual solo para el comando de dirección
            viewModelScope.launch {
                delay(300) // Un pulso visual corto para los giros
                if (uiState.value.comandoDireccionActivado == comandoDireccion) {
                    _uiState.update { it.copy(comandoDireccionActivado = null) }
                }
            }
        }

        // El Freno de Mano (CENTER) también es un pulso
        if (comandoMovimiento == Direction.CENTER) {
            viewModelScope.launch {
                delay(500) // Un pulso visual un poco más largo para el freno
                if (uiState.value.comandoMovimientoActivado == Direction.CENTER) {
                    _uiState.update { it.copy(comandoMovimientoActivado = null) }
                }
            }

        }
        // Reseteo visual después de un tiempo (para que no se quede el botón "presionado")
        viewModelScope.launch {
            delay(500)
            _uiState.update {
                it.copy(
                    comandoMovimientoActivado = null,
                    comandoDireccionActivado = null
                )
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

    //Lógica de registro de comandos, estrído del método principal para limpiar código.
    private fun registrarComando(direccion: Direction) {
        if (comandosEjecutadosStr.isNotEmpty()) {
            comandosEjecutadosStr += ","
        }
        comandosEjecutadosStr += direccion.name
    }

    //Limpieza de recursos cuando el ViewModel es destruido
    override fun onCleared() {
        super.onCleared()
        Log.i("ViewModel", "ViewModel destruido. Desconectando diadema...")
        neuroSkyManager.desconectar()
    }

}