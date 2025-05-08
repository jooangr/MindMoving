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
        composable("calibracion") { PantallaCalibracion(navController) }
        composable("register") {RegisterScreen(navController)
        }

    }

        //composable("parpadeo") { ParpadeoPantalla() }
       // composable("meditacion") { MeditacionPantalla() }

}
