package com.example.mindmoving.views.menuDrawer.viewsMenuDrawer


import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mindmoving.graficas.SimpleLineChartPlano
import com.example.mindmoving.retrofit.ApiClient
import com.example.mindmoving.retrofit.models.sesionesEGG.SesionEEGResponse

/**
 * Esta función muestra una pantalla con el historial de sesiones EEG del usuario autenticado
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialSesionesScreen(navController: NavHostController) {

    // Gradiente para el fondo de la pantalla
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
    )

    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
    val userId = sharedPrefs.getString("userId", null)

    // Estado para guardar las sesiones obtenidas
    var sesiones by remember { mutableStateOf<List<SesionEEGResponse>>(emptyList()) }

    // Estado para mostrar el indicador de carga
    var isLoading by remember { mutableStateOf(true) }

    // Efecto para cargar las sesiones desde la API al iniciarse el Composable
    LaunchedEffect(Unit) {
        userId?.let {
            try {
                val response = ApiClient.getApiService().getSesiones(it)
                if (response.isSuccessful) {
                    sesiones = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error cargando sesiones", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    // Scaffold con barra superior
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Sesiones") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        // Contenedor principal
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(gradientBrush)
        ) {
            // Mostrar indicador de carga mientras se obtienen datos
            if (isLoading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))

                // Si no hay sesiones disponibles
            } else if (sesiones.isEmpty()) {
                Text("No hay sesiones registradas", Modifier.align(Alignment.Center))

                // Si hay sesiones, mostrar resumen + lista
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    // Gráfica general con valores promedio
                    item {
                        val atencionData = sesiones.map { it.valorMedioAtencion }
                        val relajacionData = sesiones.map { it.valorMedioRelajacion }
                        val pestaneoData = sesiones.map { it.valorMedioPestaneo }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(6.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Resumen general",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.align(Alignment.CenterHorizontally),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                // Línea para cada métrica
                                SimpleLineChartPlano("Atención", atencionData, lineColor = MaterialTheme.colorScheme.primary)
                                SimpleLineChartPlano("Relajación", relajacionData, lineColor = MaterialTheme.colorScheme.tertiary)
                                SimpleLineChartPlano("Pestañeos", pestaneoData, lineColor = MaterialTheme.colorScheme.error)
                            }
                        }
                    }

                    // Lista detallada de cada sesión
                    items(sesiones) { sesion ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Fecha: ${sesion.fechaHora}")
                                Text("Atención promedio: ${sesion.valorMedioAtencion}")
                                Text("Relajación promedio: ${sesion.valorMedioRelajacion}")
                                Text("Pestañeos promedio: ${sesion.valorMedioPestaneo}")
                                Text("Comandos: ${sesion.comandosEjecutados}")
                            }
                        }
                    }
                }
            }
        }
    }
}
