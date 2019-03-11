package com.elta.android.presentation

import com.elta.android.presentation.core.ui.dialog.DialogData
import com.nullgr.core.resources.ResourceProvider

sealed class Dialogs: DialogData {

    data class EventExit(
        val resources: ResourceProvider,
        override val title: String = resources.getString(R.string.event_form_exit_dialog_title),
        override val message: String = resources.getString(R.string.event_form_exit_dialog_body),
        override val negative: String = resources.getString(R.string.event_form_exit_dialog_cancel_button),
        override val positive: String = resources.getString(R.string.event_form_exit_dialog_confirm_button)
    ): Dialogs()
}