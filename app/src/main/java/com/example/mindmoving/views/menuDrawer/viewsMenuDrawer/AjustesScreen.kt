package com.example.mindmoving.views.menuDrawer.viewsMenuDrawer

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
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

    // ViewModel que gestiona el estado del tema (oscuro o claro)
    val themeViewModel = LocalThemeViewModel.current
    var darkMode by remember { mutableStateOf(themeViewModel.isDarkTheme.value) }

    // Estado para guardar el tipo de perfil calibrado (desde SharedPreferences)
    val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
    val perfilTipoState = remember { mutableStateOf<String?>(null) }

    // Carga inicial del perfil calibrado
    LaunchedEffect(Unit) {
        perfilTipoState.value = prefs.getString("perfil_tipo", null)
    }

    // Observer para volver a cargar datos si se regresa a la pantalla
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                perfilTipoState.value = prefs.getString("perfil_tipo", null)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Estado y controlador para mostrar snackbar
    var showSnackbar by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground

    // Estructura general de la pantalla
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
            // Opción para cambiar el modo oscuro
            Text("Modo oscuro", color = textColor)
            Switch(
                checked = darkMode,
                onCheckedChange = { isChecked ->
                    darkMode = isChecked
                    themeViewModel.setTheme(isChecked)

                    // Guardar preferencia en almacenamiento local
                    sharedPreferences.edit()
                        .putString("user_theme", if (isChecked) "dark" else "light")
                        .apply()

                    // Enviar preferencia al backend si hay sesión
                    val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
                    val userId = prefs.getString("userId", null)
                    if (userId != null) {
                        val apiService = ApiClient.getApiService()
                        val themeRequest = ApiService.ThemeRequest(
                            if (isChecked) "dark" else "light"
                        )
                        apiService.updateTheme(userId, themeRequest).enqueue(object : Callback<Void> {
                            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                Log.d("AJUSTES", "Tema actualizado correctamente")
                            }

                            override fun onFailure(call: Call<Void>, t: Throwable) {
                                Log.e("AJUSTES", "Error al actualizar tema", t)
                            }
                        })
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Mostrar tipo de perfil calibrado, si existe
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Perfil calibrado: ${perfilTipoState.value ?: "No asignado"}",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Mostrar snackbar si está activado
            if (showSnackbar) {
                LaunchedEffect(snackbarHostState) {
                    snackbarHostState.showSnackbar("Cambios guardados")
                    showSnackbar = false
                }
            }
        }
    }
}
