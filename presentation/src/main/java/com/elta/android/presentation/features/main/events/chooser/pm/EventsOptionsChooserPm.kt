package com.elta.android.presentation.features.main.events.chooser.pm

import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import javax.inject.Inject

class EventsOptionsChooserPm @Inject constructor(
    services: ServiceFacade
) : BaseListPm(services)