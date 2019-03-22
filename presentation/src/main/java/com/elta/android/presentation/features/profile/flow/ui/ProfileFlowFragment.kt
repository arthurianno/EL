package com.elta.android.presentation.features.profile.flow.ui

import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseFlowFragment
import com.elta.android.presentation.features.profile.flow.pm.ProfileFlowPm

class ProfileFlowFragment : BaseFlowFragment<ProfileFlowPm>() {

    override val screenLayout = R.layout.layout_container
    override val classToken: Class<ProfileFlowPm> = ProfileFlowPm::class.java

    companion object {
        fun newInstance() = ProfileFlowFragment()
    }
}