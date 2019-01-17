package com.elta.android.presentation.features.registration.main.pm

import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import javax.inject.Inject

class RegistrationMainPm @Inject constructor(
    services: ServiceFacade
) : BasePm(services) {

    override val backAction = Action<Unit>()

    override fun onCreate() {
        super.onCreate()

        // TODO discuss
        backAction.observable
            .doOnNext { ::handleBack }
            .subscribe()
            .untilDestroy()
    }

    private fun handleBack() {
        flowRouter?.finishFlow()
    }
}