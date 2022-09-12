package com.elta.android.presentation.features.sync.flow.base.ui

import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseFlowFragment
import com.elta.android.presentation.databinding.LayoutContainerBinding
import com.elta.android.presentation.features.sync.flow.base.pm.SyncFlowPm

abstract class SyncFlowFragment<T : SyncFlowPm> :
    BaseFlowFragment<T, LayoutContainerBinding>(LayoutContainerBinding::inflate) {

    override val screenLayout: Int = R.layout.layout_container
}
