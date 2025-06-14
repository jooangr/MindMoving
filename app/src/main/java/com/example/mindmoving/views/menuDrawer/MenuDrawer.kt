package com.example.mindmoving.views.menuDrawer


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.mindmoving.R
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ExitToApp

import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.ui.platform.LocalContext
import com.example.mindmoving.utils.SessionManager



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout(navController: NavHostController, content: @Composable (PaddingValues) -> Unit) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var mostrarDialogoCerrarSesion by remember { mutableStateOf(false) }
    val context = LocalContext.current

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet {
                Text("Menú", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
                Divider()

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") },
                    selected = false,
                    onClick = {
                        navController.navigate("menu")
                        scope.launch { drawerState.close() }
                    }
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.History, contentDescription = "Historial") },
                    label = { Text("Historial de sesiones") },
                    selected = false,
                    onClick = {
                        navController.navigate("historial_sesiones")
                        scope.launch { drawerState.close() }
                    }
                )


                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Ajustes") },
                    label = { Text("Ajustes") },
                    selected = false,
                    onClick = {
                        navController.navigate("ajustes_screen") //ruta por crear
                        scope.launch { drawerState.close() }
                    }
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Help, contentDescription = "Ayuda") },
                    label = { Text("Ayuda") },
                    selected = false,
                    onClick = {
                        navController.navigate("ayuda_screen") // puedes hacer una pantalla básica
                        scope.launch { drawerState.close() }
                    }
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar sesión") },
                    label = { Text("Cerrar sesión") },
                    selected = false,
                    onClick = {
                        mostrarDialogoCerrarSesion = true
                    }
                )
            }

            if (mostrarDialogoCerrarSesion) {
                AlertDialog(
                    onDismissRequest = { mostrarDialogoCerrarSesion = false },
                    title = { Text("Cerrar sesión") },
                    text = { Text("¿Estás seguro de que quieres cerrar sesión?") },
                    confirmButton = {
                        TextButton(onClick = {
                            SessionManager.logout(context)
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                            scope.launch { drawerState.close() }
                            mostrarDialogoCerrarSesion = false
                        }) {
                            Text("Sí")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            mostrarDialogoCerrarSesion = false
                        }) {
                            Text("Cancelar")
                        }
                    }
                )
            }



        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Image(
                            painter = painterResource(id = R.drawable.logo_mindmoving),
                            contentDescription = "Logo",
                            modifier = Modifier.height(40.dp)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    },
                    actions = {
                        var expanded by remember { mutableStateOf(false) }

                        Box(contentAlignment = Alignment.TopEnd) {
                            IconButton(onClick = { expanded = true }) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Menú usuario",
                                    tint = MaterialTheme.colorScheme.onSurface

                                )
                            }

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Ver perfil") },
                                    onClick = {
                                        expanded = false
                                        navController.navigate("editar_perfil")

                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Opciones de calibración") },
                                    onClick = {
                                        expanded = false
                                        navController.navigate("calibracion_menu")
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Cerrar sesión") },
                                    onClick = {
                                        expanded = false
                                        navController.navigate("login") {
                                            popUpTo(0) // limpia el backstack
                                        }
                                    }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurface
                    )

                )

            }
        ) { padding ->
            content(padding)
        }
    }
}
