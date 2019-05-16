package com.elta.android.presentation.features.home.pm

import com.elta.android.domain.features.devices.interactor.SyncWithGlucometerUseCase
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.home.interactor.GetAddableEventsUseCase
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.Events
import com.elta.android.presentation.R
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.bus.clicks
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.bus.events
import com.elta.android.presentation.core.pm.BaseFlowPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.home.ui.adapter.items.UserEventItem
import com.elta.android.presentation.utils.toIcon
import com.elta.android.presentation.utils.toName
import com.nullgr.core.adapter.items.ListItem
import me.dmdev.rxpm.bindProgress
import me.dmdev.rxpm.skipWhileInProgress
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class HomeFlowPm @Inject constructor(
    private val syncWithGlucometerUseCase: SyncWithGlucometerUseCase,
    private val getAddableEventsUseCase: GetAddableEventsUseCase,
    services: ServiceFacade
) : BaseFlowPm(services) {

    val bottomSheetItems = State<List<ListItem>>()
    val closeBottomSheetCommand = Command<Unit>()
    val pulseCommand = Command<Boolean>()
    val menuItemSelectedAction = Action<Int>()
    val menuItemRestoredAction = Action<Int>()
    val selectedItemIdState = State(R.id.mainMenuItemView)

    private val loadEvents = Action<Unit>()
    private val startSyncAction = Action<Unit>()
    private val syncProgressState = State(false)

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

        syncProgressState.observable
            .subscribe { inProgress ->
                bus.event(Events.SyncProgress(inProgress))
            }
            .untilDestroy()

        startSyncAction.observable
            .skipWhileInProgress(syncProgressState.observable)
            .map { SyncWithGlucometerUseCase.Params() }
            .flatMapCompletable { params ->
                syncWithGlucometerUseCase.execute(params)
                    .bindProgress(syncProgressState.consumer)
                    .doOnComplete(::handleSyncCompleted)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        menuItemRestoredAction.observable
            .subscribe(selectedItemIdState.consumer)
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
        router.newTabs(arrayOf(Screens.MainTab, Screens.DiaryTab, Screens.StatisticTab, Screens.ProfileTab))
        router.navigateToTab(Screens.MainTab)
    }

    private fun handleSuccess(events: List<EventType>) {
        val items = mutableListOf<ListItem>()
        items.addAll(events.map { it.toListItem() })
        items.add(UserEventItem(R.drawable.ic_event_refresh, R.string.event_type_sync, META_SYNC))
        bottomSheetItems.consumer.accept(items)
    }

    private fun observeClicks() {
        bus.clicks<Clicks.AddUserEvent>()
            .map { it.meta }
            .doOnNext { closeBottomSheetCommand.consumer.accept(Unit) }
            .delay(OPEN_EVENT_SCREEN_DELAY, TimeUnit.MILLISECONDS)
            .doOnNext(::handleAddEventClick)
            .subscribe()
            .untilDestroy()

        menuItemSelectedAction.observable
            .doOnNext(::handleBottomMenuClick)
            .subscribe(selectedItemIdState.consumer)
            .untilDestroy()
    }

    private fun handleAddEventClick(meta: Any) {
        if (meta is EventType) {
            router.startFlow(Screens.EventsCreationScreen(meta))
        } else if (meta == META_SYNC) {
            startSyncAction.consumer.accept(Unit)
        }
    }

    private fun handleBottomMenuClick(id: Int) {
        when (id) {
            R.id.mainMenuItemView -> router.navigateToTab(Screens.MainTab)
            R.id.notesMenuItemView -> router.navigateToTab(Screens.DiaryTab)
            R.id.statsMenuItemView -> router.navigateToTab(Screens.StatisticTab)
            R.id.profileMenuItemView -> router.navigateToTab(Screens.ProfileTab)
        }
    }

    private fun EventType.toListItem() =
        UserEventItem(
            titleRes = this.toName(),
            iconRes = this.toIcon(),
            meta = this
        )

    private fun handleSyncCompleted() {

    }

    companion object {
        private const val OPEN_EVENT_SCREEN_DELAY = 400L
        private const val META_SYNC = "meta_sync"
    }
}