package com.elta.android.presentation.widgets.selector.model

import android.graphics.drawable.Drawable

data class SelectorOption(
    val text: String?,
    val icon: Drawable? = null,
    val meta: Any? = null
)
