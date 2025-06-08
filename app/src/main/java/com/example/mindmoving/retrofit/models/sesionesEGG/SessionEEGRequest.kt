package com.example.mindmoving.retrofit.models.sesionesEGG
data class SesionEEGRequest(
    val userId: String,
    val fecha: String,
    val atencionPromedio: Int,
    val meditacionPromedio: Int,
    val duracion: Int
)
