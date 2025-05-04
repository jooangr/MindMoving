package com.example.mindmoving.views.menu

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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mindmoving.navigation.TopNavigationBar

@Composable
fun MenuPantallaPrincipal(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopNavigationBar(title = "MindMoving")
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp), // Padding extra si deseas
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
