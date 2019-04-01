package com.elta.android.presentation.features.profile.settings.dialogs.base.pm;

import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade

abstract class BaseSettingsDialogPm constructor(
    services: ServiceFacade
) : BasePm(services) {
    val actionButtonEnabledCommand = State(false)
    val mainAction = Action<Unit>()
    val closeDialogCommand = Command<Unit>(bufferSize = 1)
}