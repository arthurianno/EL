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
    val highEnd: Color,
    val normalBadge: Color,
    val highBadge: Color,
    val lowBadge: Color
)

object NewDesignPaletteController {
    var activePalette by mutableStateOf(NewDesignPalette.B)
        private set

    val colors: NewDesignPaletteColors
        get() = when (activePalette) {
            NewDesignPalette.A -> NewDesignPaletteColors(
                lowStart = Color(0xFFE64B35),
                lowEnd = Color(0xFFD43B29),
                normalStart = Color(0xFF29B7A2),
                normalEnd = Color(0xFF26AD9C),
                highStart = Color(0xFFF39A22),
                highEnd = Color(0xFFE78017),
                normalBadge = Color(0xFF77D5B4),
                highBadge = Color(0xFFFFC56D),
                lowBadge = Color(0xFFF97E71)
            )
            NewDesignPalette.B -> NewDesignPaletteColors(
                lowStart = Color(0xFFD93B17),
                lowEnd = Color(0xFFAF2A2A),
                normalStart = Color(0xFF3AE39D),
                normalEnd = Color(0xFF26A69A),
                highStart = Color(0xFFFCC30D),
                highEnd = Color(0xFFDF7122),
                normalBadge = Color(0xFF2DB799),
                highBadge = Color(0xFFE47F1F),
                lowBadge = Color(0xFFAF2A2A)
            )
        }

    fun select(palette: NewDesignPalette) {
        activePalette = palette
    }
}
