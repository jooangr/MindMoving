package com.example.mindmoving.views.controlCoche

import com.example.mindmoving.retrofit.models.user.Usuario

// Estado para representar la conexión con la diadema
enum class ConnectionStatus {
    CONECTADO,
    CONECTANDO,
    DESCONECTADO,
    ERROR
}

data class ComandosDiademaState(
    // --- Estado del Usuario ---
    val usuario: Usuario? = null,

    // --- Estado de la Diadema y Conexión ---
    val estadoConexion: ConnectionStatus = ConnectionStatus.DESCONECTADO,
    val calidadSeñal: Int = 200, // Usamos el valor numérico (0-200), la UI lo traducirá a texto

    // --- Estado de la Sesión de Juego ---
    val sesionActiva: Boolean = false,
    val tiempoRestanteSeg: Int = 60, // 1 minuto

    // --- Datos EEG en Tiempo Real ---
    val atencionActual: Int = 0,
    val meditacionActual: Int = 0,
    val fuerzaParpadeoActual: Int = 0,

    // --- Interacción y Feedback para la UI ---
    // Usaremos esto para que la UI sepa qué botón del D-pad resaltar
    val comandoMovimientoActivado: Direction? = null,
    val comandoDireccionActivado: Direction? = null,

    // --- Mensajes para el usuario ---
    val mensajeUsuario: String? = null, // Para mostrar Snackbars o Toasts

    val perfilVerificado: Boolean = false,
    val necesitaCalibracion: Boolean = false,

    // Controla si se debe mostrar el diálogo de guardado de sesion
    val mostrarDialogoGuardar: Boolean = false,

    //Propiedad para los umbrales
    val umbrales: UmbralesUI = UmbralesUI(),

    //Guardará el estado del juego. Será `null` si estamos en "Modo Libre".
    val estadoJuego: ComandosDiademaViewModel.EstadoJuegoUI? = null
)

// Data class para contener los umbrales que se mostrarán en la UI
data class UmbralesUI(
    val atencion: Int = 0,
    val meditacion: Int = 0,
    val parpadeoDoble: String = "Doble Parpadeo",
    val parpadeoSimple: String = "Parpadeo Simple",
    val freno: String = "Doble Parpadeo Fuerte"
)
