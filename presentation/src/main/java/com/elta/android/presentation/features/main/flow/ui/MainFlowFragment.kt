package com.elta.android.presentation.features.main.flow.ui

import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseFlowFragment
import com.elta.android.presentation.databinding.LayoutContainerBinding
import com.elta.android.presentation.features.main.flow.pm.MainFlowPm

class MainFlowFragment :
    BaseFlowFragment<MainFlowPm, LayoutContainerBinding>(LayoutContainerBinding::inflate) {

    override val screenLayout: Int = R.layout.layout_container
    override val classToken: Class<MainFlowPm> = MainFlowPm::class.java

    companion object {
        fun newInstance() = MainFlowFragment()
    }
}
