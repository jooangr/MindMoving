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
import com.example.mindmoving.ui.theme.FadeInColumn
import com.example.mindmoving.ui.theme.GradientEndDark
import com.example.mindmoving.ui.theme.GradientEndLight
import com.example.mindmoving.ui.theme.GradientStartDark
import com.example.mindmoving.ui.theme.GradientStartLight
import kotlinx.coroutines.delay


@Composable
fun MainScreenMenu(navController: NavHostController) {

   /* val gradientBrush = Brush.verticalGradient(
      //  colors = listOf(Color(0xFF3F51B5), Color(0xFF2196F3))
        colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
    )*/

    val isDark = isSystemInDarkTheme()
    val backgroundColor = if (isDark) Color(0xFF121212) else Color(0xFFF2F3FC)



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
                    val inactivityLimit = 1 * 60 * 15000 // cambiado a 15 minutos

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


                val perfilTipo = remember {
                    val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
                    prefs.getString("perfil_tipo", null)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (perfilTipo != null) "Perfil actual: $perfilTipo" else "⚠️ No tienes perfil configurado",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Estados para controlar la visibilidad animada
                var showContent by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    delay(200) // Pequeño retraso para que se note la animación
                    showContent = true
                }

                FadeInColumn(
                    modifier = Modifier.fillMaxWidth(),
                    delayMillis = 300
                ) {
                    Button(
                        onClick = { navController.navigate("atencion") },
                       // colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF42A5F5)),
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
                        Text("Comprobar nivel de Atención")
                    }

                        Button(
                            onClick = {
                                val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)

                                //Para cuando tengamos lo de omar descomentar lo de teienperfil ya que si tiene perfil podre acceder a lo del coche
                                //   val tienePerfil = prefs.getString("perfil_tipo", null) != null

                                // if (tienePerfil) {
                                navController.navigate("comandos_diadema")
                                // } else {
                                //     Toast.makeText(context, "⚠️ Necesitas un perfil de calibración para usar esta función", Toast.LENGTH_LONG).show()
                                // }
                            },
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 32.dp, vertical = 8.dp)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),

                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar, // necesitas importarlo
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Controlar Coche (En desarrollo)")
                        }

                }


                Spacer(modifier = Modifier.height(31.dp))

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
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Gráficas recientes",
                                style = MaterialTheme.typography.titleMedium,
                                //color = Color(0xFF3F51B5)
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            SimpleBarChart(
                                title = "Nivel de Atención",
                                values = listOf(20f, 35f, 50f, 70f, 60f),
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            SimpleBarChart(
                                title = "Nivel de Relajación",
                                values = listOf(15f, 40f, 30f, 60f, 45f),
                                color = MaterialTheme.colorScheme.tertiary
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            SimpleBarChart(
                                title = "Nivel de Parpadeo",
                                values = listOf(5f, 10f, 7f, 12f, 8f),
                                color = MaterialTheme.colorScheme.error
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            SimpleLineChartPlano(
                                title = "Nivel de Atención",
                                values = listOf(20f, 35f, 50f, 70f, 60f),
                                lineColor = MaterialTheme.colorScheme.primary
                            )
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
    // Importante: este require tener 'androidx.navigation:navigation-compose' en tu proyecto
    val navController = rememberNavController()
    MainScreenMenu(navController)
}