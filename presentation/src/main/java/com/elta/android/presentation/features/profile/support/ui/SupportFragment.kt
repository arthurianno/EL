package com.elta.android.presentation.features.profile.support.ui

import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseListFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.databinding.FragmentSupportBinding
import com.elta.android.presentation.features.profile.support.pm.SupportPm

class SupportFragment :
    BaseListFragment<SupportPm, FragmentSupportBinding>(FragmentSupportBinding::inflate) {

    override val screenLayout: Int = R.layout.fragment_support
    override val classToken: Class<SupportPm> = SupportPm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    companion object {
        fun newInstance() = SupportFragment()
    }
}
