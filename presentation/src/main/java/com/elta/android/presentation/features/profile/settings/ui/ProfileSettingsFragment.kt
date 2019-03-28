package com.elta.android.presentation.features.profile.settings.ui

import android.os.Bundle
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseListFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.features.profile.settings.pm.ProfileSettingsPm
import kotlinx.android.synthetic.main.layout_toolbar.*

class ProfileSettingsFragment : BaseListFragment<ProfileSettingsPm>() {

    override val screenLayout: Int = R.layout.fragment_profile_settings
    override val classToken: Class<ProfileSettingsPm> = ProfileSettingsPm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbarTitleView.text = getString(R.string.profile_settings)
    }

    override fun onBindPresentationModel(pm: ProfileSettingsPm) {
        super.onBindPresentationModel(pm)
        bindProgressDialog(pm)
    }

    companion object {
        fun newInstance() = ProfileSettingsFragment()
    }
}