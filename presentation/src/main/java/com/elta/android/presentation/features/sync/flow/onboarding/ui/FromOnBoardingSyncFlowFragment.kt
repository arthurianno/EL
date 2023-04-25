package com.elta.android.presentation.features.sync.flow.onboarding.ui

import com.elta.android.presentation.features.sync.flow.base.ui.SyncFlowFragment
import com.elta.android.presentation.features.sync.flow.onboarding.pm.FromOnBoardingSyncFlowPm

@Deprecated("Данный flow помечен на удаление")
class FromOnBoardingSyncFlowFragment : SyncFlowFragment<FromOnBoardingSyncFlowPm>() {

    override val classToken = FromOnBoardingSyncFlowPm::class.java

    companion object {
        fun newInstance() = FromOnBoardingSyncFlowFragment()
    }
}
