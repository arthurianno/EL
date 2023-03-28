package com.elta.android.presentation.core.ui.fragment // ktlint-disable filename

import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment

fun Fragment.addOnBackPressedCallback(
    enabled: Boolean = true,
    onBackPressed: OnBackPressedCallback.() -> Unit,
): OnBackPressedCallback {
    return object : OnBackPressedCallback(enabled) {
        override fun handleOnBackPressed() = onBackPressed()
    }.also { requireActivity().onBackPressedDispatcher.addCallback(this, it) }
}
