package com.example.mindmoving.views.menuPrincipal

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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.mindmoving.graficas.SimpleLineChartPlano
import androidx.navigation.compose.rememberNavController
import com.example.mindmoving.views.menuDrawer.MainLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.mindmoving.graficas.MetricCard
import com.example.mindmoving.retrofit.ApiClient
import com.example.mindmoving.retrofit.models.sesionesEGG.SesionEEGResponse
import com.example.mindmoving.ui.theme.FadeInColumn
import kotlinx.coroutines.delay

/**
 * Es la pantalla principal tras iniciar sesión.
 *
 * Presenta un menú visual y accesible que permite al usuario navegar hacia las opciones de calibración,
 * control del coche mediante la diadema EEG y consultar un resumen reciente de
 * métricas EEG (atención, relajación y pestañeo). También incluye lógica para cerrar sesión automáticamente
 * si hay inactividad prolongada (más de 15 minutos) y mantiene actualizada la información del perfil desde
 * SharedPreferences.

 */@Composable
fun MainScreenMenu(navController: NavHostController) {

    val isDark = isSystemInDarkTheme()
    val backgroundColor = if (isDark) Color(0xFF121212) else Color(0xFFF2F3FC)

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Observador del ciclo de vida: guarda la hora de salida y cierra sesión si hubo inactividad prolongada
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)

            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    // Al salir de la app se guarda la hora
                    prefs.edit().putLong("lastPausedTime", System.currentTimeMillis()).apply()
                }
                Lifecycle.Event.ON_START -> {
                    // Al volver a entrar se comprueba el tiempo de inactividad
                    val lastPaused = prefs.getLong("lastPausedTime", 0L)
                    val now = System.currentTimeMillis()
                    val inactivityLimit = 1 * 60 * 15000 // 15 minutos

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

    // Contenedor de la vista con MainLayout y barra superior
    MainLayout(navController = navController) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(padding)
        ) {

            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(20.dp))

                // Tarjeta de bienvenida
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(horizontal = 8.dp),
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)

                ) {
                    Column(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Bienvenido a MindMoving",
                            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 26.sp),
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            "Menú Principal",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )

                        Text(
                            "Selecciona una opción para comenzar",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Estado para guardar y mostrar el perfil actual
                val perfilTipoState = remember { mutableStateOf<String?>(null) }

                // Carga inicial del perfil desde SharedPreferences
                LaunchedEffect(Unit) {
                    val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
                    perfilTipoState.value = prefs.getString("perfil_tipo", null)
                }

                // Observador de ciclo de vida para actualizar el perfil al volver del background
                val lifecycleOwner = LocalLifecycleOwner.current

                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
                            perfilTipoState.value = prefs.getString("perfil_tipo", null)
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Texto del perfil configurado o advertencia si no hay ninguno
                Text(
                    text = if (perfilTipoState.value != null)
                        "Perfil actual: ${perfilTipoState.value}"
                    else
                        "⚠️ No tienes perfil configurado",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Controla si mostrar las animaciones y los botones
                var showContent by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    delay(200)
                    showContent = true
                }

                // Botones principales con entrada animada
                FadeInColumn(
                    modifier = Modifier.fillMaxWidth(),
                    delayMillis = 300
                ) {
                    // Botón para calibración mental
                    Button(
                        onClick = { navController.navigate("calibracion_menu") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp, vertical = 8.dp)
                            .height(50.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Opciones de Calibracion")
                    }

                    // Botón para comandos con el coche RC
                    Button(
                        onClick = {
                            val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
                            navController.navigate("comandos_diadema")
                        },
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp, vertical = 8.dp)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Comando Coche")
                    }
                }

                Button(onClick = {
                    navController.navigate("sesion_diadema")
                }) {
                    Text("Iniciar Sesión Diadema")
                }


                Spacer(modifier = Modifier.height(31.dp))

                // Resumen animado con las últimas sesiones registradas
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .padding(8.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        val sesiones = remember { mutableStateListOf<SesionEEGResponse>() }

                        // Cargar las últimas 5 sesiones del usuario
                        LaunchedEffect(Unit) {
                            val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
                            val userId = prefs.getString("userId", null)

                            if (userId != null) {
                                try {
                                    val response = ApiClient.getApiService().getSesiones(userId)
                                    if (response.isSuccessful) {
                                        val todas = response.body() ?: emptyList()
                                        sesiones.clear()
                                        sesiones.addAll(todas.takeLast(5))
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error cargando sesiones", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }

                        Column {
                            Text(
                                "Resumen de métricas recientes",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Comparación de las dos últimas sesiones
                            if (sesiones.size >= 2) {
                                val ultimas = sesiones.takeLast(2)
                                val atencion = ultimas.map { it.valorMedioAtencion }
                                val relajacion = ultimas.map { it.valorMedioRelajacion }
                                val pestaneo = ultimas.map { it.valorMedioPestaneo }

                                MetricCard(
                                    title = "Atención",
                                    icon = "🧠",
                                    value = atencion[1],
                                    change = atencion[1] - atencion[0],
                                    color = MaterialTheme.colorScheme.primary
                                )

                                MetricCard(
                                    title = "Relajación",
                                    icon = "🧘",
                                    value = relajacion[1],
                                    change = relajacion[1] - relajacion[0],
                                    color = MaterialTheme.colorScheme.tertiary
                                )

                                MetricCard(
                                    title = "Pestañeo",
                                    icon = "👁️",
                                    value = pestaneo[1],
                                    change = pestaneo[1] - pestaneo[0],
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else {
                                Text(
                                    "No hay suficientes sesiones registradas para comparar.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
fun PreviewMenu() {
    val navController = rememberNavController()
    MainScreenMenu(navController)
}
