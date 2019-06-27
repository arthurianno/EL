package com.elta.android.presentation.features.profile.support.pm

import com.elta.android.presentation.Clicks
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.bus.clicks
import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.profile.support.model.SupportAction
import com.elta.android.presentation.features.profile.support.ui.builder.SupportItemsBuilder
import javax.inject.Inject

class SupportPm @Inject constructor(
    services: ServiceFacade,
    private val itemsBuilder: SupportItemsBuilder
) : BaseListPm(services) {

    override fun onCreate() {
        super.onCreate()

        bus.clicks<Clicks.SupportActionClicked>()
            .map { it.action }
            .doOnNext(::handleClick)
            .subscribe()
            .untilDestroy()

        lifecycleObservable.filter { it == Lifecycle.CREATED }
            .map { itemsBuilder.buildItems() }
            .doOnNext(items.consumer)
            .subscribe()
            .untilDestroy()
    }

    private fun handleClick(action: SupportAction) {
        when (action) {
            is SupportAction.CallAction -> router.navigateTo(Screens.CallScreen(action.phone))
            is SupportAction.MailAction -> router.navigateTo(Screens.EmailScreen(action.email))
            is SupportAction.ServiceCentersAction -> router.startFlow(Screens.ServiceCentersMap)
        }
    }
}