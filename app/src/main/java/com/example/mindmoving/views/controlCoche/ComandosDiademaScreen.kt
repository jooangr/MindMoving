package com.example.mindmoving.views.controlCoche

import androidx.compose.animation.AnimatedVisibility
import android.content.Context
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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.mindmoving.utils.Umbrales
import com.example.mindmoving.utils.obtenerUmbralesParaPerfil
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBarDefaults


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComandosDiademaScreen(
    viewModel: ComandosDiademaViewModel = viewModel(),
    navController: NavController
) {

    //Recoge el estado
    val uiState by viewModel.uiState.collectAsState()
    val umbrales = obtenerUmbralesParaPerfil(uiState.usuario?.perfilCalibracion)
    val context = LocalContext.current

    //Logica para actualizar el perfil se lo cambia en la sesión
    val perfilTipoState = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        perfilTipoState.value = prefs.getString("perfil_tipo", null)
    }

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

    // LÓGICA DEL DIÁLOGO DE AVISO
    // Se mostrará solo cuando el perfil se haya verificado y se determine que se necesita calibración.
    if (uiState.perfilVerificado && uiState.necesitaCalibracion) {
        AlertDialog(
            onDismissRequest = { /* No hacemos nada para forzar al usuario a tomar una decisión */ },
            title = { Text(text = "Usuario sin Perfil de Calibración Asignado") },
            text = { Text(text = "Para una experiencia personalizada, te recomendamos realizar la calibración primero.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        navController.navigate("calibracion_menu")
                    }
                ) {
                    Text("Ir a Calibración")
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

    // Se mostrará solo cuando el usuario haya detenido la sesion o la sesion haya terminado, para guardar los datos de la sesion
    if (uiState.mostrarDialogoGuardar) {
        AlertDialog(
            onDismissRequest = {
                // Si el usuario toca fuera del diálogo, lo consideramos un "cancelar".
                viewModel.onCancelarGuardarSesion()
            },
            title = { Text(text = "Fin de la Sesión") },
            text = { Text(text = "¿Deseas guardar los resultados de esta sesión?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        // El usuario pulsa "Guardar"
                        viewModel.onConfirmarGuardarSesion()
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        // El usuario pulsa "Descartar"
                        viewModel.onCancelarGuardarSesion()
                    }
                ) {
                    Text("Descartar")
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


    // Añadimos el `snackbarHost` al Scaffold para que sepa dónde mostrar los Snackbars.
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Control EEG") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent // Para que se integre con el fondo
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainerLowest),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Column(
                modifier = Modifier
                    .weight(1f) // Ocupa todo el espacio disponible, empujando los controles hacia abajo
                    .verticalScroll(rememberScrollState()) // Hacemos que esta área sea deslizable
            ) {
                // Ponemos el contenido de las cards dentro de otra Column con padding
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp) // Espacio uniforme
                ){

                    CardDatosUsuario(
                        nombreUsuario = uiState.usuario?.username ?: "Cargando...",
                        perfilCalibracion = perfilTipoState.value ?: "No asignado",
                        // TODO: Para el número de sesiones, necesitarías una llamada a la API
                        // que las cuente, o tener ese dato en el objeto Usuario. Por ahora lo dejamos en 0.
                        nSesiones = 0
                    )

                    CardEstadoReal(
                        estadoConexion = uiState.estadoConexion.name,
                        calidadSeñal = uiState.calidadSeñal,
                        atencion = uiState.atencionActual,
                        meditacion = uiState.meditacionActual,
                        fuerzaParpadeo = uiState.fuerzaParpadeoActual,
                        umbrales = umbrales
                    )
                    CardInstrucciones(umbrales = uiState.umbrales)
                }

            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(
                    visible = uiState.sesionActiva,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    val minutos = uiState.tiempoRestanteSeg / 60
                    val segundos = uiState.tiempoRestanteSeg % 60

                    Text(
                        text = String.format("%02d:%02d", minutos, segundos),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                // --> CAMBIO DE DISEÑO: Botones con un estilo más limpio y consistente
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(48.dp), // Altura fija para el área del botón
                    contentAlignment = Alignment.Center
                ) {
                    //Lógica condicional para los botones
                    when (uiState.estadoConexion) {
                        ConnectionStatus.CONECTADO -> {
                            // Si está CONECTADO, mostramos el botón de la sesión
                            Button(
                                onClick = { viewModel.onBotonSesionClick() },
                                // Se deshabilita si la sesión ya está activa para evitar doble clic
                                enabled = true
                            ) {
                                Text(if (uiState.sesionActiva) "Detener Sesión" else "Comenzar Sesión")
                            }
                        }

                        ConnectionStatus.CONECTANDO -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.size(8.dp))
                                Text(
                                    "Conectando...",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        else -> { // DESCONECTADO o ERROR
                            // Si está DESCONECTADO, mostramos el botón para conectar
                            Button(onClick = { viewModel.onConectarDiademaClick() }) {
                                Text("Conectar Diadema")
                            }
                        }
                    }

                }

                Spacer(Modifier. height(20.dp))

                ModernDpad(
                    modifier = Modifier.padding(bottom = 32.dp),
                    onDirectionClick = { direction ->
                        viewModel.onDpadClick(direction)
                    },
                    // --> AÑADIDO: Pasamos el comando activo desde el state
                    comandoMovimientoActivado = uiState.comandoMovimientoActivado,
                    comandoDireccionActivado = uiState.comandoDireccionActivado
                )
            }
        }
    }
}

@Composable
fun CardDatosUsuario(nombreUsuario: String,
                     perfilCalibracion: String,
                     nSesiones: Int //TODO numero de sesiones realizadas
){
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface // Fondo limpio
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Título de la Card con icono
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Usuario",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text("Usuario", style = MaterialTheme.typography.titleLarge)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text("Nombre: $nombreUsuario")
            //TODO Si no tiene perfil de calibración mostrar un mensaje, que no se quede en blanco
            if(perfilCalibracion.isNotEmpty()){
                Text("Perfil: $perfilCalibracion")
            }else Text("Perfil: Sin perfil asignado. Usando perfil predeterminado." )
        }
    }
}

@Composable
fun CardEstadoReal(
    estadoConexion: String,
    calidadSeñal: Int,
    atencion: Int,
    meditacion: Int,
    fuerzaParpadeo: Int,
    umbrales: Umbrales
)

{

    val connectionColor = when (estadoConexion) {
        ConnectionStatus.CONECTADO.name -> Color(0xFF4CAF50) // Verde
        ConnectionStatus.CONECTANDO.name -> Color(0xFFFFC107) // Ambar
        else -> MaterialTheme.colorScheme.error // Rojo
    }

    // Calculamos el color de la señal aquí
    val signalColor = getSignalQualityColor(calidadSeñal)

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )  {
        Column(
            modifier = Modifier.padding(10.dp)
        ){

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.SignalCellularAlt,
                    contentDescription = "Estado",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text("Estado en Tiempo Real", style = MaterialTheme.typography.titleLarge)
            }

            Spacer(modifier = Modifier.height(6.dp))
            InfoRow(label = "Conexión", value = estadoConexion.toString(), valueColor = connectionColor)
            Text("Calidad de Señal: $calidadSeñal")
            Spacer(modifier = Modifier.height(6.dp))
            Text("Atención: $atencion / ${umbrales.atencion}")
            Text("Meditación: $meditacion / ${umbrales.meditacion}")
            Text("Parpadeo: $fuerzaParpadeo / ${umbrales.parpadeo}")
        }
    }

}

@Composable
fun CardInstrucciones(umbrales: UmbralesUI) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Título de la Card
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.SportsEsports, // Un icono de "reglas" o "joystick"
                    contentDescription = "Controles",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text("Controles EEG", style = MaterialTheme.typography.titleLarge)
            }

            // Usamos InfoRow para una visualización limpia
            InfoRow(label = "Arriba (Acelerar)", value = "Atención > ${umbrales.atencion}")
            InfoRow(label = "Abajo (Reversa)", value = "Meditación > ${umbrales.meditacion}")
            InfoRow(label = "Izquierda (Giro)", value = umbrales.parpadeoDoble)
            InfoRow(label = "Derecha (Giro)", value = umbrales.parpadeoSimple)
            InfoRow(label = "Centro (Freno)", value = umbrales.freno)
        }
    }
}

/**
 * Muestra una fila con una etiqueta a la izquierda y un valor a la derecha.
 */
@Composable
private fun InfoRow(label: String, value: String, valueColor: Color = Color.Unspecified) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
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

@Composable
private fun getSignalQualityColor(signal: Int): Color {
    return when {
        signal == 0 -> Color(0xFF4CAF50) // Verde brillante
        signal in 1..50 -> Color(0xFF8BC34A) // Verde lima
        signal in 51..100 -> Color(0xFFFFC107) // Ámbar
        signal in 101..150 -> Color(0xFFF4511E) // Naranja oscuro
        else -> MaterialTheme.colorScheme.error // Rojo
    }
}

//********************** Lógica Dpad **********************

@Composable
fun ModernDpad(
    modifier: Modifier = Modifier,
    onDirectionClick: (Direction) -> Unit,
    comandoMovimientoActivado: Direction?,
    comandoDireccionActivado: Direction?
) {
    Box(
        modifier = modifier.size(220.dp), // Tamaño total del D-Pad
        contentAlignment = Alignment.Center
    ) {
        // --- Botones Direccionales ---
        DirectionalButton(
            onClick = { onDirectionClick(Direction.UP) },
            // El botón UP solo se activa por el comando de movimiento
            isActivatedByEEG = comandoMovimientoActivado == Direction.UP,
            icon = Icons.Default.KeyboardArrowUp,
            contentDescription = "Arriba",
            modifier = Modifier.align(Alignment.TopCenter)
        )
        DirectionalButton(
            onClick = { onDirectionClick(Direction.LEFT) },
            // El botón LEFT solo se activa por el comando de dirección
            isActivatedByEEG = comandoDireccionActivado == Direction.LEFT,
            icon = Icons.Default.KeyboardArrowUp,
            contentDescription = "Izquierda",
            modifier = Modifier.align(Alignment.CenterStart).rotate(-90f)
        )
        DirectionalButton(
            onClick = { onDirectionClick(Direction.RIGHT) },
            // El botón RIGHT solo se activa por el comando de dirección
            isActivatedByEEG = comandoDireccionActivado == Direction.RIGHT,
            icon = Icons.Default.KeyboardArrowUp,
            contentDescription = "Derecha",
            modifier = Modifier.align(Alignment.CenterEnd).rotate(90f)
        )
        DirectionalButton(
            onClick = { onDirectionClick(Direction.DOWN) },
            // El botón DOWN solo se activa por el comando de movimiento
            isActivatedByEEG = comandoMovimientoActivado == Direction.DOWN,
            icon = Icons.Default.KeyboardArrowUp,
            contentDescription = "Abajo",
            modifier = Modifier.align(Alignment.BottomCenter).rotate(180f)
        )

        // --- Botón Central ---
        // --> AÑADIDO: Haremos que el botón central también reaccione
        val isCenterActivated = comandoMovimientoActivado == Direction.CENTER
        val centerScale by animateFloatAsState(if (isCenterActivated) 1.1f else 1.0f, label = "center_scale")
        val centerColor by animateColorAsState(
            if (isCenterActivated) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surfaceBright,
            label = "center_color"
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
                            centerColor, // Usamos el color animado
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
    isActivatedByEEG: Boolean,
    contentDescription: String,
    modifier: Modifier = Modifier,
    buttonSize: Dp = 100.dp,
    iconSize: Dp = 32.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // La variable que combina ambos estados. Esta es nuestra única fuente de verdad.
    val isActivated = isPressed || isActivatedByEEG

    // --> CORRECCIÓN: Todas las animaciones ahora dependen de 'isActivated'.
    val elevation by animateDpAsState(if (isActivated) 2.dp else 8.dp, label = "elevation")
    val scale by animateFloatAsState(if (isActivated) 0.95f else 1f, label = "scale")

    val containerColor by animateColorAsState(
        if (isActivated) MaterialTheme.colorScheme.surfaceContainerHigh
        else MaterialTheme.colorScheme.surface,
        label = "containerColor"
    )
    val iconColor by animateColorAsState(
        if (isActivated) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "iconColor"
    )

    // El resto del Box y el Icon no necesitan cambios.
    Box(
        modifier = modifier
            .size(buttonSize)
            .scale(scale) // La escala se aplica aquí
            .shadow(
                elevation = elevation, // La elevación se aplica aquí
                shape = ArcShape(90f),
                clip = false
            )
            .clip(ArcShape(90f))
            .background(containerColor) // El color se aplica aquí
            .clickable(interactionSource = interactionSource, indication = null) {
                onClick()
            }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconColor, // El color del icono se aplica aquí
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