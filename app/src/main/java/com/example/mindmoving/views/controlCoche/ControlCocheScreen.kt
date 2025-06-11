package com.example.mindmoving.views.controlCoche

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun ControlCocheScreen(navController: NavHostController) {

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(" Pantalla de Control del Coche", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(20.dp))
        Text("Aquí se implementará el control de coche con la diadema EEG.")

        Spacer(modifier = Modifier.height(40.dp))
        Button(onClick = { navController.popBackStack() }) {
            Text("Volver al menú")
        }
    }
}
