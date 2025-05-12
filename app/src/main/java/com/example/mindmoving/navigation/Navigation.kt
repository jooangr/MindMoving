package com.example.mindmoving.navigation


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


@Composable
fun NavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = "login",
        modifier = modifier
    ) {
        composable("login") { Login(navController) }
        composable("menu") { MainScreenMenu(navController) }
        composable("atencion") { AtencionPantalla(navController) }
        composable("calibracion_menu") { PantallaCalibracion(navController) }
        composable("register") {RegisterScreen(navController) }
        composable("calibracion_atencion") { CalibracionAtencionScreen(navController) }
        composable("calibracion_relajacion") { CalibracionRelajacionScreen(navController) }
        composable("calibracion_parpadeo") {CalibracionParpadeoScreen(navController)
        }


    }

        //composable("parpadeo") { ParpadeoPantalla() }
       // composable("meditacion") { MeditacionPantalla() }

}
