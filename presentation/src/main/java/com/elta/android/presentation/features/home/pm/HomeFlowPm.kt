package com.elta.android.presentation.features.home.pm

import com.elta.android.presentation.R
import com.elta.android.presentation.core.pm.BaseFlowPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.home.ui.adapter.items.EventItem
import com.nullgr.core.adapter.items.ListItem
import javax.inject.Inject

class HomeFlowPm @Inject constructor(
    services: ServiceFacade
) : BaseFlowPm(services) {

    val bottomSheetItems = State<List<ListItem>>()

    override fun onCreate() {
        super.onCreate()
        addEventItems()
    }

    override fun navigateToLaunchScreen() {
        // TODO here should be called router.newTabs
    }

    private fun addEventItems() {
        bottomSheetItems.consumer.accept(
            listOf(
                EventItem(R.drawable.ic_xe, R.string.event_type_xe),
                EventItem(R.drawable.ic_ins, R.string.event_type_insulin),
                EventItem(R.drawable.ic_medicine, R.string.event_type_medicines),
                EventItem(R.drawable.ic_weight, R.string.event_type_weight),
                EventItem(R.drawable.ic_active, R.string.event_type_activity)
            )
        )
    }
}