package com.elta.android.presentation.features.profile.settings.dialogs.hemoglobin.pm

import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.profile.settings.dialogs.base.pm.BaseSettingsDialogPm
import javax.inject.Inject

class HemoglobinSettingsPm @Inject constructor(
    services: ServiceFacade
) : BaseSettingsDialogPm(services) {

    val dateState = State("12 June")
    val hemoglobinValueState = State("5,6")

    val minusAction = Action<Unit>()
    val plusAction = Action<Unit>()

}