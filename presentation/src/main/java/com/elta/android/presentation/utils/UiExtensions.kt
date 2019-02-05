package com.elta.android.presentation.utils

import android.content.Context
import android.support.v4.view.ViewCompat
import android.view.View
import android.widget.ImageView
import com.elta.android.presentation.R

fun ImageView.toggleSecureIcon(isSecure: Boolean) {
    setImageResource(when (isSecure) {
        true -> R.drawable.ic_show_password
        else -> R.drawable.ic_password_hide
    })
}

fun View.applyInsetsToContentView(fitsSystemWindows: Boolean) {
    this.fitsSystemWindows = fitsSystemWindows
    ViewCompat.requestApplyInsets(this)
}

fun View.applySystemWindowPadding() {
    this.y = getStatusBarHeight(this.context).toFloat()
}

private fun getStatusBarHeight(context: Context): Int {
    val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
    return context.resources.getDimensionPixelSize(resourceId)
}