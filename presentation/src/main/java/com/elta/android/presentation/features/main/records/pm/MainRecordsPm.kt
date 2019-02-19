package com.elta.android.presentation.features.main.records.pm

import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import javax.inject.Inject

class MainRecordsPm @Inject constructor(
    services: ServiceFacade
) : BaseListPm(services)