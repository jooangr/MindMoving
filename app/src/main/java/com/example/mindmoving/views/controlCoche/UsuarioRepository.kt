package com.example.mindmoving.views.controlCoche

import com.example.mindmoving.retrofit.ApiClient
import com.example.mindmoving.retrofit.models.SesionEEGRequest
import com.example.mindmoving.retrofit.models.Usuario
import com.example.mindmoving.utils.SessionManager

class UsuarioRepository{

    /**
     * Obtiene el usuario actual desde el SessionManager.
     * @return El objeto Usuario si existe, o null si no hay sesión.
     */
    fun getUsuarioActual(): Usuario? {
        return SessionManager.usuarioActual
    }

    /**
     * Guarda una sesión de juego en el servidor a través de la API.
     * @return Devuelve true si la llamada a la API fue exitosa (código 2xx), false en caso contrario.
     */
    suspend fun guardarSesionDeJuego(sesionRequest: SesionEEGRequest): Boolean {
        return try {
            val response = ApiClient.getApiService().crearSesionEEG(sesionRequest)
            response.isSuccessful
        } catch (e: Exception) {
            // Loguear el error de red o de otro tipo es una buena práctica
            e.printStackTrace()
            false
        }
    }

}