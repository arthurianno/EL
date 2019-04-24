package com.elta.android.presentation.features.sync.pin.pm

import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import javax.inject.Inject

class PinDialogPm @Inject constructor(
    services: ServiceFacade
) : BasePm(services) {

    val actionButtonEnabledState = State(false)
    val mainAction = Action<Unit>()
    val closeDialogCommand = Command<Unit>(bufferSize = 1)
}