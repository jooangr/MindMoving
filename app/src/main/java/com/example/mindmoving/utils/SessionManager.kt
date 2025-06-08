// utils/SessionManager.kt
package com.example.mindmoving.utils

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.mindmoving.retrofit.models.Usuario

object SessionManager {
    var usuarioActual by mutableStateOf<Usuario?>(null)
}
