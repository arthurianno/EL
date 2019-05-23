package com.elta.android.presentation.features.profile.settings.reminders.edit.ui

import android.os.Bundle
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.dialog.createDialog
import com.elta.android.presentation.features.profile.settings.reminders.base.ui.BaseRemindFragment
import com.elta.android.presentation.features.profile.settings.reminders.edit.pm.EditRemindPm
import com.elta.android.presentation.utils.bundle
import com.jakewharton.rxbinding2.view.clicks
import kotlinx.android.synthetic.main.fragment_reminder_form.*
import kotlinx.android.synthetic.main.layout_toolbar.*

class EditRemindFragment : BaseRemindFragment<EditRemindPm>() {

    override val classToken: Class<EditRemindPm> = EditRemindPm::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presentationModel.setReminderId(checkNotNull(arguments)[REMINDER_ID] as String)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbarTitleView.text = getString(R.string.profile_reminders_edit_title)
        menuButtonView.text = getString(R.string.profile_reminders_delete)
        formSaveButtonView.text = getString(R.string.profile_reminders_save_changes)
    }

    override fun onBindPresentationModel(pm: EditRemindPm) {
        super.onBindPresentationModel(pm)
        menuButtonView.clicks().bindTo(pm.deleteRemindAction)
        pm.defaultScheduleState.bindTo { scheduleView.setTitle(it) }
        pm.deleteRemindDialogControl.bindTo { data, dc -> createDialog(this, dc, data) }
    }

    companion object {
        fun newInstance(remindId: String) = EditRemindFragment().apply {
            arguments = bundle(REMINDER_ID to remindId)
        }

        private const val REMINDER_ID = "reminder_id"
    }
}