package com.example.mindmoving

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.rememberNavController
import com.example.mindmoving.navigation.NavGraph
import com.example.mindmoving.retrofit.models.user.Usuario
import com.example.mindmoving.ui.theme.AppTheme
import com.example.mindmoving.utils.LocalThemeViewModel
import com.example.mindmoving.utils.ProvideThemeViewModel
import com.example.mindmoving.utils.SessionManager
import com.example.mindmoving.utils.ThemeViewModel
import com.google.gson.Gson
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Accede a las preferencias compartidas (almacenamiento local simple)
        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)

        // Intenta restaurar el usuario guardado en memoria desde JSON (si existe)
        val perfilJson = prefs.getString("perfil_completo", null)
        if (!perfilJson.isNullOrBlank()) {
            try {
                val usuario = Gson().fromJson(perfilJson, Usuario::class.java)

                // Guarda el usuario como el actual en SessionManager (singleton global)
                SessionManager.usuarioActual = usuario
                Log.d("INIT", "️Usuario restaurado: ${usuario.id}")
            } catch (e: Exception) {
                Log.e("INIT", "Error al restaurar usuario: ${e.message}")
            }
        }

        enableEdgeToEdge()

        setContent {
            // Usa tu función centralizada
            ProvideThemeViewModel {
                val themeViewModel = LocalThemeViewModel.current

                // Efecto de una sola vez para leer el tema guardado en SharedPrefs
                LaunchedEffect(Unit) {
                    val savedTheme = prefs.getString("user_theme", "light")
                    val isDark = savedTheme == "dark"

                    val themeViewModel = ThemeViewModel()
                    themeViewModel.setTheme(isDark)

                }

                AppTheme(darkTheme = themeViewModel.isDarkTheme.value) {
                    AppNavigator()
                }
            }
        }
    }
}

    @RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigator() {
    val navController = rememberNavController()
    NavGraph(navController = navController)
}

