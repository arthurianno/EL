package com.elta.android.presentation.features.sync.connect.widgets

import android.graphics.Color
import android.view.Window
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.core.view.WindowCompat

@Suppress("DEPRECATION")
@Composable
internal fun ScannerSystemBars(window: Window) {
    DisposableEffect(window) {
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        val previousLightStatusBar = controller.isAppearanceLightStatusBars
        val previousStatusBarColor = window.statusBarColor
        controller.isAppearanceLightStatusBars = false
        window.statusBarColor = Color.BLACK
        onDispose {
            controller.isAppearanceLightStatusBars = previousLightStatusBar
            window.statusBarColor = previousStatusBarColor
        }
    }
}
