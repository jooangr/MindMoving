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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.activity.compose.BackHandler
import androidx.compose.ui.platform.LocalContext


@Composable
fun PantallaCalibracion(navController: NavHostController) {

    val gradient = Brush.verticalGradient(listOf(Color(0xFF3F51B5), Color(0xFFB0C4DE)))

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
            .background(gradient)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxHeight()
        ) {
            Text(
                text = "Antes de empezar...",
                fontSize = 26.sp,
                color = Color.White
            )

            Text(
                text = "Puedes seleccionar un perfil de atención por defecto o realizar pruebas personalizadas para calibrarlo.",
                fontSize = 16.sp,
                color = Color.White,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Botones circulares para perfiles por defecto
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                DefaultProfileButton("Bajo") { /* Guardar perfil BAJO */ }
                DefaultProfileButton("Medio") { /* Guardar perfil MEDIO */ }
                DefaultProfileButton("Alto") { /* Guardar perfil ALTO */ }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botón para niveles personalizados
            Button(
                onClick = {
                    navController.navigate("calibracion_atencion")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("Niveles personalizados", fontSize = 18.sp, color = Color.White)
            }
            // Botón "Hacer luego"
            TextButton(
                onClick = {
                    navController.navigate("menu") {
                        popUpTo(0) { inclusive = true } // 💣 Esto borra TODA la pila
                    }

                },
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Text("Hacer luego", color = Color.White, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun DefaultProfileButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .background(Color.White.copy(alpha = 0.2f), shape = CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = Color.White, fontSize = 14.sp)
    }
}
