package com.elta.android.presentation.features.shops.flow.ui

import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseFlowFragment
import com.elta.android.presentation.features.shops.flow.pm.ShopsFlowPm

class ShopsFlowFragment : BaseFlowFragment<ShopsFlowPm>() {

    override val screenLayout: Int = R.layout.layout_container
    override val classToken: Class<ShopsFlowPm> = ShopsFlowPm::class.java

    companion object {
        fun newInstance() = ShopsFlowFragment()
    }
}
