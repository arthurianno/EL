package com.elta.android.presentation.features.registration.main.pm

import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import javax.inject.Inject

class RegistrationMainPm @Inject constructor(
    services: ServiceFacade
) : BasePm(services) {

    override fun onCreate() {
        super.onCreate()
    }
}