package com.elta.android.presentation.theme

import androidx.compose.ui.graphics.Brush

data class EltaBrash(
    val downButton: Brush
)

internal val eltaBrash = EltaBrash(
    downButton = Brush.horizontalGradient(
        listOf(gGreenA, gGreenB)
    )
)
