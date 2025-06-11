package com.example.mindmoving.views.calibracion

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.activity.compose.BackHandler
import androidx.compose.ui.platform.LocalContext


@Composable
fun PantallaCalibracion(navController: NavHostController) {

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primaryContainer
        )
    )
    /**
     * Intercepta el botón físico de atrás cuando estás en esta pantalla
     *
     * En lugar de volver a login, cierra la app, como hacen apps reales tras iniciar sesión
     */
    val context = LocalContext.current
    BackHandler(enabled = true) {
        (context as? Activity)?.finish()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Opciones de Calibración",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimary
            )

            CalibracionButton("Perfil de calibración") {
                navController.navigate("perfil_calibracion")
            }

            CalibracionButton("Calibración guiada") {
                navController.navigate("calibracion_inicio")
            }

            CalibracionButton("Ajustar atención") {
                navController.navigate("ajustar_atencion")
            }

            CalibracionButton("Ajustar meditación") {
                navController.navigate("ajustar_meditacion")
            }

            CalibracionButton("Calibrar pestañeo") {
                navController.navigate("calibracion_pestaneo")
            }

            CalibracionButton("Simulador de comandos") {
                navController.navigate("simulador_comandos")
            }

            TextButton(
                onClick = {
                    navController.navigate("menu") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Text(
                    "Hacer luego",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
fun CalibracionButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp),
        shape = RoundedCornerShape(30),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Text(text = text, fontSize = 18.sp, color = Color.White)
    }
}
@Composable
@Preview(showBackground = true, showSystemUi = true)
fun PreviewPantallaCalibracion() {
    // Importante: este require tener 'androidx.navigation:navigation-compose' en tu proyecto
    val navController = rememberNavController()
    PantallaCalibracion(navController)
}