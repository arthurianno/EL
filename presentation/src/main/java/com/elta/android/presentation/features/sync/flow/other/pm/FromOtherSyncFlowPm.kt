package com.elta.android.presentation.features.sync.flow.other.pm

import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.sync.flow.base.pm.SyncFlowPm
import javax.inject.Inject

@Deprecated("Данный flow помечен на удаление")
class FromOtherSyncFlowPm @Inject constructor(
    services: ServiceFacade
) : SyncFlowPm(services) {

    override val screen = Screens.FromOtherSyncStart
}
