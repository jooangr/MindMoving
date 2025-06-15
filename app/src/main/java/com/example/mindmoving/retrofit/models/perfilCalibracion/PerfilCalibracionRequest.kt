package com.example.mindmoving.retrofit.models.perfilCalibracion

import com.example.mindmoving.retrofit.models.user.AlternanciaData
import com.example.mindmoving.retrofit.models.user.BlinkingData
import com.example.mindmoving.retrofit.models.user.ValoresEEG

data class PerfilCalibracionRequest(
    val usuarioId: String,
    val tipo: String,
    val valoresAtencion: ValoresEEG,
    val valoresMeditacion: ValoresEEG,
    val alternancia: AlternanciaData,
    val blinking: BlinkingData
)
