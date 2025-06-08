package com.example.mindmoving.retrofit.models

data class PerfilUsuario(
    val id: String,
    val username: String,
    val email: String,
    val perfilCalibracion: String,
    val valoresAtencion: ValoresEEG,
    val valoresMeditacion: ValoresEEG,
    val blinking: BlinkingData,
    val alternancia: AlternanciaData
)
