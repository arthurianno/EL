package com.elta.android.presentation.features.profile.settings.reminders.create.ui

import android.os.Bundle
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.features.profile.settings.reminders.base.ui.BaseRemindFragment
import com.elta.android.presentation.features.profile.settings.reminders.create.pm.CreateRemindPm

class CreateRemindFragment : BaseRemindFragment<CreateRemindPm>() {

    override val classToken: Class<CreateRemindPm> = CreateRemindPm::class.java

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.toolbarTitleView.text =
            getString(R.string.profile_reminders_create_new_title)
        binding.formSaveButtonView.text = getString(R.string.profile_reminders_create)
    }

    companion object {
        fun newInstance() = CreateRemindFragment()
    }
}
