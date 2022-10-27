package com.elta.android.presentation.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import com.elta.android.presentation.R

private val appBaseFontMedium = FontFamily(Font(R.font.roboto_medium))
private val appBaseFontRegular = FontFamily(Font(R.font.roboto_regular))
private val appBaseFontBold = FontFamily(Font(R.font.roboto_bold))
private val appBaseFontLight = FontFamily(Font(R.font.roboto_light))

private val title1 = TextStyle(
    fontFamily = appBaseFontBold,
    fontSize = 15.sp
)

private val title2 = TextStyle(
    fontFamily = appBaseFontMedium,
    fontSize = 15.sp
)

private val title3 = TextStyle(
    fontFamily = appBaseFontMedium,
    fontSize = 14.sp
)

private val body1 = TextStyle(
    fontFamily = appBaseFontRegular,
    fontSize = 14.sp
)

private val body2 = TextStyle(
    fontFamily = appBaseFontRegular,
    fontSize = 15.sp
)

private val subtitle1 = TextStyle(
    fontFamily = appBaseFontRegular,
    fontSize = 15.sp
)

private val h1 = TextStyle(
    fontFamily = appBaseFontBold,
    fontSize = 24.sp
)

private val h2 = TextStyle(
    fontFamily = appBaseFontMedium,
    fontSize = 20.sp
)

private val caption1 = TextStyle(
    fontFamily = appBaseFontRegular,
    fontSize = 12.sp
)

private val caption2 = TextStyle(
    fontFamily = appBaseFontRegular,
    fontSize = 12.sp
)

private val snackBar = TextStyle(
    fontFamily = appBaseFontRegular,
    fontSize = 15.sp
)

private val toolBar = TextStyle(
    fontFamily = appBaseFontRegular,
    fontSize = 14.sp
)

private val buttonLargeText = TextStyle(
    fontFamily = appBaseFontMedium,
    fontSize = 15.sp
)

private val buttonMenuText = TextStyle(
    fontFamily = appBaseFontRegular,
    fontSize = 12.sp
)

private val textStyle2 = TextStyle(
    fontFamily = appBaseFontMedium,
    fontSize = 12.sp
)

data class EltaTypes(
    val title1: TextStyle,
    val title2: TextStyle,
    val title3: TextStyle,
    val body1: TextStyle,
    val body2: TextStyle,
    val subtitle1: TextStyle,
    val h1: TextStyle,
    val h2: TextStyle,
    val caption1: TextStyle,
    val caption2: TextStyle,
    val snackBar: TextStyle,
    val toolBar: TextStyle,
    val buttonLargeText: TextStyle,
    val buttonSmallText: TextStyle,
    val buttonMenuText: TextStyle,
    val textStyle2: TextStyle
)

internal val eltaTypes = EltaTypes(
    title1 = title1,
    title2 = title2,
    title3 = title3,
    body1 = body1,
    body2 = body2,
    subtitle1 = subtitle1,
    h1 = h1,
    h2 = h2,
    caption1 = caption1,
    caption2 = caption2,
    snackBar = snackBar,
    toolBar = toolBar,
    buttonLargeText = buttonMenuText,
    buttonSmallText = buttonMenuText,
    buttonMenuText = buttonLargeText,
    textStyle2 = textStyle2
)

val materialThemeTypography = Typography(
    h1 = h1,
    h2 = h2,
//    h3 = TextStyle(),
//    h4 = TextStyle(),
//    h5 = TextStyle(),
//    h6 = TextStyle(),
    subtitle1 = subtitle1,
//    subtitle2 = TextStyle(),
    body1 = body1,
    body2 = body2,
    button = buttonLargeText,
//    overline = TextStyle(),
    caption = caption1
)
