package com.example.mindmoving.views.controlCoche

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
//
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*

import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController


@Composable
fun ComandosDiademaScreen(
    viewModel: ComandosDiademaViewModel = viewModel(),
    navController: NavController
) {

    // 2. Recoge el estado
    val uiState by viewModel.uiState.collectAsState()

    // --> AÑADIDO 1: LÓGICA DEL DIÁLOGO DE AVISO
    // Se mostrará solo cuando el perfil se haya verificado y se determine que se necesita calibración.
    if (uiState.perfilVerificado && uiState.necesitaCalibracion) {
        AlertDialog(
            onDismissRequest = { /* No hacemos nada para forzar al usuario a tomar una decisión */ },
            title = { Text(text = "Perfil No Encontrado") },
            text = { Text(text = "Para una experiencia personalizada, te recomendamos realizar la calibración primero.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Navega a tu pantalla de calibración.
                        // Cambia "calibracion_route" por la ruta real que hayas definido en tu NavHost.
                        navController.navigate("calibracion_route")
                    }
                ) {
                    Text("Ir a Calibrar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        // Notifica al ViewModel que el usuario ha decidido ignorar el aviso.
                        viewModel.onIgnorarAvisoCalibracion()
                    }
                ) {
                    Text("Continuar sin calibrar")
                }
            }
        )
    }

    // LÓGICA DEL SNACKBAR PARA MENSAJES
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.mensajeUsuario) {
        val mensaje = uiState.mensajeUsuario

        if (mensaje  != null) {
            snackbarHostState.showSnackbar(message = mensaje)
            // El ViewModel se encarga de limpiar el mensaje después de un tiempo,
        }
    }


    // --> CAMBIO 3: MODIFICACIÓN DEL SCAFFOLD
    // Añadimos el `snackbarHost` al Scaffold para que sepa dónde mostrar los Snackbars.
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFE0E0E0), Color(0xFFF5F5F5))
                    )
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // El resto de tu UI se queda igual. Ya está correctamente conectada
            // al uiState, por lo que reaccionará a los cambios sin necesidad
            // de modificar nada más aquí.
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                CardDatosUsuario(
                    nombreUsuario = uiState.usuario?.username ?: "Cargando...",
                    perfilCalibracion = uiState.usuario?.perfilCalibracion ?: "N/A",
                    // TODO: Para el número de sesiones, necesitarías una llamada a la API
                    // que las cuente, o tener ese dato en el objeto Usuario.
                    // Por ahora lo dejamos en 0.
                    nSesiones = 0
                )
                Spacer(modifier = Modifier.height(16.dp))
                CardEstadoReal(
                    estadoConexion = uiState.estadoConexion.name,
                    calidadSeñal = getSignalQualityDescription(uiState.calidadSeñal),
                    atencion = uiState.atencionActual,
                    meditacion = uiState.meditacionActual,
                    fuerzaParpadeo = uiState.fuerzaParpadeoActual
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { viewModel.onBotonSesionClick() },
                ) {
                    Text(if (uiState.sesionActiva) "Detener Sesión" else "Comenzar Sesión")
                }
            }

            ModernDpad(
                modifier = Modifier.padding(bottom = 32.dp),
                onDirectionClick = { direction ->
                    viewModel.onDpadClick(direction)
                }
            )
        }
    }
}

@Composable
fun CardDatosUsuario(nombreUsuario: String,
                     perfilCalibracion: String,
                     nSesiones: Int //TODO numero de sesiones realizadas
){
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Usuario", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Nombre: $nombreUsuario")
            Text("Perfil: $perfilCalibracion")
        }
    }
}

@Composable
fun CardEstadoReal(estadoConexion: String,
                   calidadSeñal: String,
                   atencion: Int,
                   meditacion: Int,
                   fuerzaParpadeo: Int){
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Estado en Tiempo Real", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Conexión: $estadoConexion")
            Text("Calidad de Señal: $calidadSeñal")
            Spacer(modifier = Modifier.height(8.dp))
            Text("Atención: $atencion")
            Text("Meditación: $meditacion")
            Text("Parpadeo: $fuerzaParpadeo")
        }
    }

}

// Función helper que puedes poner en el mismo archivo o en uno de utilidades
private fun getSignalQualityDescription(signal: Int): String {
    return when {
        signal == 0 -> "Excelente"
        signal in 1..50 -> "Buena"
        signal in 51..100 -> "Aceptable"
        signal in 101..150 -> "Débil"
        signal in 151..199 -> "Muy débil"
        signal >= 200 -> "Sin contacto/No colocada"
        else -> "Desconocida"
    }
}

//********************** Lógica Dpad **********************

@Composable
fun ModernDpad(
    modifier: Modifier = Modifier,
    onDirectionClick: (Direction) -> Unit
) {
    Box(
        modifier = modifier.size(220.dp), // Tamaño total del D-Pad
        contentAlignment = Alignment.Center
    ) {
        // --- Botones Direccionales (sin cambios aquí) ---
        DirectionalButton(
            onClick = { onDirectionClick(Direction.UP) },
            icon = Icons.Default.KeyboardArrowUp,
            contentDescription = "Arriba",
            modifier = Modifier.align(Alignment.TopCenter)
        )
        DirectionalButton(
            onClick = { onDirectionClick(Direction.LEFT) },
            icon = Icons.Default.KeyboardArrowUp,
            contentDescription = "Izquierda",
            modifier = Modifier.align(Alignment.CenterStart).rotate(-90f)
        )
        DirectionalButton(
            onClick = { onDirectionClick(Direction.RIGHT) },
            icon = Icons.Default.KeyboardArrowUp,
            contentDescription = "Derecha",
            modifier = Modifier.align(Alignment.CenterEnd).rotate(90f)
        )
        DirectionalButton(
            onClick = { onDirectionClick(Direction.DOWN) },
            icon = Icons.Default.KeyboardArrowUp,
            contentDescription = "Abajo",
            modifier = Modifier.align(Alignment.BottomCenter).rotate(180f)
        )

        // --- Botón Central (Opcional, actualizado a M3) ---
        Box(
            modifier = Modifier
                .size(70.dp)
                .shadow(6.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceBright,
                            MaterialTheme.colorScheme.surfaceContainer
                        ),
                        radius = 40f
                    )
                )
                .clickable { onDirectionClick(Direction.CENTER) },
            contentAlignment = Alignment.Center
        ) {
            // Puedes poner un icono o texto aquí si quieres
        }
    }
}

@Composable
fun DirectionalButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    buttonSize: Dp = 100.dp,
    iconSize: Dp = 32.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Animaciones para el efecto de presionado
    val elevation by animateDpAsState(if (isPressed) 2.dp else 8.dp, label = "elevation")
    val scale by animateFloatAsState(if (isPressed) 0.95f else 1f, label = "scale")

    // Colores del tema de Material 3 para adaptarse a temas claro/oscuro
    val containerColor by animateColorAsState(
        if (isPressed) MaterialTheme.colorScheme.surfaceContainerHigh
        else MaterialTheme.colorScheme.surface,
        label = "containerColor"
    )
    val iconColor by animateColorAsState(
        if (isPressed) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "iconColor"
    )

    Box(
        modifier = modifier
            .size(buttonSize)
            .scale(scale)
            .shadow(
                elevation = elevation,
                shape = ArcShape(90f),
                clip = false
            )
            .clip(ArcShape(90f))
            .background(containerColor)
            .clickable(interactionSource = interactionSource, indication = null) {
                onClick()
            }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier
                .align(Alignment.Center)
                .size(iconSize)
                .offset(y = -buttonSize / 4.5f)
        )
    }
}

// Enum para manejar las direcciones fácilmente
enum class Direction {
    UP, DOWN, LEFT, RIGHT, CENTER
}

// Forma de Arco para los botones direccionales
class ArcShape(private val sweepAngle: Float) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            // El centro del arco estará en la parte inferior central del `size`
            val centerX = size.width / 2f
            val centerY = size.height
            // El radio será la altura del componente
            val radius = size.height
            // Rectángulo que define el óvalo del que se cortará el arco
            val rect = Rect(
                left = centerX - radius,
                top = centerY - radius,
                right = centerX + radius,
                bottom = centerY + radius
            )
            // Ángulo de inicio (270° es arriba, pero lo ajustamos para que empiece desde un lado)
            val startAngle = 270f - sweepAngle / 2f
            // Mover al punto central (la "punta" del pastel)
            moveTo(centerX, centerY)
            // Dibujar el arco
            arcTo(rect, startAngle, sweepAngle, false)
            // Cerrar el camino para formar el "trozo de pastel"
            close()
        }
        return Outline.Generic(path)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ComandosDireccionPreview() {
    // Puedes usar un NavHostController falso si no necesitas navegación
    //ModernDpadScreen(navController = rememberNavController())
}