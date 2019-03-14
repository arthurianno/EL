package com.elta.android.presentation.features.diary.main.pm

import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import javax.inject.Inject

class MainDiaryPm @Inject constructor(
    services: ServiceFacade
) : BaseListPm(services) {

    override fun onCreate() {
        super.onCreate()
    }
}