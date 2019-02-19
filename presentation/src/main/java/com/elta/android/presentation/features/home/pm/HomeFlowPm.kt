package com.elta.android.presentation.features.home.pm

import com.elta.android.domain.features.events.model.UserEvent
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.bus.clicks
import com.elta.android.presentation.core.pm.BaseFlowPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.home.ui.adapter.items.UserEventItem
import com.elta.android.presentation.utils.toIcon
import com.elta.android.presentation.utils.toName
import com.nullgr.core.adapter.items.ListItem
import timber.log.Timber
import javax.inject.Inject

class HomeFlowPm @Inject constructor(
    services: ServiceFacade
) : BaseFlowPm(services) {

    val bottomSheetItems = State<List<ListItem>>()
    val closeBottomSheetCommand = Command<Unit>()

    override fun onCreate() {
        super.onCreate()
        addEventItems()
        observeClicks()
    }

    override fun navigateToLaunchScreen() {
        router.newTabs(arrayOf(Screens.MainTab))
        router.navigateToTab(Screens.MainTab)
    }

    private fun addEventItems() {
        bottomSheetItems.consumer.accept(
            UserEvent.values().map { it.toListItem() }
        )
    }

    private fun observeClicks() {
        bus.clicks<Clicks.AddUserEvent>()
            .map { it.userEvent }
            .doOnNext(::handleAddEventClick)
            .map { Unit }
            .doOnNext(closeBottomSheetCommand.consumer)
            .subscribe()
            .untilDestroy()
    }

    private fun handleAddEventClick(event: UserEvent) {
        Timber.d("handleAddEventClick $event")
        // TODO navigateTo -> Add event screen
    }

    private fun UserEvent.toListItem() =
        UserEventItem(
            titleRes = this.toName(),
            iconRes = this.toIcon(),
            userEvent = this
        )
}