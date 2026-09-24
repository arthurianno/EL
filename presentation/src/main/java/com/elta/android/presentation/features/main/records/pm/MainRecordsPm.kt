package com.elta.android.presentation.features.main.records.pm

import android.content.Context
import com.elta.android.domain.features.devices.interactor.GetGlucometersUseCase
import com.elta.android.domain.features.devices.model.Glucometer
import com.elta.android.domain.features.devices.model.GlucometerInfo
import com.elta.android.domain.features.diary.events.interactor.GetEventsByPeriodUseCase
import com.elta.android.domain.features.diary.home.interactor.GetHomeModelUseCase
import com.elta.android.domain.features.diary.home.model.HomeModel
import com.elta.android.domain.features.multiLangsConfig.interactor.GetScreenConfigFromCache
import com.elta.android.domain.features.multiLangsConfig.model.ScreenEntity
import com.elta.android.domain.features.userinfo.interactor.UpdateUserInfoUseCase
import com.elta.android.domain.features.userinfo.model.UserInfo
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.Events
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.bus.clicks
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.bus.events
import com.elta.android.presentation.core.date.DateChangedEvent
import com.elta.android.presentation.core.pm.ExpandableListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.core.pm.widgets.stateControl
import com.elta.android.presentation.features.main.records.mapper.MainRecordsMapper
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordItem
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordsGroupItem
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordsHeaderItem
import com.elta.android.presentation.features.main.records.ui.compose.DashboardDevice
import com.nullgr.core.adapter.items.ListItem
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import javax.inject.Inject
import me.dmdev.rxpm.action
import me.dmdev.rxpm.state

class MainRecordsPm @Inject constructor(
    private val getHomeModelUseCase: GetHomeModelUseCase,
    private val getGlucometers: GetGlucometersUseCase,
    private val getEventsByPeriodUseCase: GetEventsByPeriodUseCase,
    private val updateUserInfoUseCase: UpdateUserInfoUseCase,
    private val recordsMapper: MainRecordsMapper,
    private val context: Context,
    private val getScreenConfigFromCacheUseCase: GetScreenConfigFromCache,
    services: ServiceFacade
) : ExpandableListPm(services) {

    // Переопределяем screenConfigKey и getScreenConfigUseCase для поддержки конфигов
    override val screenConfigKey: String = "main-screen"
    override val getScreenConfigUseCase: GetScreenConfigFromCache = getScreenConfigFromCacheUseCase

    // State для хранения конфигурации экрана
    val mainScreenConfig = state<ScreenEntity?>()
    val mainScreenImageReady = state(true)

    val mainScreenState = stateControl()

    private val loadScreenAction = action<Unit>()

    override fun onCreate() {
        super.onCreate()

        // Загружаем конфигурацию экрана
        loadScreenConfig(context)

        // Биндим загруженную конфигурацию
        screenConfigState.observable
            .subscribe { config ->
                if (config != null) {
                    mainScreenConfig.consumer.accept(config)
                }
            }
            .untilDestroy()

        imagePreloadState.observable
            .subscribe { isReady ->
                mainScreenImageReady.consumer.accept(isReady)
            }
            .untilDestroy()

        loadScreenAction.observable
            .skipWhileInProgress()
            .flatMap { params ->
                getHomeModelUseCase.execute(params)
                    .hideErrorContainer()
                    .bindProgress()
                    .flatMapSingle { model -> getGlucometers.execute().map { model to it } }
                    .doOnNext { (model, devices) -> handleSuccess(model, devices) }
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        Observable.merge(
            listOf(
                lifecycleObservable.filter { it == Lifecycle.CREATED }.map { Unit },
                bus.events<Events.ProfileUpdated>().map { Unit },
                bus.events<Events.EventsChanged>().map { Unit },
                bus.events<DateChangedEvent>().map { Unit },
                bus.events<Events.DeviceChanged>().map { Unit },
                bus.events<Events.Sync.Glucometer>().filter {
                    it is Events.Sync.Glucometer.Success ||
                        it is Events.Sync.Glucometer.NoNewEvents ||
                        it is Events.Sync.Glucometer.InvalidTime
                }.map { Unit }
            )
        )
            .subscribe(loadScreenAction.consumer)
            .untilDestroy()

        bus.events<Events.DashboardConnectDeviceRequested>()
            .subscribe { router.startFlow(Screens.ConnectTypeScreen(isOnBoarding = false)) }
            .untilDestroy()
        bus.events<Events.DashboardDeviceInfoRequested>()
            .subscribe { router.navigateTo(Screens.DeviceInfo(it.name, it.address)) }
            .untilDestroy()

        bus.events<Events.DetailedChartRangeRequested>()
            // Several neighbouring months can be requested while the user pans the
            // continuous chart. Cancelling the previous request loses data that is
            // still useful for the in-memory month cache.
            .flatMap { request ->
                getEventsByPeriodUseCase.execute(
                    GetEventsByPeriodUseCase.Params(request.start, request.end)
                )
                    .map { events ->
                        Events.DetailedChartRangeLoaded(request.start, request.end, events)
                    }
                    .doOnError(::handleError)
                    .onErrorResumeNext(Observable.empty())
            }
            .subscribe(bus::event)
            .untilDestroy()

        Observables.combineLatest(
            lifecycleObservable.filter { it == Lifecycle.UNBINDED },
            bus.events<Events.HomeModelChanged>().filter { it.model.isFirstEntrance }
        ).flatMapCompletable {
            if (it.second.model.isFirstEntrance) updateUserInfoUseCase.execute(createUserInfoParams())
            else Completable.complete()
        }
            .subscribe()
            .untilDestroy()
    }

    override fun onItemExpandCollapse(
        clickedItem: ListItem,
        allItems: List<ListItem>
    ): List<ListItem> {
        if (clickedItem !is RecordsGroupItem) return allItems
        var expanded = false
        return allItems.map {
            when {
                it is RecordsGroupItem && it.id == clickedItem.id -> {
                    expanded = !it.isExpanded
                    it.copy(isExpanded = expanded)
                }
                it is RecordItem && it.groupId == clickedItem.id -> {
                    it.copy(isVisible = expanded)
                }
                else -> {
                    it
                }
            }
        }
    }

    override fun onBind() {
        super.onBind()

        bus.clicks<Clicks.RecordClicked>()
            .map { it.item }
            .doOnNext(::navigateToEventScreen)
            .subscribe()
            .untilUnbind()
    }

    private fun navigateToEventScreen(record: RecordItem) {
        router.startFlow(Screens.EditEventScreen(record.id as String, record.eventType))
    }

    private fun handleSuccess(
        model: HomeModel,
        devices: List<Pair<Glucometer, GlucometerInfo>>
    ) {
        val device = devices.firstOrNull { it.first.isPrimary }?.let { (meter, info) ->
            DashboardDevice(
                address = meter.address,
                name = meter.name.orEmpty(),
                serialNumber = info.glucometerSerialNumber,
                lastSyncAtMillis = info.syncDate?.toInstant()?.toEpochMilli()
            )
        }
        bus.event(Events.HomeModelChanged(model))
        model.launchState()
        listItems.consumer.accept(recordsMapper.mapFromObject(model).map { item ->
            if (item is RecordsHeaderItem) {
                item.copy(device = device)
            } else item
        })
    }

    private fun HomeModel.launchState() {
        // The dashboard owns both the populated and no-measurements states. Leaving the
        // legacy full-screen state visible here hides the RecyclerView before Compose can
        // render the dashboard's empty state.
        mainScreenState.visibilityState.consumer.accept(false)
    }

    private fun createUserInfoParams(): UpdateUserInfoUseCase.Params =
        UpdateUserInfoUseCase.Params(UserInfo(isFirstHomeEntrance = false))
}
