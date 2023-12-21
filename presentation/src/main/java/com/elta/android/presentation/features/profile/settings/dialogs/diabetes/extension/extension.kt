package com.elta.android.presentation.features.profile.settings.dialogs.diabetes.extension

import android.content.Context
import android.widget.LinearLayout
import android.widget.TextView
import com.elta.android.presentation.R

fun createDiabetesButtonView(context: Context): TextView = TextView(
    context,
    null,
    0,
    R.style.Button_Small_Blue3
).apply {
    layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    ).apply {
        val dp = resources.getDimensionPixelSize(R.dimen.small_padding)
        setMargins(0, dp, 0, dp)
    }
}