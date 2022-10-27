package com.elta.android.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Shapes
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

val materialThemeShapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(0.dp)
)

data class EltaShapes(
    val card: Shape
)

internal val eltaShapes = EltaShapes(
    card = RoundedCornerShape(16.dp)
)
