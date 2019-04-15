package com.elta.android.presentation.features.profile.settings.reminders.all.ui

import android.os.Bundle
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseListFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.features.profile.settings.reminders.all.pm.RemindersPm
import kotlinx.android.synthetic.main.layout_toolbar.*

class RemindersFragment : BaseListFragment<RemindersPm>() {

    override val screenLayout: Int = R.layout.fragment_reminders
    override val classToken: Class<RemindersPm> = RemindersPm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        menuButtonView.text = getString(R.string.profile_reminders_create_new)
    }

    override fun onBindPresentationModel(pm: RemindersPm) {
        super.onBindPresentationModel(pm)
        bindProgressDialog(pm)
    }

    companion object {
        fun newInstance() = RemindersFragment()
    }
}