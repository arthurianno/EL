package com.elta.android.presentation.theme // ktlint-disable filename

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun EltaTheme(
    content: @Composable () -> Unit
) {
    LocalContentProvider() {
        MaterialTheme(
            colors = materialThemeColors,
            typography = materialThemeTypography,
            shapes = materialThemeShapes,
            content = content
        )
    }
}
