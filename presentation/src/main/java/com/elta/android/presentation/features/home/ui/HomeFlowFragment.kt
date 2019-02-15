package com.elta.android.presentation.features.home.ui

import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseFlowFragment
import com.elta.android.presentation.features.home.pm.HomeFlowPm

class HomeFlowFragment : BaseFlowFragment<HomeFlowPm>() {

    override val screenLayout: Int = R.layout.fragment_home_flow
    override val classToken: Class<HomeFlowPm> = HomeFlowPm::class.java

    companion object {
        fun newInstance() = HomeFlowFragment()
    }
}
