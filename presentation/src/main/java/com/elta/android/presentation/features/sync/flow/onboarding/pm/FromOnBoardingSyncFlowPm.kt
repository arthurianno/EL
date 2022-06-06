package com.elta.android.presentation.features.sync.flow.onboarding.pm

import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.sync.flow.base.pm.SyncFlowPm
import javax.inject.Inject

class FromOnBoardingSyncFlowPm @Inject constructor(
    services: ServiceFacade
) : SyncFlowPm(services) {

    override val screen = Screens.FromOnBoardingSyncStart
}
