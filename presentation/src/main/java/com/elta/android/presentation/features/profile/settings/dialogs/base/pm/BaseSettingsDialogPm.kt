package com.elta.android.presentation.features.profile.settings.dialogs.base.pm

import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import me.dmdev.rxpm.action
import me.dmdev.rxpm.command
import me.dmdev.rxpm.state

abstract class BaseSettingsDialogPm constructor(
    services: ServiceFacade
) : BasePm(services) {
    val actionButtonEnabledCommand = state(false)
    val mainAction = action<Unit>()
    val closeDialogCommand = command<Unit>(bufferSize = 1)
}
