package com.elta.android.presentation.features.main.records.pm

import com.elta.android.presentation.R
import com.elta.android.presentation.States
import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordItem
import com.nullgr.core.adapter.items.ListItem
import com.elta.android.presentation.core.pm.widgets.stateControl
import com.elta.android.presentation.utils.getGreetingText
import java.util.Calendar
import javax.inject.Inject

@Suppress("MagicNumber", "ForEachOnRange")
class MainRecordsPm @Inject constructor(
    services: ServiceFacade
) : BaseListPm(services) {

    val mainScreenState = stateControl()

    override fun onCreate() {
        super.onCreate()

        items.consumer.accept(arrayListOf<ListItem>().apply {
            (0..5).forEach { item ->
                add(
                    RecordItem(
                        id = item,
                        icon = R.drawable.ic_medicine,
                        title = "Title #$item",
                        type = "Type #$item",
                        count = "$item ед",
                        date = "12:00",
                        showLabel = item % 2 == 0
                    )
                )
            }
        })

        bindMainScreenState()
    }

    private fun bindMainScreenState() {
        mainScreenState.dataState.consumer.accept(makeNewDayLaunchState())
        mainScreenState.visibilityState.consumer.accept(false)
    }

    private fun makeFirsLaunchState() =
        States.MainRecordsScreenFirstLaunchState(resources)

    private fun makeNewDayLaunchState() =
        States.MainRecordsScreenNewDayState(
            resources,
            title = Calendar.getInstance().getGreetingText(resources)
        )
}