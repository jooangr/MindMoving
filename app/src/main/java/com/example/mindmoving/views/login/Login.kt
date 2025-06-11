package com.example.mindmoving.views.login

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.material3.OutlinedTextFieldDefaults

import androidx.compose.ui.graphics.Brush
import androidx.navigation.NavHostController
import com.example.mindmoving.R
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.mindmoving.retrofit.ApiClient
import com.example.mindmoving.retrofit.models.AlternanciaData
import com.example.mindmoving.retrofit.models.BlinkingData
import com.example.mindmoving.retrofit.models.LoginRequest
import com.example.mindmoving.retrofit.models.Usuario
import com.example.mindmoving.retrofit.models.ValoresEEG
import com.example.mindmoving.ui.theme.AppTypography
import com.example.mindmoving.utils.SessionManager
import com.google.gson.Gson



@Composable
fun Login(navController: NavHostController) {

    ContentLoginView(navController)

}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentLoginView(navController: NavHostController) {
    var userdata by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val apiService = ApiClient.getApiService()

    //Error y avisos
    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Error de inicio de sesión", color = Color.Black) },
            text = { Text(dialogMessage, color = Color.Black) },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Aceptar", color = MaterialTheme.colorScheme.primary)
                }
            },
            containerColor = Color.White
        )
    }

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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top

        ) {
            Spacer(modifier = Modifier.height(70.dp))
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
                    style = AppTypography.titleMedium
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            Row {
                OutlinedTextField(
                    value = userdata,
                    onValueChange = { userdata = it },
                    placeholder = {
                        Text(
                            text = "Correo electrónico o username",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal
                        )
                    },
                    shape = RoundedCornerShape(40),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Transparent,
                        unfocusedLabelColor = Color.Transparent,
                        errorBorderColor = Color.Transparent,
                        cursorColor = Color.White,

                        focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                        disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                        errorContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                    ),
                    singleLine = true,
                    textStyle = AppTypography.bodySmall.copy(color = Color.White, fontSize = 14.sp)

                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row (
                modifier = Modifier.padding(15.dp)
            ) {
                OutlinedTextField(
                    value = password, onValueChange = {password = it},
                    placeholder = { // Usamos placeholder en lugar de label para texto interior
                        Text(
                            text = "Contraseña",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Light
                        )
                    },
                    shape = RoundedCornerShape(40),
                    colors = OutlinedTextFieldDefaults.colors(
                        // Color del texto cuando escribes
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        //Color del fonod del campo
                        focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                        disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                        errorContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),

                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        disabledBorderColor = Color.Transparent,
                        errorBorderColor = Color.Transparent,


                        cursorColor = Color.White

                    ),
                    singleLine = true,
                    textStyle = AppTypography.bodySmall.copy(color = Color.White, fontSize = 14.sp)

                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row {

                Row {
                    val startColorButton = MaterialTheme.colorScheme.primary
                    val endColorButton = MaterialTheme.colorScheme.secondary

                    val gradientBrushButton = Brush.horizontalGradient(
                        colors = listOf(startColorButton, endColorButton)
                    )
                    val buttonShapeButton = RoundedCornerShape(40)

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    val response = apiService.loginUser(LoginRequest(userdata.trim(), password.trim()))
                                    if (response.isSuccessful && response.body()?.userId != null) {

                                        // Guardar userId en SharedPreferences
                                        val sharedPrefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
                                        val currentTime = System.currentTimeMillis()

                                        val now = System.currentTimeMillis()
                                        sharedPrefs.edit()
                                            .putString("userId", response.body()?.userId)
                                            .putLong("lastLoginTime", now)
                                            .putLong("lastPausedTime", now) // 🔥 importante: reinicia el contador de inactividad
                                            .apply()

                                        // Obtener el perfil del backend y decidir navegación
                                        val userId = response.body()?.userId ?: return@launch

                                        // 1. Obtener datos del usuario desde el backend
                                        val userInfoResponse = apiService.getUsuario(userId)
                                        if (!userInfoResponse.isSuccessful || userInfoResponse.body() == null) {
                                            dialogMessage = "Error obteniendo información del usuario"
                                            showDialog = true
                                            return@launch
                                        }
                                        val userInfo = userInfoResponse.body()!!

                                        // 2. Obtener perfil de calibración si existe
                                        val perfilResponse = apiService.getPerfil(userId)
                                        Log.d("LOGIN", "Perfil recibido del backend: ${perfilResponse.body()}")

                                        val perfil = if (perfilResponse.isSuccessful) perfilResponse.body() else null

                                        // 3. Construir usuario completo con datos combinados
                                        val usuarioCompleto = Usuario(
                                            id = userId,
                                            username = userInfo.username,
                                            email = userInfo.email,
                                            password = "", //contraseña no por qle backend no deja
                                            perfilCalibracion = perfil?.tipo ?: "",
                                            valoresAtencion = perfil?.valoresAtencion ?: ValoresEEG(0, 0, 0, 0f),
                                            valoresMeditacion = perfil?.valoresMeditacion ?: ValoresEEG(0, 0, 0, 0f),
                                            blinking = perfil?.blinking ?: BlinkingData(0, 0),
                                            alternancia = perfil?.alternancia ?: AlternanciaData(0, 0)
                                        )

                                        // 4. Guardar en SharedPreferences y en SessionManager
                                        val perfilJson = Gson().toJson(usuarioCompleto)
                                        sharedPrefs.edit()
                                            .putString("perfil_tipo", perfil?.tipo)
                                            .putString("perfil_completo", perfilJson)
                                            .apply()

                                        SessionManager.usuarioActual = usuarioCompleto

                                        Log.d("LOGIN", "✅ Usuario COMPLETO guardado con ID: ${usuarioCompleto.id}")
                                        Toast.makeText(context, "Login exitoso", Toast.LENGTH_SHORT).show()

                                        // 5. Navegar según si tiene perfil o no
                                        if (perfil != null) {
                                            navController.navigate("menu") {
                                                popUpTo(0) { inclusive = true }
                                            }
                                        } else {
                                            navController.navigate("calibracion_menu") {
                                                popUpTo(0) { inclusive = true }
                                            }
                                        }


                                    } else {
                                        dialogMessage = "Usuario, correo o contraseña incorrectos"
                                        showDialog = true
                                    }
                                } catch (e: Exception) {
                                    dialogMessage = "Error de red: ${e.message}"
                                    showDialog = true
                                }
                            }

                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 60.dp)
                            .height(55.dp),
                        shape = buttonShapeButton,
                        contentPadding = PaddingValues(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        elevation = ButtonDefaults.buttonElevation(
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
                                style = AppTypography.bodyMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
                Spacer(modifier = Modifier.height(9.dp))
            Row {
                Text(
                    text = "¿No tienes cuenta? Regístrate",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        navController.navigate("register")
                    }
                )

            }
        }
    }
}
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewLoginScreen() {
    val navController = rememberNavController()
    Login(navController = navController)
}
