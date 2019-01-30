package com.elta.android.presentation.features.onboaring.pm

import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.onboaring.ui.adapter.items.TestItem
import javax.inject.Inject

class OnBoardingPm @Inject constructor(
    services: ServiceFacade
) : BaseListPm(services) {

    override fun onCreate() {
        super.onCreate()
        items.consumer.accept(listOf(
            TestItem("ITEM 1"),
            TestItem("ITEM 2"),
            TestItem("ITEM 2")
        ))
    }
}