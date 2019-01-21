package com.elta.android.presentation.features.registration.activation.pm

import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import javax.inject.Inject

class ActivationPm @Inject constructor(
    services: ServiceFacade
) : BasePm(services) {

    val sendAgainAction = Action<Unit>()
    val continueAction = Action<Unit>()
}