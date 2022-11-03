package com.elta.android.presentation.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class EltaDimens(
    val verySmallDim: Dp,
    val smallDim: Dp,
    val halfMediumDim: Dp,
    val mediumDim: Dp,
    val bigDim: Dp,
    val veryBugDim: Dp,
    val contentPadding: Dp,
    val downButtonHeight: Dp,
    val lastWordVertical: Dp,
    val dishCardVerticalSpace: Dp,
    val dishNameSpace: Dp,
    val borderWidth: Dp
)

internal val eltaDimens = EltaDimens(
    verySmallDim = 4.dp,
    smallDim = 8.dp,
    halfMediumDim = 12.dp,
    mediumDim = 16.dp,
    bigDim = 24.dp,
    veryBugDim = 32.dp,
    contentPadding = 16.dp,
    downButtonHeight = 52.dp,
    lastWordVertical = 12.dp,
    dishCardVerticalSpace = 8.dp,
    dishNameSpace = 12.dp,
    borderWidth = 1.dp
)
