package com.example.mindmoving.retrofit.models

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
    val fuerzaPromedio: Int,
    val sensibilidad: Int // umbral ajustado para disparar comandos
)

// Capacidad de alternar entre estados mentales (clave para perfiles y comandos combinados)
data class AlternanciaData(
    val tiempoCambioAMeditacion: Int, // en segundos
    val tiempoCambioAAtencion: Int    // en segundos
)