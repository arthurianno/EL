package com.elta.android.presentation.features.auth.ui

import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import com.elta.android.presentation.R

internal object AuthTypography {
    private val base = TextStyle(
        fontFamily = FontFamily(Font(R.font.roboto_regular)),
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None)
    )
    val title = base.copy(
        fontFamily = FontFamily(Font(R.font.gothampro_medium)),
        fontSize = 32.sp,
        lineHeight = 28.sp
    )
    val subtitle = base.copy(fontSize = 17.sp, lineHeight = 19.sp)
    val field = base.copy(fontSize = 15.sp, lineHeight = 20.sp)
    val helper = base.copy(fontSize = 12.sp, lineHeight = 16.sp)
    val toolbar = base.copy(fontSize = 14.sp, lineHeight = 24.sp)
    val button = base.copy(
        fontFamily = FontFamily(Font(R.font.roboto_medium)),
        fontSize = 16.sp,
        lineHeight = 24.sp
    )
}
