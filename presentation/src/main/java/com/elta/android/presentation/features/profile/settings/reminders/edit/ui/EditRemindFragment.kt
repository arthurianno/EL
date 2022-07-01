package com.elta.android.presentation.features.profile.settings.reminders.edit.ui

import android.os.Bundle
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.dialog.createDialog
import com.elta.android.presentation.features.profile.settings.reminders.base.ui.BaseRemindFragment
import com.elta.android.presentation.features.profile.settings.reminders.edit.pm.EditRemindPm
import com.elta.android.presentation.utils.bundle
import com.jakewharton.rxbinding2.view.clicks
import me.dmdev.rxpm.bindTo
import me.dmdev.rxpm.widget.bindTo

class EditRemindFragment : BaseRemindFragment<EditRemindPm>() {

    override val classToken: Class<EditRemindPm> = EditRemindPm::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presentationModel.setReminderId(arguments?.getString(REMINDER_ID).orEmpty())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.toolbarTitleView.text = getString(R.string.profile_reminders_edit_title)
            toolbar.menuButtonView.text = getString(R.string.profile_reminders_delete)
            formSaveButtonView.text = getString(R.string.profile_reminders_save_changes)
        }
    }

    override fun onBindPresentationModel(pm: EditRemindPm) {
        super.onBindPresentationModel(pm)
        binding.toolbar.menuButtonView.clicks().bindTo(pm.deleteRemindAction)
        pm.defaultScheduleState.bindTo { binding.scheduleView.setTitle(it) }
        pm.deleteRemindDialogControl.bindTo { data, dc -> createDialog(this, dc, data) }
    }

    companion object {
        fun newInstance(remindId: String) = EditRemindFragment().apply {
            arguments = bundle(REMINDER_ID to remindId)
        }

        private const val REMINDER_ID = "reminder_id"
    }
}
