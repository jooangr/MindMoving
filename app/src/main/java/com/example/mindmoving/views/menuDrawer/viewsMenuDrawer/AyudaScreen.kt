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
    // Usamos un Scaffold con barra superior personalizada
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Info sobre la App", color = MaterialTheme.colorScheme.onSurface)
                },
                navigationIcon = {
                    // Botón de retroceso
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        // Contenido principal desplazable
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Título principal
            Text(
                text = "Ayuda de MindMoving",
                fontSize = 24.sp,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Sección: ¿Qué es la app?
            AyudaSection(
                title = "¿Qué es MindMoving?",
                description = "MindMoving es una aplicación que utiliza una banda EEG (como NeuroSky) para detectar tus niveles de atención, relajación y parpadeos. Con esta información, puedes controlar interfaces como un coche RC o juegos mentales."
            )

            // Sección: Cómo calibrar
            AyudaSection(
                title = "¿Cómo calibrar tu mente?",
                description = "1. Realiza tres pruebas: ATENTO, MEDITATIVO y EQUILIBRADO.\n" +
                              "2. Sigue las instrucciones en pantalla durante cada prueba.\n" +
                              "3. Se calcularán promedios personalizados para ti.\n" +
                              "4. Se creará un perfil mental con base en estos valores."
            )

            // Sección: Uso de parpadeos
            AyudaSection(
                title = "Control por parpadeo",
                description = "Puedes activar funciones parpadeando voluntariamente. En los ajustes puedes configurar su comportamiento: por ejemplo, usarlo para ejecutar comandos o cambiar de modo. Asegúrate de no confundir parpadeos normales con intencionales."
            )

            // Sección: Consejos de precisión
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

// Componente reutilizable para mostrar secciones informativas dentro de la pantalla de ayuda
@Composable
fun AyudaSection(title: String, description: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        // Título de la sección
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Descripción de la sección
        Text(
            text = description,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
