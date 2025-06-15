package com.example.mindmoving.retrofit.models.sesionesEGG

data class SesionEEGRequest(
    val usuarioId: String,
    val fechaHora: String, // formato ISO-8601
    val duracion: Int,
    val valorMedioAtencion: Float,
    val valorMedioRelajacion: Float,
    val valorMedioPestaneo: Float,
    val comandosEjecutados: String
)
