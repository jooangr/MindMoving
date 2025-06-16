package com.example.mindmoving.views.controlCoche

import android.util.Log
import com.example.mindmoving.retrofit.ApiClient
import com.example.mindmoving.retrofit.models.sesionesEGG.SesionEEGRequest
import com.example.mindmoving.retrofit.models.user.Usuario
import com.example.mindmoving.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UsuarioRepository{

    /**
     * Obtiene el usuario actual y se asegura de que sus datos de calibración estén actualizados.
     * Primero intenta obtener el usuario de SessionManager.
     * Si el usuario no tiene datos de calibración, intenta obtenerlos desde la API.
     * @return El objeto Usuario con datos de calibración si existen, o null.
     */
    suspend fun getUsuarioConPerfil(): Usuario? {
        // Obtenemos el usuario de la sesión actual
        var usuario = SessionManager.usuarioActual
        if (usuario == null) return null // Si no hay usuario en sesión, no hay nada que hacer

        // --- LÓGICA DE VERIFICACIÓN (VERSIÓN CORREGIDA) ---
        if (usuario.perfilCalibracion.isNullOrBlank() || usuario.perfilCalibracion.equals("ninguno", ignoreCase = true)) {
            Log.d("Repository", "Perfil no encontrado en SessionManager. Consultando API...")
            try {
                // 1. CREA UNA COPIA LOCAL INMUTABLE
                val usuarioActual = usuario

                // 2. USA ESA COPIA INMUTABLE DENTRO DE LA CORRUTINA
                val response = withContext(Dispatchers.IO) {
                    // Ahora usamos usuarioActual.id, que es garantizado no nulo
                    ApiClient.getApiService().getPerfil(usuarioActual.id)
                }

                if (response.isSuccessful && response.body() != null) {
                    val perfilResponse = response.body()!!
                    Log.d("Repository", "✅ Perfil obtenido de la API: ${perfilResponse.tipo}")

                    // Actualizamos la variable 'usuario' original con los nuevos datos
                    usuario = usuarioActual.copy( // Usamos la copia para estar seguros
                        perfilCalibracion = perfilResponse.tipo,
                        valoresAtencion = perfilResponse.valoresAtencion,
                        valoresMeditacion = perfilResponse.valoresMeditacion,
                        blinking = perfilResponse.blinking,
                        alternancia = perfilResponse.alternancia
                    )
                    SessionManager.usuarioActual = usuario
                } else {
                    Log.w("Repository", "⚠️ No se encontró perfil en la API o hubo un error: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("Repository", "❌ Error de red al obtener el perfil", e)
            }
        } else {
            Log.d("Repository", "Perfil ya presente en SessionManager: ${usuario.perfilCalibracion}")
        }

        return usuario
    }

    /**
     * Guarda una sesión de juego en el servidor a través de la API.
     * @return Devuelve true si la llamada a la API fue exitosa (código 2xx), false en caso contrario.
     */
    suspend fun guardarSesionDeJuego(sesionRequest: SesionEEGRequest): Boolean {
        return try {
            val response = withContext(Dispatchers.IO) {
                ApiClient.getApiService().crearSesionEEG(sesionRequest)
            }
            // Como crearSesionEEG devuelve Response<Void>, solo nos importa si fue exitoso
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("Repository", "❌ Error de red al guardar la sesión", e)
            false
        }
    }

}