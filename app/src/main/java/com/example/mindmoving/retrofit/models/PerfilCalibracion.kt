package com.example.mindmoving.retrofit.models

enum class PerfilCalibracion (
    val nombre: String,
    val valoresAtencion: ValoresEEG,
    val valoresMeditacion: ValoresEEG,
    val alternancia: AlternanciaData
) {
    EQUILIBRADO(
        nombre = "Equilibrado",
        valoresAtencion = ValoresEEG(60, 85, 45, 10f),
        valoresMeditacion = ValoresEEG(60, 80, 40, 10f),
        alternancia = AlternanciaData(7, 8)
    ),
    ATENTO(
        nombre = "Predominantemente Atento",
        valoresAtencion = ValoresEEG(75, 95, 55, 5f),
        valoresMeditacion = ValoresEEG(30, 50, 10, 15f),
        alternancia = AlternanciaData(15, 3)
    ),
    MEDITATIVO(
        nombre = "Predominantemente Meditativo",
        valoresAtencion = ValoresEEG(40, 60, 20, 15f),
        valoresMeditacion = ValoresEEG(75, 95, 55, 5f),
        alternancia = AlternanciaData(5, 20)
    )
}