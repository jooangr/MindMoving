package com.example.mindmoving.views.menuDrawer.viewsMenuDrawer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AyudaScreen(navController: NavController) {
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF3F51B5), Color(0xFF2196F3))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Info sobre la App") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBrush)
                .padding(padding) // Padding del Scaffold
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Ayuda de MindMoving",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            AyudaSection(
                title = "¿Qué es MindMoving?",
                description = "MindMoving es una aplicación que utiliza una banda EEG (como NeuroSky) para detectar tus niveles de atención, relajación y parpadeos. Con esta información, puedes controlar interfaces como un coche RC o juegos mentales."
            )

            AyudaSection(
                title = "¿Cómo calibrar tu mente?",
                description = "1. Realiza tres pruebas: ATENTO, MEDITATIVO y EQUILIBRADO.\n" +
                        "2. Sigue las instrucciones en pantalla durante cada prueba.\n" +
                        "3. Se calcularán promedios personalizados para ti.\n" +
                        "4. Se creará un perfil mental con base en estos valores."
            )

            AyudaSection(
                title = "Control por parpadeo",
                description = "Puedes activar funciones parpadeando voluntariamente. En los ajustes puedes configurar su comportamiento: por ejemplo, usarlo para ejecutar comandos o cambiar de modo. Asegúrate de no confundir parpadeos normales con intencionales."
            )

            AyudaSection(
                title = "Consejos para mejorar la precisión",
                description = "- Usa la banda en un entorno sin distracciones.\n" +
                        "- Asegúrate de que el sensor esté bien colocado.\n" +
                        "- Relájate antes de iniciar una sesión.\n" +
                        "- Mantente quieto durante las pruebas."
            )
        }
    }
}

@Composable
fun AyudaSection(title: String, description: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1A237E)) // Azul marino oscuro
            .padding(16.dp)
    ) {
        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = description, fontSize = 14.sp, color = Color.White)
    }
}