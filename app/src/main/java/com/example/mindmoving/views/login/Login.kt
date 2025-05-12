package com.example.mindmoving.views.login

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mindmoving.ui.theme.Typography
import androidx.compose.ui.graphics.Brush
import androidx.navigation.NavHostController
import com.example.mindmoving.R


@Composable
fun Login(navController: NavHostController) {
    ContentLoginView(navController)
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentLoginView(navController: NavHostController) {
    var userdata by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize().padding(),
    ){
        Image(
            painter = painterResource(id = R.drawable.fondo_5),
            contentDescription = "Fondo de pantalla",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(135.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                   painter = painterResource(id = R.drawable.logo_mindmoving_sinfondo),
                    contentDescription = "Imagen Logo",
                    modifier = Modifier
                        .size(200.dp)
                        .padding(5.dp)
                )
                Text(
                    text = "MindMoving",
                    color = Color.White,
                    fontSize = 40.sp,
                    style = Typography.titleMedium
                )
            }
            Spacer(modifier = Modifier.height(50.dp))
            Row {
                OutlinedTextField(
                    value = userdata, onValueChange = {userdata = it},
                    placeholder = { // Usamos placeholder en lugar de label para texto interior
                        Text(
                            text = "Correo electrónico o username",
                            color = Color(185, 185, 185),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal
                        )
                    },
                    shape = RoundedCornerShape(40),
                    colors = TextFieldDefaults.textFieldColors(
                        // Color del texto cuando escribes
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        // Color de fondo del campo
                        containerColor = Color(0xFF1F1A33).copy(alpha = 0.4f), // Un color oscuro semi-transparente (ajústalo al exacto si lo tienes)
                        // Colores del indicador (la línea de abajo), los hacemos transparentes
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Transparent, // También para el estado de error si lo manejas
                        // Color del cursor
                        cursorColor = Color.White
                    ),
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal
                    )
                )
            }
            Row (
                modifier = Modifier.padding(15.dp)
            ) {
                OutlinedTextField(
                    value = password, onValueChange = {password = it},
                    placeholder = { // Usamos placeholder en lugar de label para texto interior
                        Text(
                            text = "Contraseña",
                            color = Color(185, 185, 185),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Light
                        )
                    },
                    shape = RoundedCornerShape(40),
                    colors = TextFieldDefaults.textFieldColors(
                        // Color del texto cuando escribes
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        // Color de fondo del campo
                        containerColor = Color(0xFF1F1A33).copy(alpha = 0.4f), // Un color oscuro semi-transparente (ajústalo al exacto si lo tienes)
                        // Colores del indicador (la línea de abajo), los hacemos transparentes
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Transparent, // También para el estado de error si lo manejas
                        // Color del cursor
                        cursorColor = Color.White
                    ),
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Light
                    )
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row {
                // --- ***** INICIO DEL CÓDIGO DEL BOTÓN ***** ---
                // 1. Define los colores y el pincel del gradiente
                val startColorButton = Color(67, 137, 254) // Ajusta este color
                val endColorButton = Color(2, 97, 254 )   // Ajusta este color
                val gradientBrushButton = Brush.horizontalGradient(
                    colors = listOf(startColorButton, endColorButton)
                )
                // 2. Define la forma
                val buttonShapeButton = RoundedCornerShape(40)
                // 3. Crea el Botón
                Button(
                    onClick = {
                        navController.navigate("calibracion_menu") {
                            popUpTo("login") { inclusive = true } // Evita volver atrás al login
                        }

                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 60.dp)
                        .height(55.dp),
                    shape = buttonShapeButton,
                    contentPadding = PaddingValues(), // Quitar padding interno
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent // Fondo del botón transparente
                    ),
                    elevation = ButtonDefaults.buttonElevation( // Opcional: sin sombra
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp
                    )
                ) {
                    // 4. Box interno para aplicar el fondo de gradiente
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = gradientBrushButton,
                                shape = buttonShapeButton
                            )
                            .clip(buttonShapeButton),
                        contentAlignment = Alignment.Center
                    ) {
                        // 5. Texto del botón
                        Text(
                            text = "Iniciar sesión",
                            color = Color.White,
                            fontSize = 20.sp,
                            style = Typography.bodyMedium
                        )
                    }
                }
                // --- ***** FIN DEL CÓDIGO DEL BOTÓN ***** ---
                Spacer(modifier = Modifier.height(20.dp))
            }
            Spacer(modifier = Modifier.height(9.dp))
            Row {
                Text(
                    text = "¿No tienes cuenta? Regístrate",
                    color = Color(114, 32, 248),
                    modifier = Modifier.clickable {
                        navController.navigate("register")
                    }
                )

            }
        }
    }
}