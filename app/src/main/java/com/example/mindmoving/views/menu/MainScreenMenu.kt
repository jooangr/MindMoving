package com.example.mindmoving.views.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mindmoving.navigation.TopNavigationBar

@Composable
fun MainScreenMenu(navController: NavHostController) {
    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF3F51B5), Color(0xFFB0C4DE)) // Ajusta los colores
    )
    Scaffold(
        topBar = {
            TopNavigationBar()
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(brush = gradient)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            //Esta provisional esto, aqui habra algo mejor y bonito pero es solo prueba
            Text("Menú Principal")
            Button(onClick = { navController.navigate("atencion") }) {
                Text("Comprobar nivel de Atencion")
            }
            //TODO cambiar el uso de botones ya que esto no sera asi
            Button(onClick = { navController.navigate("parpadeo") }) {
                Text("Controlar el coche")
            }
            //TODO cambiar el uso de botones ya que esto no sera asi
            Button(onClick = { navController.navigate("meditacion") }) {
                Text("Ajustar Conecentracion")
            }
        }
    }
}
