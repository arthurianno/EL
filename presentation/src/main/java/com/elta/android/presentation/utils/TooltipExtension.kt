package com.elta.android.presentation.utils

import android.support.annotation.StyleRes
import android.view.View
import com.elta.android.presentation.R
import com.tooltip.Tooltip

fun View.showTooltip(
    text: String,
    @StyleRes style: Int = R.style.TooltipStyle
): Tooltip {
    return Tooltip.Builder(this, style)
        .setText(text)
        .show()
}