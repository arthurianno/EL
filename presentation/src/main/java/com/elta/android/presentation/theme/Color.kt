package com.elta.android.presentation.theme

import androidx.compose.material.lightColors
import androidx.compose.ui.graphics.Color

internal val blue = Color(0xFF245aa7)
internal val red = Color(0xFFd93b17)
internal val shadeRed1 = Color(0xFFd91717)
internal val gPurpurA = Color(0xFF17ead9)
internal val gPurpurB = Color(0xFF6078ea)
internal val gOrangeA = Color(0xFFff8058)
internal val gOrangeB = Color(0xFFff505d)
internal val gGreenA = Color(0xFF43e695)
internal val gGreenB = Color(0xFF3bb2b8)
internal val shadeGPurpurA = Color(0xFF38b7e1)
internal val shadeGGreenA0_16 = Color(0x273ec9a8)
internal val shadeGGreenA = Color(0xFF3ec9a8)
internal val blackBlue = Color(0xFF3d4556)
internal val shadeBlack1 = Color(0xFF878b93)
internal val shadeBlack2 = Color(0xFFbbbfca)
internal val shadeBlack3 = Color(0xFFe3e3e3)
internal val shadeBlack4 = Color(0xFFf5f6fa)
internal val shadeBlue = Color(0xFF1c7aff)
internal val shadeBlue_10 = Color(0x1A1C7AFF)
internal val shadeBlue3 = Color(0xFF4480d4)
internal val shadeBlue3_20 = Color(0x334480d4)
internal val black = Color(0xFF17191f)
internal val shadeBlack0 = Color(0xFF626a7c)
internal val white = Color(0xFFffffff)
internal val shadeGGreen2A = Color(0xFF3ea48d)
internal val shadeGGreen2B = Color(0xFF3b919a)
internal val paleGray = Color(0xFFf7f7f7)

internal val materialThemeColors = lightColors(
    primary = white,
    secondary = shadeGGreenA
)

data class EltaColors(
    val blue: Color,
    val red: Color,
    val shadeRed1: Color,
    val gPurpurA: Color,
    val gPurpurB: Color,
    val gOrangeA: Color,
    val gOrangeB: Color,
    val gGreenA: Color,
    val gGreenB: Color,
    val shadeGPurpurA: Color,
    val shadeGGreenA0_16: Color,
    val shadeGGreenA: Color,
    val blackBlue: Color,
    val shadeBlack1: Color,
    val shadeBlack2: Color,
    val shadeBlack3: Color,
    val shadeBlack4: Color,
    val shadeBlue: Color,
    val shadeBlue_10: Color,
    val shadeBlue3: Color,
    val shadeBlue3_20: Color,
    val black: Color,
    val shadeBlack0: Color,
    val white: Color,
    val shadeGGreen2A: Color,
    val shadeGGreen2B: Color,
    val paleGray: Color
)

internal val eltaColors = EltaColors(
    blue = blue,
    red = red,
    shadeRed1 = shadeRed1,
    gPurpurA = gPurpurA,
    gPurpurB = gPurpurB,
    gOrangeA = gOrangeA,
    gOrangeB = gOrangeB,
    gGreenA = gGreenA,
    gGreenB = gGreenB,
    shadeGPurpurA = shadeGPurpurA,
    shadeGGreenA0_16 = shadeGGreenA0_16,
    shadeGGreenA = shadeGGreenA,
    blackBlue = blackBlue,
    shadeBlack1 = shadeBlack1,
    shadeBlack2 = shadeBlack2,
    shadeBlack3 = shadeBlack3,
    shadeBlack4 = shadeBlack4,
    shadeBlue = shadeBlue,
    shadeBlue_10 = shadeBlue_10,
    shadeBlue3 = shadeBlue3,
    shadeBlue3_20 = shadeBlue3_20,
    black = black,
    shadeBlack0 = shadeBlack0,
    white = white,
    shadeGGreen2A = shadeGGreen2A,
    shadeGGreen2B = shadeGGreen2B,
    paleGray = paleGray
)
