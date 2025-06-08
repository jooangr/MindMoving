package com.example.mindmoving.views.calibracion.guiada

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

@Composable
fun CalibracionInicioScreen(navController: NavHostController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "INICIANDO PROCESO DE CALIBRACIÓN MENTAL",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.Cyan,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Conéctate. Sincroniza. Domina tu mente.",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = { navController.navigate("fase_calibracion") },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan)
            ) {
                Text("INICIAR CALIBRACIÓN")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCalibracionInicioScreen() {
    CalibracionInicioScreen(navController = rememberNavController())
}