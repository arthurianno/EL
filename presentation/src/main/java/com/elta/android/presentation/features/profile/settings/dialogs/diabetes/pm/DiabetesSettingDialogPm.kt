package com.elta.android.presentation.features.profile.settings.dialogs.diabetes.pm

import com.elta.android.domain.features.user.model.Diabetes
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import javax.inject.Inject

class DiabetesSettingDialogPm @Inject constructor(
    services: ServiceFacade
) : BasePm(services) {

    val initialDiabetesAction = Action<Diabetes>()
}