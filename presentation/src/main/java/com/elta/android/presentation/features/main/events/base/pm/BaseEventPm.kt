package com.elta.android.presentation.features.main.events.base.pm

import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade

abstract class BaseEventPm constructor(
    services: ServiceFacade
) : BasePm(services)