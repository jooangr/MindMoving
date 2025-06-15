package com.example.mindmoving.retrofit.models.user

import com.google.gson.annotations.SerializedName

// Datos relevantes del usuario para personalizar el control del RC según su perfil cognitivo

data class Usuario(
    val id: String,
    val username: String,
    val email: String,
    val password: String,

    // Perfil asignado tras la calibración: "Equilibrado", "Predominantemente Atento", etc.
    val perfilCalibracion: String,

    // Registros crudos y métricas procesadas de atención
    val valoresAtencion: ValoresEEG,

    // Registros crudos y métricas procesadas de meditación
    val valoresMeditacion: ValoresEEG,

    // Nivel de control por parpadeo
    val blinking: BlinkingData,

    // Medida de la capacidad del usuario para alternar entre atención y meditación
    val alternancia: AlternanciaData
)

// Valores EEG de una dimensión (atención o meditación)
data class ValoresEEG(
    val media: Int,
    val maximo: Int,
    val minimo: Int,
    val variabilidad: Float // mide cuánto fluctúa en ventanas cortas
)

// Información relacionada con el parpadeo
data class BlinkingData(
    @SerializedName("maxPestaneos")
    val fuerzaPromedio: Int,
    @SerializedName("tiempoVentana")
    val sensibilidad: Int // umbral ajustado para disparar comandos
)

// Capacidad de alternar entre estados mentales (clave para perfiles y comandos combinados)
data class AlternanciaData(
    @SerializedName("tiempoMeditacion")
    val tiempoCambioAMeditacion: Int, // en segundos
    @SerializedName("tiempoAtencion")
    val tiempoCambioAAtencion: Int    // en segundos
)