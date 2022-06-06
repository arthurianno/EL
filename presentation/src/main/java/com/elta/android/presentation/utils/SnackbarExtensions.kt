package com.elta.android.presentation.utils

import android.view.View
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import com.elta.android.presentation.R
import com.elta.android.presentation.core.pm.widgets.SnackBarControl
import com.elta.android.presentation.core.ui.snack_bar_view.SnackBarData
import com.google.android.material.snackbar.Snackbar

fun Snackbar.applyTextAppearance(@StyleRes style: Int): Snackbar {
    with(view) {
        val textView = findViewById<TextView>(R.id.snackbar_text)
        textView?.let {
            TextViewCompat.setTextAppearance(it, style)
            it.maxLines = Int.MAX_VALUE
        }
    }
    return this
}

fun Snackbar.applySnackbarHeight(): Snackbar {
    with(view) {
        minimumHeight = view.resources.getDimensionPixelSize(R.dimen.large_button_height)
    }
    return this
}

fun Snackbar.applyTextDrawable(@DrawableRes drawable: Int?): Snackbar {
    drawable?.let { nonNullDrawable ->
        with(view) {
            val textView = findViewById<TextView>(R.id.snackbar_text)
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

fun makeSnackBarWithAction(
    view: View,
    data: SnackBarData,
    control: SnackBarControl<SnackBarData>
): Snackbar =
    makeSnackBar(checkNotNull(view), data)
        .also { snackBar -> snackBar.duration = data.duration ?: Snackbar.LENGTH_INDEFINITE }
        .setActionTextColor(ContextCompat.getColor(view.context, R.color.shade_blue))
        .setAction(data.button) { control.sendResult() }
