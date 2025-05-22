package com.example.mindmoving.navigation


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.mindmoving.views.login.Login
import com.example.mindmoving.views.menu.MainScreenMenu
import com.example.mindmoving.views.menu.attention.*
import com.example.mindmoving.views.calibracion.*
import com.example.mindmoving.views.login.RegisterScreen
import com.example.mindmoving.views.menu.calibracion.CalibracionAtencionScreen
import com.example.mindmoving.views.menu.calibracion.CalibracionParpadeoScreen
import com.example.mindmoving.views.menu.calibracion.CalibracionRelajacionScreen
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import com.example.mindmoving.views.controlCoche.ControlCocheScreen


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavGraph(navController: NavHostController, modifier: Modifier = Modifier) {

    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
    val userId = sharedPrefs.getString("userId", null)

    // ⚠️ Solo comprobamos si pasó el tiempo, sin borrar sesión aquí
    val lastPaused = sharedPrefs.getLong("lastPausedTime", 0L)
    val now = System.currentTimeMillis()
    val oneMinuteMillis = 1 * 60 * 1000

    val isSessionExpired = userId != null && (now - lastPaused > oneMinuteMillis)

    // decidimos qué pantalla abrir al inicio

    val startDestination = if (userId != null && !isSessionExpired) "menu" else "login"



    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable("login") { Login(navController) }
        composable("menu") { MainScreenMenu(navController) }
        composable("atencion") { AtencionPantalla(navController) }
        composable("calibracion_menu") { PantallaCalibracion(navController) }
        composable("register") {RegisterScreen(navController) }
        composable("calibracion_atencion") { CalibracionAtencionScreen(navController) }
        composable("calibracion_relajacion") { CalibracionRelajacionScreen(navController) }
        composable("control_coche") {ControlCocheScreen(navController)
        }


    }


    }


