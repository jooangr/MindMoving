package com.example.mindmoving.views.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import com.example.mindmoving.views.MainLayout

@Composable
fun MainScreenMenu(navController: NavHostController) {
    MainLayout(navController = navController) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF3F51B5), Color(0xFFB0C4DE))
                    )
                )
                .padding(padding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Menú Principal")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { navController.navigate("atencion") }) {
                    Text("Comprobar nivel de Atención")
                }
            }
        }
    }
}