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

        iniciarTemporizadorDeSesion()

        sesionJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    sesionActiva = true,
                    tiempoRestanteSeg = 60
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


    /*
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

        // Si estamos en modo juego, procesamos su lógica
        if (uiState.value.estadoJuego != null) {
            procesarLogicaJuego(comandoMovimiento, comandoDireccion)
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

     */


    private fun procesarEEG(datos: EEGData) {
        if (!uiState.value.sesionActiva) return

        // Recopilación de datos (sin cambios)
        atencionRecogida.add(datos.attention)
        meditacionRecogida.add(datos.meditation)
        if (datos.blinkStrength > 0) {
            parpadeosRecogidos.add(datos.blinkStrength)
        }

        // --- Definición de Umbrales (sin cambios) ---
        val perfilUsuario = PerfilCalibracion.values().find { it.nombre == uiState.value.usuario?.perfilCalibracion }
            ?: PerfilCalibracion.EQUILIBRADO
        val umbralAtencion = (perfilUsuario.valoresAtencion.media * 1.10).toInt()
        val umbralMeditacion = (perfilUsuario.valoresMeditacion.media * 1.10).toInt()

        // --- Variables para este tick ---
        var comandoMovimiento: Direction? = null
        var comandoDireccion: Direction? = null
        val ahora = System.currentTimeMillis()

        // --- LÓGICA DE PRIORIDADES Y GRUPOS (sin cambios en la detección) ---
        val esDobleParpadeoFuerte = datos.blinkStrength > UMBRAL_PARPADEO_FUERTE &&
                (ahora - ultimoTiempoParpadeoNormal > INTERVALO_MIN_DOBLE_PARPADEO) &&
                (ahora - ultimoTiempoParpadeoNormal < INTERVALO_MAX_DOBLE_PARPADEO)

        if (esDobleParpadeoFuerte) {
            comandoMovimiento = Direction.CENTER
            ultimoTiempoParpadeoNormal = 0L
        } else {
            if (datos.blinkStrength in UMBRAL_PARPADEO_NORMAL until UMBRAL_PARPADEO_FUERTE) {
                val tiempoDesdeUltimoParpadeo = ahora - ultimoTiempoParpadeoNormal
                if (tiempoDesdeUltimoParpadeo in (INTERVALO_MIN_DOBLE_PARPADEO + 1) until INTERVALO_MAX_DOBLE_PARPADEO) {
                    comandoDireccion = Direction.LEFT
                    ultimoTiempoParpadeoNormal = 0L
                } else if (tiempoDesdeUltimoParpadeo > INTERVALO_MAX_DOBLE_PARPADEO) {
                    comandoDireccion = Direction.RIGHT
                    ultimoTiempoParpadeoNormal = ahora
                }
            }

            // ** GRUPO B: Movimiento (Arriba/Abajo) - Lógica Sostenida MEJORADA **
            val atencionActiva = datos.attention > umbralAtencion
            val meditacionActiva = datos.meditation > umbralMeditacion

            // Creamos la lista de candidatos
            val candidatos = mutableListOf<Direction>()
            if (atencionActiva) candidatos.add(Direction.UP)
            if (meditacionActiva) candidatos.add(Direction.DOWN)

            // Aplicamos la nueva lógica de decisión
            if (ultimoComandoMovimiento in candidatos) {
                // 1. Regla de Inercia: Si podemos mantener el comando anterior, lo hacemos.
                comandoMovimiento = ultimoComandoMovimiento
            } else {
                // 2. Si no hay inercia, decidimos qué nuevo comando activar
                when {
                    // 2a. Prioridad a la Atención: Si la atención está activa (sola o con meditación), elegimos UP.
                    atencionActiva -> comandoMovimiento = Direction.UP
                    // 2b. Si solo la meditación está activa, elegimos DOWN.
                    meditacionActiva -> comandoMovimiento = Direction.DOWN
                    // 2c. Si ninguna está activa, no hay movimiento.
                    else -> comandoMovimiento = null
                }
            }
        }

        // --> CORRECCIÓN 3: Se actualiza la memoria de inercia
        ultimoComandoMovimiento = comandoMovimiento

        // --- ACTUALIZACIÓN DEL ESTADO DE LA UI ---

        // 1. Actualiza el estado de movimiento SOSTENIDO (UP/DOWN/null)
        if (comandoMovimiento != uiState.value.comandoMovimientoActivado) {
            _uiState.update { it.copy(comandoMovimientoActivado = comandoMovimiento) }
        }

        // Si hubo un comando de movimiento este tick (y no es el freno), lo registramos.
        if (comandoMovimiento != null && comandoMovimiento != Direction.CENTER) {
            registrarComando(comandoMovimiento)
        }

        // 2. Si se detectó un comando de PULSO (Dirección o Freno), se activa y se lanza su propio temporizador de reseteo.
        if (comandoDireccion != null) {
            _uiState.update { it.copy(comandoDireccionActivado = comandoDireccion) }
            registrarComando(comandoDireccion)

            viewModelScope.launch {
                delay(1000)
                if (uiState.value.comandoDireccionActivado == comandoDireccion) {
                    _uiState.update { it.copy(comandoDireccionActivado = null) }
                }
            }
        }

        if (comandoMovimiento == Direction.CENTER) {
            registrarComando(Direction.CENTER)

            viewModelScope.launch {
                delay(1000)
                if (uiState.value.comandoMovimientoActivado == Direction.CENTER) {
                    _uiState.update { it.copy(comandoMovimientoActivado = null) }
                }
            }
        }

        // Si estamos en modo juego, procesamos su lógica
        if (uiState.value.estadoJuego != null) {
            procesarLogicaJuego(comandoMovimiento, comandoDireccion)
        }
    }

    // --> COMPLETADO: Función final para guardar los datos
    private fun guardarResultadosDeSesion() {
        val usuario = uiState.value.usuario
        if (usuario == null || atencionRecogida.isEmpty()) {
            Log.w("ViewModel", "Guardado cancelado: no hay usuario o no se recogieron datos.")
            return
        }

        val duracionReal = 60 - uiState.value.tiempoRestanteSeg
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


    //****************** Lógica sobre sesión de juego ****************

    // Define los tipos de órdenes que existen
    enum class TipoComando {
        ATENCION, RELAJACION, GIRO_DERECHA, GIRO_IZQUIERDA, FRENO, ATENCION_Y_GIRO_IZQ, ATENCION_Y_DER, MEDITACION_Y_IZQ, MEDITACION_Y_DER
    }

    // Define cómo se puntúa cada orden
    enum class TipoPuntuacion {
        POR_ACTIVACION, // Ganas puntos cada vez que activas el comando
        POR_TIEMPO      // Ganas puntos por cada segundo que mantienes el comando
    }

    // La estructura de cada nivel/orden del juego
    data class OrdenJuego(
        val nivel: Int,
        val instruccion: String,
        val comandoObjetivo: TipoComando,
        val puntuacionObjetivo: Int,
        val tipoPuntuacion: TipoPuntuacion
    )

    // Una clase para guardar el estado actual del juego en la UI
    data class EstadoJuegoUI(
        val nivel: Int = 1,
        val instruccion: String = "",
        val puntuacionActual: Int = 0,
        val puntuacionObjetivo: Int = 1000
    )

    // --- ESTRUCTURA DEL JUEGO ---
    private val ordenesDelJuego = listOf(
        OrdenJuego(1, "¡Mantén la concentración!", TipoComando.ATENCION, 1000, TipoPuntuacion.POR_TIEMPO),
        OrdenJuego(2, "¡Gira a la derecha 10 veces!", TipoComando.GIRO_DERECHA, 10, TipoPuntuacion.POR_ACTIVACION),
        OrdenJuego(3, "¡Ahora relájate!", TipoComando.RELAJACION, 1000, TipoPuntuacion.POR_TIEMPO),
        OrdenJuego(4, "¡Gira a la izquierda 10 veces!", TipoComando.GIRO_IZQUIERDA, 10, TipoPuntuacion.POR_ACTIVACION),
        OrdenJuego(5, "¡Acelera mientras giras a la izquierda!", TipoComando.ATENCION_Y_GIRO_IZQ, 10, TipoPuntuacion.POR_ACTIVACION),
        OrdenJuego(6, "¡Frena en seco 5 veces!", TipoComando.FRENO, 5, TipoPuntuacion.POR_ACTIVACION)
    )

    // --- ESTADO INTERNO DEL JUEGO ---
    private var nivelActual = 0
    private var puntuacionActualJuego = 0
    private var tiempoSostenidoInicio = 0L


    // --- NUEVOS EVENTOS DESDE LA UI ---
    fun onJugarClick() {
        if (uiState.value.estadoJuego != null) { // Si ya está en modo juego, lo detiene
            detenerModoJuego()
        } else { // Si no, lo inicia
            iniciarModoJuego()
        }
    }

    // --- LÓGICA DE MODO JUEGO ---
    private fun iniciarModoJuego() {
        if (uiState.value.estadoConexion != ConnectionStatus.CONECTADO) {
            _uiState.update { it.copy(mensajeUsuario = "Conecta la diadema para jugar") }
            return
        }
        _uiState.update { it.copy(sesionActiva = true) }

        nivelActual = 0
        puntuacionActualJuego = 0

        iniciarTemporizadorDeSesion()

        siguienteOrden()
    }

    private fun detenerModoJuego() {
        _uiState.update { it.copy(sesionActiva = false, estadoJuego = null) }
    }

    private fun siguienteOrden() {
        if (nivelActual >= ordenesDelJuego.size) {
            _uiState.update { it.copy(estadoJuego = null, mensajeUsuario = "¡Felicidades, has completado el juego!") }
            return
        }
        val ordenActual = ordenesDelJuego[nivelActual]
        puntuacionActualJuego = 0
        _uiState.update {
            it.copy(
                estadoJuego = EstadoJuegoUI(
                    nivel = ordenActual.nivel,
                    instruccion = ordenActual.instruccion,
                    puntuacionActual = 0,
                    puntuacionObjetivo = ordenActual.puntuacionObjetivo
                )
            )
        }
    }


    private fun procesarLogicaJuego(movimiento: Direction?, direccion: Direction?) {
        val ordenActual = ordenesDelJuego.getOrNull(nivelActual) ?: return
        var comandoCorrectoDetectado = false

        // Comprueba si los comandos actuales coinciden con el objetivo
        when (ordenActual.comandoObjetivo) {
            TipoComando.ATENCION -> if (movimiento == Direction.UP) comandoCorrectoDetectado = true
            TipoComando.RELAJACION -> if (movimiento == Direction.DOWN) comandoCorrectoDetectado = true
            TipoComando.GIRO_DERECHA -> if (direccion == Direction.RIGHT) comandoCorrectoDetectado = true
            TipoComando.GIRO_IZQUIERDA -> if (direccion == Direction.LEFT) comandoCorrectoDetectado = true
            TipoComando.FRENO -> if (movimiento == Direction.CENTER) comandoCorrectoDetectado = true
            TipoComando.ATENCION_Y_GIRO_IZQ -> if (movimiento == Direction.UP && direccion == Direction.LEFT) comandoCorrectoDetectado = true
            TipoComando.ATENCION_Y_DER -> if (movimiento == Direction.UP && direccion == Direction.RIGHT) comandoCorrectoDetectado = true
            TipoComando.MEDITACION_Y_IZQ -> if (movimiento == Direction.DOWN && direccion == Direction.LEFT) comandoCorrectoDetectado = true
            TipoComando.MEDITACION_Y_DER -> if (movimiento == Direction.DOWN && direccion == Direction.RIGHT) comandoCorrectoDetectado = true

        }

        // Actualiza la puntuación
        if (comandoCorrectoDetectado) {
            when (ordenActual.tipoPuntuacion) {
                TipoPuntuacion.POR_ACTIVACION -> {
                    // Solo puntuamos una vez por activación, no en cada tick.
                    // Para ello, necesitamos asegurarnos de que el comando no estaba activo en el tick anterior.
                    // Por ahora, asumimos que los comandos de pulso (giros) funcionan bien aquí.
                    puntuacionActualJuego++
                }
                TipoPuntuacion.POR_TIEMPO -> {
                    if (tiempoSostenidoInicio == 0L) {
                        // Si es la primera vez que detectamos el comando, guardamos la hora
                        tiempoSostenidoInicio = System.currentTimeMillis()
                    }
                    // Calculamos cuántos segundos completos han pasado
                    val segundosSostenidos = (System.currentTimeMillis() - tiempoSostenidoInicio) / 1000
                    // La puntuación es 100 por cada segundo completo
                    puntuacionActualJuego = (segundosSostenidos * 100).toInt()
                }
            }
        } else {
            // Si el comando deja de ser correcto, resetea el contador de tiempo
            tiempoSostenidoInicio = 0L
        }

        // Actualiza la UI y comprueba si se pasa de nivel
        _uiState.update { it.copy(estadoJuego = it.estadoJuego?.copy(puntuacionActual = puntuacionActualJuego)) }
        if (puntuacionActualJuego >= ordenActual.puntuacionObjetivo) {
            nivelActual++
            siguienteOrden()
        }
    }

    private fun iniciarTemporizadorDeSesion() {
        // Cancelamos cualquier temporizador anterior para evitar duplicados
        sesionJob?.cancel()

        // Iniciamos la nueva corrutina para el temporizador
        sesionJob = viewModelScope.launch {
            // Ponemos el estado de la sesión en activo y reiniciamos el tiempo
            _uiState.update { it.copy(sesionActiva = true, tiempoRestanteSeg = 60) }

            // Bucle del temporizador
            while (uiState.value.tiempoRestanteSeg > 0) {
                delay(1000)
                _uiState.update { it.copy(tiempoRestanteSeg = it.tiempoRestanteSeg - 1) }
            }

            // Cuando el tiempo llega a 0, detenemos la sesión/juego
            if (uiState.value.estadoJuego != null) {
                detenerModoJuego()
            } else {
                detenerSesion()
            }
        }
    }


}