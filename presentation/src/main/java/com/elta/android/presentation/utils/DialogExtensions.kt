@file:Suppress("NOTHING_TO_INLINE")

package com.elta.android.presentation.utils

import com.afollestad.materialdialogs.MaterialDialog
import io.reactivex.functions.Consumer

inline fun MaterialDialog.visibility(): Consumer<in Boolean> = Consumer {
    when (it) {
        true -> show()
        else -> dismiss()
    }
}