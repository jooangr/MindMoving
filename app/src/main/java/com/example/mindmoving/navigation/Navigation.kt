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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mindmoving.views.controlCoche.ComandosDiademaScreen
import com.example.mindmoving.views.controlCoche.ComandosDiademaViewModel
import com.example.mindmoving.views.controlCoche.ControlCocheScreen


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavGraph(navController: NavHostController, modifier: Modifier = Modifier) {

    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
    val userId = sharedPrefs.getString("userId", null)

    val startDestination = if (userId != null) "calibracion_menu" else "login"

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
        composable("control_coche") {ControlCocheScreen(navController)}
        composable("comandos_diadema") {
            // Así se instancia un ViewModel sin Hilt
            val viewModel: ComandosDiademaViewModel = viewModel()
            ComandosDiademaScreen(viewModel = viewModel)
        }



    }


    }


