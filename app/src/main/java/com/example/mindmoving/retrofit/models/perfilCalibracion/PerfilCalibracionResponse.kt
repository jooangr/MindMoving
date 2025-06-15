package com.example.mindmoving.retrofit.models.perfilCalibracion

import com.example.mindmoving.retrofit.models.user.AlternanciaData
import com.example.mindmoving.retrofit.models.user.BlinkingData
import com.example.mindmoving.retrofit.models.user.ValoresEEG

data class PerfilCalibracionResponse(
    val _id: String,
    val usuarioId: String,
    val tipo: String,
    val valoresAtencion: ValoresEEG,   // <- nullable
    val valoresMeditacion: ValoresEEG, // <- nullable
    val alternancia: AlternanciaData,  // <- nullable
    val blinking: BlinkingData         // <- nullable
)

