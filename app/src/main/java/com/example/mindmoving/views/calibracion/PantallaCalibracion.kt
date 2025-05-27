package com.example.mindmoving.views.calibracion

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

@Composable
fun PantallaCalibracion(navController: NavHostController) {
    val gradient = Brush.verticalGradient(listOf(Color(0xFF3F51B5), Color(0xFFB0C4DE)))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
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
                fontSize = 26.sp,
                color = Color.White
            )

            CalibracionButton("Perfil de calibración") {
                navController.navigate("perfil_calibracion")
            }

            CalibracionButton("Calibración guiada") {
                navController.navigate("calibracion_guiada")
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
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
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
