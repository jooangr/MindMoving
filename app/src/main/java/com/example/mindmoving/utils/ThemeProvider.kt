package com.example.mindmoving.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.lifecycle.viewmodel.compose.viewModel

val LocalThemeViewModel = compositionLocalOf<ThemeViewModel> {
    error("ThemeViewModel not provided")
}

/**
 * Permite inyectar y acceder al ThemeViewModel desde cualquier Composable de forma limpia y sin necesidad de pasarlo como parámetro.
 */
@Composable
fun ProvideThemeViewModel(content: @Composable () -> Unit) {
    val themeViewModel: ThemeViewModel = viewModel()
    androidx.compose.runtime.CompositionLocalProvider(
        LocalThemeViewModel provides themeViewModel,
        content = content
    )
}
