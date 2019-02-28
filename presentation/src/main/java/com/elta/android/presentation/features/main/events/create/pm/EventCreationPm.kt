package com.elta.android.presentation.features.main.events.create.pm

import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import javax.inject.Inject

class EventCreationPm @Inject constructor(
    services: ServiceFacade
) : BasePm(services)