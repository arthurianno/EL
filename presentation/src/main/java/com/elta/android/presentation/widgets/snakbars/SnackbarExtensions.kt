package com.elta.android.presentation.widgets.snakbars

import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.annotation.StyleRes
import android.support.design.widget.Snackbar
import android.support.v4.widget.TextViewCompat
import android.view.View
import android.widget.TextView
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.snack_bar_view.SnackBarData

fun Snackbar.applyTextAppearance(@StyleRes style: Int): Snackbar {
    with(view) {
        val textView = findViewById<TextView>(android.support.design.R.id.snackbar_text)
        textView?.let {
            TextViewCompat.setTextAppearance(it, style)
            it.maxLines = Int.MAX_VALUE
        }
    }
    return this
}

fun Snackbar.applySnackbarHeight(): Snackbar {
    with(view) {
       layoutParams?.height = view.resources.getDimensionPixelSize(R.dimen.large_button_height)
    }
    return this
}

fun Snackbar.applyTextDrawable(@DrawableRes drawable: Int?): Snackbar {
    drawable?.let { nonNullDrawable ->
        with(view) {
            val textView = findViewById<TextView>(android.support.design.R.id.snackbar_text)
            textView?.let {
                it.compoundDrawablePadding = paddingLeft
                it.setPadding(0, 0, 0, 0)
                it.setCompoundDrawablesWithIntrinsicBounds(nonNullDrawable, 0, 0, 0)
            }
        }
    }
    return this
}

fun Snackbar.applyBackgroundColor(@ColorRes color: Int): Snackbar {
    with(view) {
        setBackgroundResource(color)
    }
    return this
}

fun makeSnackBar(view: View, data: SnackBarData): Snackbar =
    Snackbar.make(view, data.message, Snackbar.LENGTH_SHORT)
        .applySnackbarHeight()
        .applyBackgroundColor(R.color.black)
        .applyTextAppearance(R.style.SnackbarText)
        .applyTextDrawable(data.icon)