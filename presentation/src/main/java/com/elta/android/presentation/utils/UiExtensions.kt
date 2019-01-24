package com.elta.android.presentation.utils

import android.widget.ImageView
import com.elta.android.presentation.R

fun ImageView.toggleSecureIcon(isSecure: Boolean) {
    setImageResource(when (isSecure) {
        true -> R.drawable.ic_show_password
        else -> R.drawable.ic_password_hide
    })
}
