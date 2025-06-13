package com.example.mindmoving.views.controlCoche

import android.app.Application
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

class ComandosDiademaViewModel (application: Application) : AndroidViewModel(application){
    // Ya no son privados, y les damos un valor por defecto
    // Instancia las dependencias aquí dentro, pasando el contexto
    private val repository: UsuarioRepository = UsuarioRepository()
    private val neuroSkyManager: NeuroSkyManager = NeuroSkyManager(application.applicationContext)


    // --- GESTIÓN DEL ESTADO ---
    // El StateFlow privado y mutable que gestionará el estado internamente
    private val _uiState = MutableStateFlow(ComandosDiademaState())

    // El StateFlow público e inmutable que la UI observará
    val uiState = _uiState.asStateFlow()

    // --- INICIALIZACIÓN ---
    init {
        // Acciones que se ejecutan en cuanto el ViewModel se crea
        cargarUsuario()
        iniciarConexionDiadema()
    }

    // --- FUNCIONES PÚBLICAS (Eventos desde la UI) ---

    fun onBotonSesionClick() {
        // Lógica para iniciar o detener la sesión
        // (La implementaremos en el Paso 4)
        if (uiState.value.sesionActiva) {
            detenerSesion()
        } else {
            iniciarSesion()
        }
    }


    fun onDpadClick(direction: Direction) {
        // Opcional: para manejar clics manuales en el D-pad si fuera necesario
        println("El usuario ha presionado manualmente: $direction")
    }

    // --- LÓGICA PRIVADA (Acciones internas del ViewModel) ---

    private fun cargarUsuario() {
        // Lógica para obtener el usuario del repositorio y actualizar el state
        // (La implementaremos en el Paso 5)
        // val usuario = repository.getUsuarioActual()
        // _uiState.update { it.copy(usuario = usuario) }
    }

    private fun iniciarConexionDiadema() {
        // 1. Conectar con la diadema
        // Aquí es donde deberías gestionar los permisos. Por ahora, asumimos que están concedidos.
        // En una app real, llamarías a esto DESPUÉS de que la UI confirme los permisos.
        neuroSkyManager.conectar()

        // 2. Escuchar el estado de la conexión
        neuroSkyManager.connectionState
            .onEach { estado ->
                _uiState.update { it.copy(estadoConexion = estado) }
            }
            .launchIn(viewModelScope) // Lanza la corrutina en el scope del ViewModel

        // 3. Escuchar la calidad de la señal
        neuroSkyManager.signalQuality
            .onEach { calidad ->
                _uiState.update { it.copy(calidadSeñal = calidad.level) }
            }
            .launchIn(viewModelScope)

        // 4. Escuchar los datos EEG (Atención, Meditación, Parpadeo)
        neuroSkyManager.eegData
            .onEach { datos ->
                // Actualizamos el estado de la UI con los datos en tiempo real
                _uiState.update {
                    it.copy(
                        atencionActual = datos.attention,
                        meditacionActual = datos.meditation,
                        fuerzaParpadeoActual = datos.blinkStrength
                    )
                }

                // Si la sesión está activa, procesamos los datos para generar comandos
                if (uiState.value.sesionActiva) {
                    procesarEEG(datos)
                }
            }
            .launchIn(viewModelScope)
    }

    // Propiedad para gestionar el trabajo del temporizador
    private var sesionJob: Job? = null

    // Propiedades para acumular datos durante la sesión
    private val atencionRecogida = mutableListOf<Int>()
    private val meditacionRecogida = mutableListOf<Int>()
    private val parpadeosRecogidos = mutableListOf<Int>()
    private var comandosEjecutadosStr = ""

    private fun iniciarSesion() {
        // Si ya hay una sesión activa, no hacemos nada
        if (sesionJob?.isActive == true) return

        // Cancelamos cualquier job anterior por si acaso
        sesionJob?.cancel()

        // Iniciamos una nueva corrutina para el temporizador
        sesionJob = viewModelScope.launch {
            // Actualizamos el estado para reflejar que la sesión ha comenzado
            _uiState.update {
                it.copy(
                    sesionActiva = true,
                    tiempoRestanteSeg = 120 // Reinicia el tiempo
                )
            }

            // Bucle del temporizador
            while (uiState.value.tiempoRestanteSeg > 0) {
                delay(1000) // Espera un segundo
                _uiState.update {
                    it.copy(tiempoRestanteSeg = it.tiempoRestanteSeg - 1)
                }
            }

            // Cuando el tiempo llega a 0, detenemos la sesión
            detenerSesion()
        }
    }

    private fun detenerSesion() {
        sesionJob?.cancel() // Detiene el temporizador si sigue activo
        sesionJob = null
        _uiState.update { it.copy(sesionActiva = false) }

        // TODO en Paso 5: Aquí llamaremos a la función para guardar los resultados en la BBDD.
        Log.d("ViewModel", "Sesión terminada. Listo para guardar datos.")
    }

    // --- LÓGICA DE TRADUCCIÓN EEG A COMANDOS ---
    private fun procesarEEG(datos: EEGData) {
        // Obtenemos el perfil del usuario desde el estado actual
        val perfil = uiState.value.usuario?.perfilCalibracion ?: "EQUILIBRADO"

        // Lógica de ejemplo. ¡Aquí es donde debes implementar tus reglas!
        // Estas reglas dependen completamente de tu `PerfilCalibracion` y de cómo definas los umbrales.
        val umbralAtencion: Int = when (perfil) {
            "CONCENTRADO" -> 70 // Para un perfil concentrado, el umbral es más bajo
            "EQUILIBRADO" -> 80
            "RELAJADO" -> 90 // Para un perfil relajado, se necesita más esfuerzo
            else -> 80
        }

        var comandoGenerado: Direction? = null

        if (datos.attention > umbralAtencion) {
            comandoGenerado = Direction.UP
            Log.i(
                "ViewModel",
                "COMANDO ARRIBA: Atención (${datos.attention}) > Umbral ($umbralAtencion)"
            )
        } else if (datos.meditation > 80) {
            // Podrías añadir lógica para otros comandos
            // comandoGenerado = Direction.DOWN
        }

        // Si se generó un comando, lo actualizamos en el estado
        if (comandoGenerado != null) {
            // Solo actualizamos si el comando es diferente al anterior para evitar spam
            if (uiState.value.comandoActivado != comandoGenerado) {
                _uiState.update { it.copy(comandoActivado = comandoGenerado) }

                // Opcional: Ponemos el comando a null después de un breve instante
                // para que la animación en la UI solo dure un momento.
                viewModelScope.launch {
                    delay(500) // Duración del "resaltado" del botón
                    // Solo lo ponemos a null si no ha sido sobreescrito por otro comando
                    if (uiState.value.comandoActivado == comandoGenerado) {
                        _uiState.update { it.copy(comandoActivado = null) }
                    }
                }
            }
        }

    }
}