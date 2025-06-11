package com.example.mindmoving.views.menuDrawer.viewsMenuDrawer

import android.app.Activity
import androidx.compose.ui.Alignment
import android.content.Context
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




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen(navController: NavHostController) {
    val gradientBrush = Brush.verticalGradient(
        //colors = listOf(Color(0xFF3F51B5), Color(0xFF2196F3)) // Siempre modo oscuro
        colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
    )

    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("mindmoving_prefs", Context.MODE_PRIVATE)

    var darkMode by remember { mutableStateOf(false) }
    var perfilPredeterminado by remember {
        mutableStateOf(sharedPreferences.getString("perfil_predeterminado", "EQUILIBRADO") ?: "EQUILIBRADO")
    }
    var showSnackbar by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

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
                .background(brush = gradientBrush)
                .padding(16.dp)
                .padding(paddingValues)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Modo oscuro", color = Color.White)
                Switch(
                    checked = darkMode,
                    onCheckedChange = { darkMode = it }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Perfil predeterminado", color = Color.White)
            DropdownMenuPerfil(
                selectedOption = perfilPredeterminado,
                onOptionSelected = {
                    perfilPredeterminado = it
                    sharedPreferences.edit().putString("perfil_predeterminado", it).apply()
                    showSnackbar = true
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

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
