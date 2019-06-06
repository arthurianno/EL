package com.elta.android.presentation.features.sync.start.onboarding.ui

import com.elta.android.presentation.features.sync.start.base.ui.SyncStartFragment
import com.elta.android.presentation.features.sync.start.onboarding.pm.FromOnBoardingSyncStartPm

class FromOnBoardingSyncStartFragment : SyncStartFragment<FromOnBoardingSyncStartPm>() {

    override val classToken: Class<FromOnBoardingSyncStartPm> = FromOnBoardingSyncStartPm::class.java

    companion object {
        fun newInstance() = FromOnBoardingSyncStartFragment()
    }
}