// utils/SessionManager.kt
package com.example.mindmoving.utils

import android.content.Context
import android.util.Log
import com.example.mindmoving.retrofit.models.user.Usuario
/*
* Esta clase se usa para poder almacenar el usuario logueado en la memoria,
* tambien permite cerrar sesión limpiando datos locales.
*
* */
object SessionManager {
    var usuarioActual: Usuario? = null

    fun logout(context: Context) {
        // Limpia el usuario en memoria
        usuarioActual = null

        // Limpia preferencias
        context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()

        context.getSharedPreferences("mindmoving_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()

        Log.d("LOGOUT", "🧹 SharedPreferences limpiados correctamente")
    }

}
