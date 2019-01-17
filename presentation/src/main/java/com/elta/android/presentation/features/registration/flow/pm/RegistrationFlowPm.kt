package com.elta.android.presentation.features.registration.flow.pm

import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import javax.inject.Inject

class RegistrationFlowPm @Inject constructor(
    services: ServiceFacade
) : BasePm(services) {

    override fun onCreate() {
        super.onCreate()
        flowRouter?.navigateTo(Screens.RegistrationMain)
    }
}