@file:Suppress("NOTHING_TO_INLINE")

package com.elta.android.presentation.utils

import android.support.v4.app.FragmentManager
import com.afollestad.materialdialogs.MaterialDialog
import com.elta.android.presentation.widgets.dialogs.ProgressDialog
import com.nullgr.core.ui.fragments.showDialog
import io.reactivex.functions.Consumer

inline fun MaterialDialog.visibility(): Consumer<in Boolean> = Consumer {
    when (it) {
        true -> show()
        else -> dismiss()
    }
}

inline fun ProgressDialog.visibility(fragmentManager: FragmentManager): Consumer<in Boolean> = Consumer {
    when (it) {
        true -> fragmentManager.showDialog(this)
        else -> dismissAllowingStateLoss()
    }
}