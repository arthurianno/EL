package com.elta.android.presentation.features.home.pm

import com.elta.android.domain.features.events.model.UserEvent
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.bus.clicks
import com.elta.android.presentation.core.pm.BaseFlowPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.home.ui.adapter.items.UserEventItem
import com.elta.android.presentation.features.main.events.chooser.models.ChooserConfiguration
import com.elta.android.presentation.features.main.events.chooser.models.ChooserType
import com.elta.android.presentation.utils.toIcon
import com.elta.android.presentation.utils.toName
import com.nullgr.core.adapter.items.ListItem
import java.util.concurrent.TimeUnit
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
            .doOnNext { closeBottomSheetCommand.consumer.accept(Unit) }
            .map { it.userEvent }
            .delay(OPEN_EVENT_SCREEN_DELAY, TimeUnit.MILLISECONDS)
            .doOnNext(::handleAddEventClick)
            .subscribe()
            .untilDestroy()
    }

    private fun handleAddEventClick(event: UserEvent) {
        // TODO TEST CASE ONLY
        router.startFlow(
            Screens.EventsChooserScreen(
                ChooserConfiguration(ChooserType.GROUP_TAGS, event)
            )
        )
    }

    private fun UserEvent.toListItem() =
        UserEventItem(
            titleRes = this.toName(),
            iconRes = this.toIcon(),
            userEvent = this
        )

    companion object {
        private const val OPEN_EVENT_SCREEN_DELAY = 300L
    }
}