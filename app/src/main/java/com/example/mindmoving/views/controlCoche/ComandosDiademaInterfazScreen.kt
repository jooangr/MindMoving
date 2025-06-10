package com.example.mindmoving.views.controlCoche

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.mindmoving.R
//
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*

import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/*
@Composable
fun ComandosDireccion (navController: NavHostController){
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize(),
        ) {
            // Fila 1: Botón arriba

            IconButton(onClick = {  }) {
                Image(
                    painter = painterResource(
                        id = R.drawable.icon_flecha_anvanzar_2),
                    contentDescription = "Forward",
                    modifier = Modifier.size(300.dp),
                    contentScale = ContentScale.FillBounds
                )
            }
            Spacer(modifier = Modifier.height(32.dp))

            // Fila 2: Botón izquierda - espacio vacío - botón derecha
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {  }) {
                    Icon(
                        painter = painterResource(
                            id = R.drawable.baseline_arrow_back_ios_new_24),
                        contentDescription = "Left",
                        tint = Color.Black,
                        modifier = Modifier.size(30.dp)
                    )
                }
                Spacer(modifier = Modifier.width(80.dp)) // Espacio central (vacío)

                IconButton(onClick = {  }) {
                    Icon(
                        painter = painterResource(
                            id = R.drawable.baseline_arrow_back_ios_new_24),
                        contentDescription = "Right",
                        tint = Color.Black,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Fila 3: Botón abajo
            IconButton(onClick = {  }) {
                Icon(
                    painter = painterResource(
                        id = R.drawable.baseline_arrow_back_ios_new_24),
                    contentDescription = "Backward",
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}

 */

@Preview(showBackground = true)
@Composable
fun ModernDpadScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            // Un fondo con gradiente sutil para que el D-Pad destaque
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFE0E0E0), Color(0xFFF5F5F5))
                )
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        Spacer(modifier = Modifier.size(20.dp))
        ModernDpad { direction ->
            // Aquí manejas la lógica del clic
            // Por ejemplo, imprimir en la consola para depurar
            println("Botón presionado: $direction")
        }

    }
}

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