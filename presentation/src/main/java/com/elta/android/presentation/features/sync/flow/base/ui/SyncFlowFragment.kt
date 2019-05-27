package com.elta.android.presentation.features.sync.flow.base.ui

import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseFlowFragment
import com.elta.android.presentation.features.sync.flow.base.pm.SyncFlowPm

abstract class SyncFlowFragment<T : SyncFlowPm> : BaseFlowFragment<T>() {

    override val screenLayout: Int = R.layout.layout_container
}
