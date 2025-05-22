package com.example.mindmoving.views.menu

import android.content.Context
import android.widget.Toast
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mindmoving.graficas.SimpleBarChart
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.mindmoving.graficas.SimpleLineChartPlano
import com.example.mindmoving.views.MainLayout

@Composable
fun MainScreenMenu(navController: NavHostController) {

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 🧠 Observer que guarda y valida la sesión al salir y volver
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)

            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    // Salida de la app: guardar hora
                    prefs.edit().putLong("lastPausedTime", System.currentTimeMillis()).apply()
                }
                Lifecycle.Event.ON_START -> {
                    // Entrada a la app: comprobar inactividad
                    val lastPaused = prefs.getLong("lastPausedTime", 0L)
                    val now = System.currentTimeMillis()
                    val inactivityLimit = 1 * 60 * 1000 // 1 minuto

                    if ((now - lastPaused) > inactivityLimit) {
                        prefs.edit().clear().apply()

                        Toast.makeText(context, "Sesión cerrada por inactividad", Toast.LENGTH_LONG).show()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

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

            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState), // 👈 aquí está el scroll
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ){

                Spacer(modifier = Modifier.height(35.dp))

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
                    onClick = { navController.navigate("control_coche") },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(vertical = 8.dp)
                ) {
                    Text("Controlar Coche (En desarrollo)")
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