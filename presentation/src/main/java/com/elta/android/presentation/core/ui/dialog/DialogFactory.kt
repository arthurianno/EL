package com.elta.android.presentation.core.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.support.v4.app.Fragment
import com.afollestad.materialdialogs.MaterialDialog
import me.dmdev.rxpm.widget.DialogControl

fun createDialog(
    fragment: Fragment,
    dc: DialogControl<DialogData, DialogResult>,
    data: DialogData
) = createDialog(checkNotNull(fragment.activity), dc, data)

fun createDialog(
    activity: Activity,
    dc: DialogControl<DialogData, DialogResult>,
    data: DialogData
): Dialog =
    MaterialDialog.Builder(activity)
        .cancelable(false)
        .title(data.title)
        .content(data.message)
        .buttons(dc, data)
        .build()

fun MaterialDialog.Builder.buttons(
    dc: DialogControl<DialogData, DialogResult>,
    data: DialogData
): MaterialDialog.Builder =
    this.also { builder ->
        data.negative?.let { text ->
            builder
                .negativeText(text)
                .onNegative { _, _ -> dc.sendResult(DialogResult.NEGATIVE) }
        }
        data.positive?.let { text ->
            builder
                .positiveText(text)
                .onPositive { _, _ -> dc.sendResult(DialogResult.POSITIVE) }
        }
    }