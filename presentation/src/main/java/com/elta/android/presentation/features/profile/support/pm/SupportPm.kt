package com.elta.android.presentation.features.profile.support.pm

import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import javax.inject.Inject

class SupportPm @Inject constructor(
    services: ServiceFacade
) : BaseListPm(services)