package com.elta.android.presentation.features.profile.main.ui

import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseListFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.features.profile.main.pm.MainProfilePm

class MainProfileFragment : BaseListFragment<MainProfilePm>() {

    override val screenLayout = R.layout.fragment_main_profile
    override val classToken: Class<MainProfilePm> = MainProfilePm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    override val backgroundColor = R.color.pale_gray

    override fun onBindPresentationModel(pm: MainProfilePm) {
        super.onBindPresentationModel(pm)
    }

    companion object {
        fun newInstance() = MainProfileFragment()
    }
}