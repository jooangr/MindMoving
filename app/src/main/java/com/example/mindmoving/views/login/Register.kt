package com.example.mindmoving.views.login

//noinspection UsingMaterialAndMaterial3Libraries
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.mindmoving.retrofit.ApiClient
import com.example.mindmoving.retrofit.models.RegisterRequest
import com.example.mindmoving.views.controlCoche.ControlCocheScreen
import kotlinx.coroutines.launch
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.example.mindmoving.R
import com.example.mindmoving.ui.theme.Typography


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavHostController) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val apiService = ApiClient.getApiService()


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF3F2B96), // morado azulado
                        Color(0xFF5C258D), // violeta intenso
                        Color(0xFF6A0572)  // púrpura oscuro
                    )
                )
            )

            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter = painterResource(id = R.drawable.baseline_person_24),
                contentDescription = "Imagen Logo",
                modifier = Modifier
                    .size(90.dp)
                    .padding(5.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Crear cuenta",
                color = Color.White,
                fontSize = 40.sp,
                style = Typography.titleMedium
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                placeholder = { // Usamos placeholder en lugar de label para texto interior
                    Text(
                        text = "Nombre de usuario",
                        color = Color(185, 185, 185),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Light
                    )
                },
                shape = RoundedCornerShape(40),
                colors = androidx.compose.material3.TextFieldDefaults.textFieldColors(
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


            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { // Usamos placeholder en lugar de label para texto interior
                    Text(
                        text = "Correo electrónico",
                        color = Color(185, 185, 185),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Light
                    )
                },
                shape = RoundedCornerShape(40),
                colors = androidx.compose.material3.TextFieldDefaults.textFieldColors(
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

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { // Usamos placeholder en lugar de label para texto interior
                    Text(
                        text = "Contraseña",
                        color = Color(185, 185, 185),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Light
                    )
                },
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(40),
                colors = androidx.compose.material3.TextFieldDefaults.textFieldColors(
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

            Spacer(modifier = Modifier.height(24.dp))

            // Variables para diseño del botón Registro
            val startColorButton = Color(0xFF66BB6A)
            val endColorButton = Color(0xFF4CAF50)
            val gradientBrushButton = Brush.horizontalGradient(
                colors = listOf(startColorButton, endColorButton)
            )
            val buttonShapeButton = RoundedCornerShape(40)

            Button(

                onClick = {
                    coroutineScope.launch {
                        try {
                            val response = apiService.registerUser(
                                RegisterRequest(
                                    username = username,
                                    email = email,
                                    password = password
                                )

                            )
                            if (response.isSuccessful && response.body()?.success == true) {
                                Toast.makeText(context, "Registro exitoso", Toast.LENGTH_SHORT).show()
                                navController.navigate("login") {
                                    popUpTo("register") { inclusive = true }
                                }
                            } else {
                                val errorMsg = response.body()?.message ?: "Error al registrar"
                                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error de red: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                //colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50))
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 60.dp)
                    .height(55.dp),
                shape = buttonShapeButton,
                contentPadding = PaddingValues(),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                elevation = androidx.compose.material3.ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(brush = gradientBrushButton, shape = buttonShapeButton)
                        .clip(buttonShapeButton),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Iniciar sesión",
                        color = Color.White,
                        fontSize = 20.sp,
                        style = Typography.bodyMedium
                    )
                }
            }


            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = {
                navController.navigate("login") {
                    popUpTo("register") { inclusive = true }
                }
            }) {
                Text("¿Ya tienes cuenta? Inicia sesión", color = Color.LightGray)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegisterScreenPreview() {
    // Puedes usar un NavHostController falso si no necesitas navegación
    RegisterScreen(navController = rememberNavController())
}
