package com.elta.android.presentation.features.main.records.pm

import android.content.Context
import com.elta.android.domain.features.devices.interactor.GetGlucometersUseCase
import com.elta.android.domain.features.devices.model.Glucometer
import com.elta.android.domain.features.devices.model.GlucometerInfo
import com.elta.android.domain.features.diary.events.interactor.GetEventsByPeriodUseCase
import com.elta.android.domain.features.diary.events.model.EventV2
import com.elta.android.domain.features.diary.home.interactor.GetHomeModelUseCase
import com.elta.android.domain.features.diary.home.model.HomeModel
import com.elta.android.domain.features.multiLangsConfig.interactor.GetScreenConfigFromCache
import com.elta.android.domain.features.multiLangsConfig.model.ScreenEntity
import com.elta.android.domain.features.userinfo.interactor.UpdateUserInfoUseCase
import com.elta.android.domain.features.userinfo.model.UserInfo
import com.elta.android.presentation.Events
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.bus.events
import com.elta.android.presentation.core.date.DateChangedEvent
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.core.pm.widgets.stateControl
import com.elta.android.presentation.features.main.records.mapper.MainRecordsMapper
import com.elta.android.presentation.features.main.records.ui.compose.DashboardDevice
import com.elta.android.presentation.features.main.records.ui.compose.GlucoseDashboardUiState
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.Observables
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.YearMonth
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
) : BasePm(services) {

    // Переопределяем screenConfigKey и getScreenConfigUseCase для поддержки конфигов
    override val screenConfigKey: String = "main-screen"
    override val getScreenConfigUseCase: GetScreenConfigFromCache = getScreenConfigFromCacheUseCase

    // State для хранения конфигурации экрана
    val mainScreenConfig = state<ScreenEntity?>()
    val mainScreenImageReady = state(true)
    val dashboardState = state<GlucoseDashboardUiState>()
    val detailedEventsByMonth = state<Map<YearMonth, List<EventV2>>>(emptyMap())

    private val monthCache = DashboardMonthCache()

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
                    .observeOn(Schedulers.computation())
                    .map { (model, devices) ->
                        model to recordsMapper.map(model, devices.primaryDashboardDevice())
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext { (model, dashboard) -> handleSuccess(model, dashboard) }
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
            .observeOn(AndroidSchedulers.mainThread())
            .flatMap { request ->
                val month = YearMonth.from(request.start)
                val requestGeneration = monthCache.start(month)
                    ?: return@flatMap Observable.empty<List<EventV2>>()
                getEventsByPeriodUseCase.execute(
                    GetEventsByPeriodUseCase.Params(request.start, request.end)
                )
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext { events ->
                        monthCache.complete(month, requestGeneration, events)
                            ?.let(detailedEventsByMonth.consumer::accept)
                    }
                    .doOnError { error ->
                        if (monthCache.fail(month, requestGeneration)) {
                            handleError(error)
                        }
                    }
                    .onErrorResumeNext(Observable.empty())
            }
            .subscribe()
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

    private fun handleSuccess(
        model: HomeModel,
        dashboard: GlucoseDashboardUiState
    ) {
        bus.event(Events.HomeModelChanged(model))
        detailedEventsByMonth.consumer.accept(monthCache.clear())
        dashboardState.consumer.accept(dashboard)
        mainScreenState.visibilityState.consumer.accept(false)
    }

    private fun List<Pair<Glucometer, GlucometerInfo>>.primaryDashboardDevice(): DashboardDevice? =
        firstOrNull { it.first.isPrimary }?.let { (meter, info) ->
            DashboardDevice(
                address = meter.address,
                name = meter.name.orEmpty(),
                serialNumber = info.glucometerSerialNumber,
                lastSyncAtMillis = info.syncDate?.toInstant()?.toEpochMilli()
            )
        }

    private fun createUserInfoParams(): UpdateUserInfoUseCase.Params =
        UpdateUserInfoUseCase.Params(UserInfo(isFirstHomeEntrance = false))
}
