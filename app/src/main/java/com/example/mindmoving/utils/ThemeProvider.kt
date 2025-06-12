package com.example.mindmoving.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.lifecycle.viewmodel.compose.viewModel

val LocalThemeViewModel = compositionLocalOf<ThemeViewModel> {
    error("ThemeViewModel not provided")
}

@Composable
fun ProvideThemeViewModel(content: @Composable () -> Unit) {
    val themeViewModel: ThemeViewModel = viewModel()
    androidx.compose.runtime.CompositionLocalProvider(
        LocalThemeViewModel provides themeViewModel,
        content = content
    )
}
