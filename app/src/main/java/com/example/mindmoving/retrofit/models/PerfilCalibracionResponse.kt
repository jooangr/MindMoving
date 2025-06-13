package com.example.mindmoving.retrofit.models

data class PerfilCalibracionResponse(
    val _id: String,
    val usuarioId: String,
    val tipo: String,
    val valoresAtencion: ValoresEEG,
    val valoresMeditacion: ValoresEEG,
    val alternancia: AlternanciaData,
    val blinking: BlinkingData
)
