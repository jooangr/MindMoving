package com.example.mindmoving.views.login

import android.content.Context
import android.os.Handler
import android.os.Looper
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.graphics.Brush
import androidx.navigation.NavHostController
import com.example.mindmoving.R
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.mindmoving.retrofit.ApiClient
import com.example.mindmoving.retrofit.ApiService
import com.example.mindmoving.retrofit.models.user.AlternanciaData
import com.example.mindmoving.retrofit.models.user.BlinkingData
import com.example.mindmoving.retrofit.models.login_register.LoginRequest
import com.example.mindmoving.retrofit.models.user.Usuario
import com.example.mindmoving.retrofit.models.user.ValoresEEG
import com.example.mindmoving.ui.theme.AppTheme
import com.example.mindmoving.ui.theme.AppTypography
import com.example.mindmoving.utils.LocalThemeViewModel
import com.example.mindmoving.utils.SessionManager
import com.example.mindmoving.utils.ThemeViewModel
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Pantalla de Login que permite al usuario autenticarse con su email o nombre de usuario.
 * Si el login es exitoso, guarda su información en SharedPreferences y lo redirige al menú principal o calibración.
 */
@Composable
fun Login(navController: NavHostController) {
    val themeViewModel = LocalThemeViewModel.current
    AppTheme(darkTheme = true) {
        ContentLoginView(navController, themeViewModel)
    }
}

@Composable
fun ContentLoginView(navController: NavHostController, themeViewModel: ThemeViewModel) {
    var userdata by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val apiService = ApiClient.getApiService()

    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val darkThemeState = remember { mutableStateOf(false) }

    val gradientBackground = Brush.verticalGradient(
        colors = listOf(Color(0xFF0A0A23), Color(0xFF1A1A40))
    )

    if (isLoading) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Iniciando sesión...", color = MaterialTheme.colorScheme.primary) },
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Por favor espera...", color = MaterialTheme.colorScheme.onSurface)
                }
            },
            confirmButton = {},
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Error de inicio de sesión", color = MaterialTheme.colorScheme.error) },
            text = { Text(dialogMessage, color = MaterialTheme.colorScheme.onSurface) },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Aceptar", color = MaterialTheme.colorScheme.primary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    Box(
        modifier = Modifier.fillMaxSize().background(gradientBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(70.dp))

            Image(
                painter = painterResource(id = R.drawable.logo_mindmoving_sinfondo),
                contentDescription = "Logo",
                modifier = Modifier.size(200.dp).padding(5.dp)
            )
            Text("MindMoving", color = Color.White, fontSize = 40.sp, style = AppTypography.titleMedium)

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = userdata,
                onValueChange = { userdata = it },
                placeholder = { Text("Correo electrónico o username") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                shape = RoundedCornerShape(40),
                singleLine = true
            )

            PasswordField(
                value = password,
                onValueChange = { password = it },
                placeholder = "Contraseña",
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            val buttonShape = RoundedCornerShape(40)
            val gradientBrush = Brush.horizontalGradient(
                colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
            )

            Button(
                onClick = {

                    coroutineScope.launch {
                        isLoading = true
                        val sharedPrefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)

                        try {
                            val response = apiService.loginUser(LoginRequest(userdata.trim(), password.trim()))

                            if (response.isSuccessful && response.body()?.userId != null) {
                                val userId = response.body()!!.userId

                                val now = System.currentTimeMillis()
                                sharedPrefs.edit().putString("userId", userId)
                                    .putLong("lastLoginTime", now)
                                    .putLong("lastPausedTime", now)
                                    .apply()

                                val userInfoResponse = apiService.getUsuario(userId)
                                if (!userInfoResponse.isSuccessful || userInfoResponse.body() == null) {
                                    isLoading = false
                                    dialogMessage = "Error obteniendo información del usuario"
                                    showDialog = true
                                    return@launch
                                }

                                val userInfo = userInfoResponse.body()!!
                                val perfilResponse = apiService.getPerfil(userId)
                                val perfil = if (perfilResponse.isSuccessful) perfilResponse.body() else null

                                val usuarioCompleto = Usuario(
                                    id = userId,
                                    username = userInfo.username,
                                    email = userInfo.email,
                                    password = "",
                                    perfilCalibracion = perfil?.tipo ?: "",
                                    valoresAtencion = perfil?.valoresAtencion ?: ValoresEEG(0, 0, 0, 0f),
                                    valoresMeditacion = perfil?.valoresMeditacion ?: ValoresEEG(0, 0, 0, 0f),
                                    blinking = perfil?.blinking ?: BlinkingData(0, 0),
                                    alternancia = perfil?.alternancia ?: AlternanciaData(0, 0)
                                )

                                val tienePerfilValido = perfil?.tipo?.isNotBlank() == true

                                sharedPrefs.edit()
                                    .putString("perfil_tipo", perfil?.tipo ?: "")
                                    .putString("perfil_completo", Gson().toJson(usuarioCompleto))
                                    .apply()

                                SessionManager.usuarioActual = usuarioCompleto
                                isLoading = false
                                Toast.makeText(context, "Login exitoso", Toast.LENGTH_SHORT).show()

                                navController.navigate(if (tienePerfilValido) "menu" else "calibracion_menu") {
                                    popUpTo(0) { inclusive = true }
                                }

                                apiService.getTheme(userId).enqueue(object : Callback<ApiService.ThemeResponse> {
                                    override fun onResponse(call: Call<ApiService.ThemeResponse>, response: Response<ApiService.ThemeResponse>) {
                                        val theme = if (response.isSuccessful) response.body()?.theme ?: "light" else "light"
                                        darkThemeState.value = theme == "dark"
                                        sharedPrefs.edit().putString("user_theme", theme).apply()
                                        Handler(Looper.getMainLooper()).post {
                                            themeViewModel.setTheme(theme == "dark")
                                        }
                                    }

                                    override fun onFailure(call: Call<ApiService.ThemeResponse>, t: Throwable) {
                                        isLoading = false
                                        dialogMessage = "No se pudo obtener el tema del usuario"
                                        showDialog = true
                                    }
                                })
                            } else {
                                isLoading = false
                                dialogMessage = "Usuario, correo o contraseña incorrectos"
                                showDialog = true
                            }
                        } catch (e: Exception) {
                            isLoading = false
                            dialogMessage = "Error de red: ${e.message}"
                            showDialog = true
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 60.dp).height(55.dp),
                shape = buttonShape,
                contentPadding = PaddingValues(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(gradientBrush, shape = buttonShape)
                        .clip(buttonShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Iniciar sesión", color = MaterialTheme.colorScheme.onPrimary, fontSize = 20.sp, style = AppTypography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "¿No tienes cuenta? Regístrate",
                color = MaterialTheme.colorScheme.primary,
                style = AppTypography.bodySmall,
                modifier = Modifier.clickable {
                    navController.navigate("register")
                }
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewLoginScreen() {
    val navController = rememberNavController()
    Login(navController = navController)
}

@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                placeholder,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                fontSize = 16.sp
            )
        },
        shape = RoundedCornerShape(40),
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
            errorContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
            disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent
        ),
        textStyle = AppTypography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface),
        modifier = modifier
    )
}
