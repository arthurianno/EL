package com.elta.android.presentation.features.sync.flow.other.ui

import com.elta.android.presentation.features.sync.flow.base.ui.SyncFlowFragment
import com.elta.android.presentation.features.sync.flow.other.pm.FromOtherSyncFlowPm

@Deprecated("Данный flow помечен на удаление")
class FromOtherSyncFlowFragment : SyncFlowFragment<FromOtherSyncFlowPm>() {

    override val classToken = FromOtherSyncFlowPm::class.java

    companion object {
        fun newInstance() = FromOtherSyncFlowFragment()
    }
}
