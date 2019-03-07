package com.elta.android.presentation.features.main.events.create.pm

import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.main.events.base.pm.BaseEventPm
import javax.inject.Inject

class EventCreationPm @Inject constructor(
    services: ServiceFacade
) : BaseEventPm(services) {

    override fun handleBack(i: Unit) {
        router.exit()
    }
}