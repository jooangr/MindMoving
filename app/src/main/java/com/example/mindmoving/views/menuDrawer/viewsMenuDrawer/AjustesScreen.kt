package com.example.mindmoving.views.menuDrawer.viewsMenuDrawer

import android.app.Activity
import androidx.compose.ui.Alignment
import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.mindmoving.retrofit.ApiClient
import com.example.mindmoving.retrofit.ApiService
import com.example.mindmoving.utils.LocalThemeViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen(navController: NavHostController) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("mindmoving_prefs", Context.MODE_PRIVATE)

    val themeViewModel = LocalThemeViewModel.current
    var darkMode by remember { mutableStateOf(themeViewModel.isDarkTheme.value) }

    var perfilPredeterminado by remember {
        mutableStateOf(sharedPreferences.getString("perfil_predeterminado", "EQUILIBRADO") ?: "EQUILIBRADO")
    }
    var showSnackbar by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(16.dp)
                .padding(paddingValues)
        ) {
            // 🌗 Cambiar tema
            Text("Modo oscuro", color = textColor)
            Switch(
                checked = darkMode,
                onCheckedChange = { isChecked ->
                    darkMode = isChecked
                    themeViewModel.setTheme(isChecked)

                    // Guardar localmente
                    sharedPreferences.edit().putString("user_theme", if (isChecked) "dark" else "light").apply()

                    // Enviar a backend
                    val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
                    val userId = prefs.getString("userId", null)
                    if (userId != null) {
                        val apiService = ApiClient.getApiService()
                        val themeRequest =
                            ApiService.ThemeRequest(if (isChecked) "dark" else "light")
                        apiService.updateTheme(userId, themeRequest).enqueue(object : Callback<Void> {
                            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                Log.d("AJUSTES", "🌗 Tema actualizado correctamente")
                            }

                            override fun onFailure(call: Call<Void>, t: Throwable) {
                                Log.e("AJUSTES", "❌ Error al actualizar tema", t)
                            }
                        })
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 🎯 Perfil predeterminado
            Text("Perfil predeterminado", color = textColor)
            DropdownMenuPerfil(
                selectedOption = perfilPredeterminado,
                onOptionSelected = {
                    perfilPredeterminado = it
                    sharedPreferences.edit().putString("perfil_predeterminado", it).apply()
                    showSnackbar = true
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 🗑️ Botón restablecer datos
            Button(
                onClick = {
                    sharedPreferences.edit().clear().apply()
                    showSnackbar = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Restablecer datos")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 🔐 Botón cerrar sesión
            Button(
                onClick = {
                    sharedPreferences.edit().clear().apply()
                    navController.navigate("login") {
                        popUpTo("main") { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cerrar sesión")
            }

            // 🍞 Snackbar
            if (showSnackbar) {
                LaunchedEffect(snackbarHostState) {
                    snackbarHostState.showSnackbar("Cambios guardados")
                    showSnackbar = false
                }
            }
        }
    }
}

@Composable
fun DropdownMenuPerfil(selectedOption: String, onOptionSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val opciones = listOf("ATENTO", "MEDITATIVO", "EQUILIBRADO")

    Column {
        TextButton(onClick = { expanded = true }) {
            Text(text = selectedOption)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion) },
                    onClick = {
                        onOptionSelected(opcion)
                        expanded = false
                    }
                )
            }
        }
    }
}
