package com.example.mindmoving.views.calibracion

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssistWalker
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.DeveloperMode



@Composable
fun PantallaCalibracion(navController: NavHostController) {
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surfaceVariant
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
                color = MaterialTheme.colorScheme.onBackground
            )

            CalibracionButton("Perfil de calibración", Icons.Default.Person) {
                navController.navigate("perfil_calibracion")
            }

            CalibracionButton("Calibración guiada", Icons.Default.AssistWalker) {
                navController.navigate("calibracion_inicio")
            }

            CalibracionButton("Juego Atención", Icons.Default.CenterFocusStrong) {
                navController.navigate("juego_concentracion")
            }


            CalibracionButton("Juego Meditación", Icons.Default.SelfImprovement) {
                navController.navigate("juego_meditacion")
            }

            CalibracionButton("Juego Pestañeo", Icons.Default.RemoveRedEye) {
                navController.navigate("juego_parpadeo")
            }

            CalibracionButton("Simulador de comandos", Icons.Default.DeveloperMode) {
                navController.navigate("comandos_diadema")
            }


            TextButton(
                onClick = {
                    navController.navigate("menu") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Inicio",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    "Ir al menú principal",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

        }
    }
}

@Composable
fun CalibracionButton(text: String, icon: ImageVector, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp),
        shape = RoundedCornerShape(30),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 8.dp)
            )
            Text(
                text = text,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}


@Composable
@Preview(showBackground = true, showSystemUi = true)
fun PreviewPantallaCalibracion() {
    val navController = rememberNavController()
    PantallaCalibracion(navController)
}