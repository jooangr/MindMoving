package com.example.mindmoving.views.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mindmoving.graficas.SimpleBarChart
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.mindmoving.graficas.SimpleLineChartPlano
import androidx.navigation.compose.rememberNavController
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
                Text(
                    text = "Bienvenido a MindMoving",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )

                Text("Menú Principal")

                Text(
                    text = "Selecciona una opción para comenzar",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )


                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { navController.navigate("atencion") },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(vertical = 8.dp)
                ) {
                    Text("Comprobar nivel de Atención")
                }

                Button(
                    onClick = { navController.navigate("calibracion") }, // ← Vista de calibración
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(vertical = 8.dp)
                ) {
                    Text("Opcional: Calibrar tu atención base")
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 🎯 Las gráficas van aquí dentro del Column
                SimpleBarChart(
                    title = "Nivel de Atención",
                    values = listOf(20f, 35f, 50f, 70f, 60f),
                    color = Color(0xFF42A5F5)
                )

                Spacer(modifier = Modifier.height(16.dp))

                SimpleBarChart(
                    title = "Nivel de Relajación",
                    values = listOf(15f, 40f, 30f, 60f, 45f),
                    color = Color(0xFF66BB6A)
                )

                Spacer(modifier = Modifier.height(16.dp))

                SimpleBarChart(
                    title = "Nivel de Parpadeo",
                    values = listOf(5f, 10f, 7f, 12f, 8f),
                    color = Color(0xFFEF5350)
                )

                Spacer(modifier = Modifier.height(16.dp))


                SimpleLineChartPlano(
                    title = "Nivel de Atención",
                    values = listOf(20f, 35f, 50f, 70f, 60f),
                    lineColor = Color(0xFF42A5F5)
                )

            }
        }
    }
}
@Composable
@Preview(showBackground = true, showSystemUi = true)
fun PreviewMenu() {
    // Importante: este require tener 'androidx.navigation:navigation-compose' en tu proyecto
    val navController = rememberNavController()
    MainScreenMenu(navController)
}