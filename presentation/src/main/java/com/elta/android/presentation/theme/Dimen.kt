package com.elta.android.presentation.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class EltaDimens(
    val contentPadding: Dp,
    val downButtonHeight: Dp
)

internal val eltaDimens = EltaDimens(
    contentPadding = 16.dp,
    downButtonHeight = 52.dp
)
