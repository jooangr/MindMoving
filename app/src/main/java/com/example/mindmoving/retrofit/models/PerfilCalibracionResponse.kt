package com.example.mindmoving.retrofit.models

data class PerfilCalibracionResponse(
    val _id: String,
    val usuarioId: String,
    val tipo: String,
    val valoresAtencion: ValoresEEG,
    val valoresMeditacion: ValoresEEG,
    val alternancia: Alternancia,
    val blinking: Blinking
)

data class ValoresEEG(
    val media: Float,
    val minimo: Float,
    val maximo: Float,
    val variabilidad: Float
)

data class Alternancia(
    val tiempoAtencion: Int,
    val tiempoMeditacion: Int
)

data class Blinking(
    val maxPestaneos: Int,
    val tiempoVentana: Int
)

