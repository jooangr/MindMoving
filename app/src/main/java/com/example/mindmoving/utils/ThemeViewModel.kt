package com.example.mindmoving.utils

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.compositionLocalOf

/**
 * Aqui se gestiona el estado del tema oscuro o claro de la app de forma reactiva
 */
class ThemeViewModel : ViewModel() {
    val isDarkTheme = mutableStateOf(false)

    fun toggleTheme() {
        isDarkTheme.value = !isDarkTheme.value
    }

    fun setTheme(dark: Boolean) {
        isDarkTheme.value = dark
    }



    val LocalDarkThemeOverride = compositionLocalOf { false }

}