package com.elta.android.presentation.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

data class EltaBrash(
    val background: Brush
)

internal val eltaBrash = EltaBrash(
    background = Brush.verticalGradient(
        listOf(Color.Black, Color.White)
    )
)
