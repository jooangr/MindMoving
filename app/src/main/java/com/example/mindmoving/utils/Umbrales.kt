package com.example.mindmoving.utils

import com.example.mindmoving.retrofit.models.perfilCalibracion.PerfilCalibracion

// Clase de datos para representar los umbrales mínimos requeridos para atención, meditación y parpadeo.
data class Umbrales(
    val atencion: Int,
    val meditacion: Int,
    val parpadeo: Int = 40
)

/**
 * Función para obtener los umbrales minimo de activación para control de coche
 */
fun obtenerUmbralesParaPerfil(perfil: String?): Umbrales {
    val perfilEncontrado = PerfilCalibracion.values().find { it.nombre == perfil }

    return if (perfilEncontrado != null) {
        Umbrales(
            atencion = perfilEncontrado.valoresAtencion.minimo,
            meditacion = perfilEncontrado.valoresMeditacion.minimo,
            parpadeo = 40 // fijo, porque no tienes info calibrada para esto
        )
    } else {
        // Si no se encuentra el perfil, se usan valores por defecto (Equilibrado)
        Umbrales(atencion = 45, meditacion = 40, parpadeo = 40)
    }
}
