package com.elta.android.presentation.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

data class EltaDimens(
    // Base Dims
    val verySmallDim: Dp,
    val smallDim: Dp,
    val halfMediumDim: Dp,
    val mediumDim: Dp,
    val bigDim: Dp,
    val veryBugDim: Dp,
    val contentPadding: Dp,
    val downButtonHeight: Dp,
    val textFieldPopupOffset: DpOffset,
    val borderWidth: Dp,
    // Calculator Dims
    val lastWordVertical: Dp,
    val dishCardVerticalSpace: Dp,
    val dishNameSpace: Dp,
    val dishCardTextEndPadding: Dp,
    // Dish Dims
    val headerBottomDim: Dp,
    val dishCardHeight: Dp,
    val dishHeaderTitle: PaddingValues,
    val xeValueCard: PaddingValues,
    val xeValue: PaddingValues,
    val verifyIconSize: Dp,
    val dishChars: PaddingValues
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
    textFieldPopupOffset = DpOffset(16.dp, 0.dp),
    borderWidth = 1.dp,
    lastWordVertical = 12.dp,
    dishCardVerticalSpace = 8.dp,
    dishNameSpace = 12.dp,
    dishCardTextEndPadding = 60.dp,
    headerBottomDim = 36.dp,
    dishCardHeight = 488.dp,
    dishHeaderTitle = PaddingValues(top = 12.dp, bottom = 56.dp, start = 16.dp, end = 86.dp),
    xeValueCard = PaddingValues(bottom = 52.dp, end = 16.dp),
    xeValue = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
    verifyIconSize = 20.dp,
    dishChars = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
)
