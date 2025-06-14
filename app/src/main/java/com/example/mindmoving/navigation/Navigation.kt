package com.example.mindmoving.navigation


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.mindmoving.views.login.Login
import com.example.mindmoving.views.menuPrincipal.MainScreenMenu
import com.example.mindmoving.views.menuPrincipal.attention.*
import com.example.mindmoving.views.calibracion.*
import com.example.mindmoving.views.login.RegisterScreen

import android.content.Context
import androidx.compose.ui.platform.LocalContext
import com.example.mindmoving.views.calibracion.guiada.CalibracionCompletaScreen
import com.example.mindmoving.views.calibracion.guiada.CalibracionInicioScreen
import com.example.mindmoving.views.menuDrawer.viewMenuDerecha.EditarPerfilScreen
import com.example.mindmoving.views.controlCoche.ControlCocheScreen
import com.example.mindmoving.views.menuDrawer.viewsMenuDrawer.AjustesScreen
import com.example.mindmoving.views.menuDrawer.viewsMenuDrawer.AyudaScreen
import com.example.mindmoving.views.menuDrawer.viewsMenuDrawer.HistorialSesionesScreen

// Requiere Android 8.0 (API 26) o superior para ejecutar esta función
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavGraph(navController: NavHostController, modifier: Modifier = Modifier) {

    /**Gestión de sesión y lógica de inicio*/

    val context = LocalContext.current

    // Accedemos a las preferencias compartidas (almacenamiento local)
    val sharedPrefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)

    // Obtenemos el ID del usuario guardado tras el login
    val userId = sharedPrefs.getString("userId", null)

    //  Comprobamos si pasó el tiempo de inactividad (1 minuto)
    val lastPaused = sharedPrefs.getLong("lastPausedTime", 0L)
    val now = System.currentTimeMillis()
    val inactivityLimit = 1 * 60 * 1000

    // Evaluamos si la sesión ha caducado comparando tiempos
    val isSessionExpired = now - lastPaused > inactivityLimit

    // Determinamos la pantalla de inicio en función de sesión activa
    val startDestination = if (userId != null && !isSessionExpired) {
        "menu"
    } else {
        "login"
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        /**Declaración de rutas*/

        //Login y resgister
        composable("login") { Login(navController) }
        composable("register") { RegisterScreen(navController) }

        //Menu principal
        composable("menu") { MainScreenMenu(navController) }
        composable("control_coche") { ControlCocheScreen(navController) }

        //Menu Derecha Arriba
        composable("editar_perfil") {EditarPerfilScreen(navController)}

        //Menu lateral
        composable("historial_sesiones") { HistorialSesionesScreen(navController) }
        composable("ayuda_screen") { AyudaScreen(navController) }
        composable("ajustes_screen") { AjustesScreen(navController) }

        //Calibracion Menu
        composable("calibracion_menu") { PantallaCalibracion(navController) }

        //CalibracionGuiada
        composable("calibracion_inicio") {CalibracionInicioScreen(navController = navController)}
        composable("fase_calibracion") {CalibracionCompletaScreen(navController = navController)}
        composable("perfil_calibracion") { PerfilCalibracionScreen(navController = navController)
        }



        //quitar luego esta de pruebas
        composable("atencion") { AtencionPantalla(navController) }


    }
}

