package com.elta.android.presentation.features.main.records.pm

import com.elta.android.presentation.States
import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.core.pm.widgets.stateControl
import com.elta.android.presentation.utils.getGreetingText
import java.util.Calendar
import javax.inject.Inject

class MainRecordsPm @Inject constructor(
    services: ServiceFacade
) : BaseListPm(services) {

    val mainScreenState = stateControl()

    override fun onCreate() {
        super.onCreate()
        bindMainScreenState()
    }

    private fun bindMainScreenState() {
        mainScreenState.dataState.consumer.accept(makeNewDayLaunchState())
    }

    private fun makeFirsLaunchState() =
        States.MainRecordsScreenFirstLaunchState(resources)

    private fun makeNewDayLaunchState() =
        States.MainRecordsScreenNewDayState(
            resources,
            title = Calendar.getInstance().getGreetingText(resources)
        )
}