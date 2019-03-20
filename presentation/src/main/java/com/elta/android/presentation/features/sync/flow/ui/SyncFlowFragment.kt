package com.elta.android.presentation.features.sync.flow.ui

import android.os.Bundle
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseFlowFragment
import com.elta.android.presentation.features.sync.flow.pm.SyncFlowPm

class SyncFlowFragment : BaseFlowFragment<SyncFlowPm>() {

    override val screenLayout: Int = R.layout.layout_container
    override val classToken: Class<SyncFlowPm> = SyncFlowPm::class.java

    companion object {
        fun newInstance(): SyncFlowFragment {
            return SyncFlowFragment().apply {
                arguments = Bundle().apply {
                }
            }
        }
    }
}
