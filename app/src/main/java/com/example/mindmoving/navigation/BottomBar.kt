package com.example.mindmoving.navigation

import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.mindmoving.R

@Composable
fun BottomNavigationBar (navController: NavController) {

    //antes de crear el bottom bar tenemos que importar la dependencia
    BottomNavigation {
        val navBackStackEntry = navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry.value?.destination
        val iconPrimer = painterResource(id = R.drawable.baseline_menu_24)
        val iconSegun = painterResource(id = R.drawable.baseline_directions_car_24)
        val iconTercer = painterResource(id = R.drawable.baseline_settings_24)

        BottomNavigationItem(
            selected = currentDestination?.route == "cripto",
            onClick = {
                navController.navigate("cripto") {
                    popUpTo("cripto") { inclusive = true }
                    launchSingleTop = true
                }
            },
            icon = {
                androidx.compose.material.Icon(
                    painter = iconPrimer,
                    tint = Color.LightGray,
                    contentDescription = null
                )
            },
            label = {
                androidx.compose.material.Text(text = "Main Menu")
            }
        )

        BottomNavigationItem(
            selected = currentDestination?.route == "noticias",
            onClick = {
                navController.navigate("noticias") {
                    popUpTo("noticias") { inclusive = true }
                    launchSingleTop = true
                }
            },
            icon = {
                androidx.compose.material.Icon(
                    painter = iconSegun,
                    tint = Color.LightGray,
                    contentDescription = null
                )
            },
            label = {
                androidx.compose.material.Text(text = "CarControl")
            }
        )

        BottomNavigationItem(
            selected = currentDestination?.route == "cripto",
            onClick = {
                navController.navigate("cripto") {
                    popUpTo("cripto") { inclusive = true }
                    launchSingleTop = true
                }
            },
            icon = {
                androidx.compose.material.Icon(
                    painter = iconPrimer,
                    tint = Color.LightGray,
                    contentDescription = null
                )
            },
            label = {
                androidx.compose.material.Text(text = "Settings")
            }
        )


    }

}