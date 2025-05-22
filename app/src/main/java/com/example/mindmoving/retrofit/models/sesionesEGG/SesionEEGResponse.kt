package com.example.mindmoving.retrofit.models.sesionesEGG


data class SesionEEGResponse(
    val _id: String,
    val usuarioId: String,
    val fechaHora: String,
    val duracion: Int,
    val valorMedioAtencion: Float,
    val valorMedioRelajacion: Float,
    val valorMedioPestaneo: Float,
    val comandosEjecutados: String
)