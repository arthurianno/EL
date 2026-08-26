package com.elta.android.presentation.features.main.records.ui.compose

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

enum class NewDesignPalette { A, B }

data class NewDesignPaletteColors(
    val lowStart: Color,
    val lowEnd: Color,
    val normalStart: Color,
    val normalEnd: Color,
    val highStart: Color,
    val highEnd: Color
)

object NewDesignPaletteController {
    var activePalette by mutableStateOf(NewDesignPalette.A)
        private set

    val colors: NewDesignPaletteColors
        get() = when (activePalette) {
            NewDesignPalette.A -> NewDesignPaletteColors(
                lowStart = Color(0xFFFFA669),
                lowEnd = Color(0xFFF2557A),
                normalStart = Color(0xFFCEEA96),
                normalEnd = Color(0xFF1FBFD2),
                highStart = Color(0xFFFFE471),
                highEnd = Color(0xFFFFA669)
            )
            NewDesignPalette.B -> NewDesignPaletteColors(
                lowStart = Color(0xFFD93B17),
                lowEnd = Color(0xFFAF2A2A),
                normalStart = Color(0xFF43E695),
                normalEnd = Color(0xFF26A69A),
                highStart = Color(0xFFFCC30D),
                highEnd = Color(0xFFDF7122)
            )
        }

    fun select(palette: NewDesignPalette) {
        activePalette = palette
    }
}
