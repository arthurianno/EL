package com.elta.android.presentation.features.home.pm

import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.home.interactor.GetAddableEventsUseCase
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.Events
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.bus.clicks
import com.elta.android.presentation.core.bus.events
import com.elta.android.presentation.core.pm.BaseFlowPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.home.ui.adapter.items.UserEventItem
import com.elta.android.presentation.utils.toIcon
import com.elta.android.presentation.utils.toName
import com.nullgr.core.adapter.items.ListItem
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class HomeFlowPm @Inject constructor(
    private val getAddableEventsUseCase: GetAddableEventsUseCase,
    services: ServiceFacade
) : BaseFlowPm(services) {

    val bottomSheetItems = State<List<ListItem>>()
    val closeBottomSheetCommand = Command<Unit>()
    val pulseCommand = Command<Boolean>()

    private val loadEvents = Action<Unit>()

    override fun onCreate() {
        super.onCreate()

        loadEvents.observable
            .skipWhileInProgress()
            .flatMapSingle { params ->
                getAddableEventsUseCase.execute(params)
                    .hideErrorContainer()
                    .bindProgress()
                    .doOnSuccess(::handleSuccess)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        lifecycleObservable
            .filter { it == Lifecycle.CREATED }
            .map { Unit }
            .subscribe(loadEvents.consumer)
            .untilDestroy()

        bus.events<Events.HomeModelChanged>()
            .map { it.model.isFirstEntrance || !it.model.hasEvents }
            .subscribe(pulseCommand.consumer)
            .untilDestroy()

        observeClicks()
    }

    override fun navigateToLaunchScreen() {
        router.newTabs(arrayOf(Screens.MainTab))
        router.navigateToTab(Screens.MainTab)
    }

    private fun handleSuccess(events: List<EventType>) {
        bottomSheetItems.consumer.accept(events.map { it.toListItem() })
    }

    private fun observeClicks() {
        bus.clicks<Clicks.AddUserEvent>()
            .map { it.event }
            .doOnNext { closeBottomSheetCommand.consumer.accept(Unit) }
            .delay(OPEN_EVENT_SCREEN_DELAY, TimeUnit.MILLISECONDS)
            .doOnNext(::handleAddEventClick)
            .subscribe()
            .untilDestroy()
    }

    private fun handleAddEventClick(event: EventType) {
        router.startFlow(Screens.EventsCreationScreen(event))
    }

    private fun EventType.toListItem() =
        UserEventItem(
            titleRes = this.toName(),
            iconRes = this.toIcon(),
            event = this
        )

    companion object {
        private const val OPEN_EVENT_SCREEN_DELAY = 400L
    }
}