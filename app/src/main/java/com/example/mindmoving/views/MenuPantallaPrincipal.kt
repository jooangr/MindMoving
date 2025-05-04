package com.example.mindmoving.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController

@Composable
fun MenuPantallaPrincipal(navController: NavHostController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Menú Principal")
        Button(onClick = { navController.navigate("atencion") }) {
            Text("Ir a Atención")
        }
        Button(onClick = { navController.navigate("parpadeo") }) {
            Text("Ir a Parpadeo")
        }
        Button(onClick = { navController.navigate("meditacion") }) {
            Text("Ir a Meditación")
        }
    }
}
