package com.example.mindmoving.ui.theme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Column

// Composable reutilizable que envuelve un contenido en una animación de aparición (fade-in) con un pequeño retraso.
@Composable
fun FadeInColumn(
    modifier: Modifier = Modifier,
    delayMillis: Long = 300,
    content: @Composable () -> Unit
) {

    // Estado que controla si el contenido es visible
    var visible by remember { mutableStateOf(false) }

    // Efecto lanzado al componerse por primera vez, espera el tiempo indicado y luego hace visible el contenido
    LaunchedEffect(Unit) {
        delay(delayMillis)
        visible = true
    }

    // Animación de visibilidad con efectos de entrada y salida (fade)
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Column(modifier = modifier) {
            content()
        }
    }
}
