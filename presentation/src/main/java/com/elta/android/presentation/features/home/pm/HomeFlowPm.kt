package com.elta.android.presentation.features.home.pm

import android.util.Log
import com.elta.android.common.errors.BluetoothNotEnabledError
import com.elta.android.common.errors.GlucometerOfflineError
import com.elta.android.common.errors.GlucometerSyncError
import com.elta.android.common.errors.InvalidRefreshTokenError
import com.elta.android.common.errors.LocationNotEnabledError
import com.elta.android.common.errors.LocationPermissionNotGrantedError
import com.elta.android.common.errors.PrimaryGlucometerNotFoundError
import com.elta.android.domain.features.auth.interactor.LogOutUseCase
import com.elta.android.domain.features.devices.interactor.GetGlucometersUseCase
import com.elta.android.domain.features.devices.interactor.SyncWithGlucometerUseCase
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.home.interactor.GetAddableEventsUseCase
import com.elta.android.domain.features.diary.home.model.CalculatorFlow.Companion.toCalculatorFlow
import com.elta.android.domain.features.feedback.interactor.ShouldSendFeedbackUseCase
import com.elta.android.domain.features.sync.interactor.SyncLocalChangesUseCase
import com.elta.android.domain.features.user.interactor.GetGlucoseFormatUseCase
import com.elta.android.domain.features.user.interactor.GetUpdatedProfileUseCase
import com.elta.android.domain.features.user.model.GlucoseFormat
import com.elta.android.domain.features.userinfo.interactor.GetUserInfoUseCase
import com.elta.android.domain.features.userinfo.interactor.UpdateUserInfoUseCase
import com.elta.android.domain.features.userinfo.model.UserInfo
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.Dialogs
import com.elta.android.presentation.Events
import com.elta.android.presentation.R
import com.elta.android.presentation.Screens
import com.elta.android.presentation.analytics.model.AnalyticsEvent
import com.elta.android.presentation.analytics.model.AnalyticsEventType
import com.elta.android.presentation.core.bus.clicks
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.bus.events
import com.elta.android.presentation.core.pm.BaseFlowPm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.core.pm.listeners.ConnectionListener
import com.elta.android.presentation.core.ui.dialog.DialogData
import com.elta.android.presentation.core.ui.dialog.DialogResult
import com.elta.android.presentation.features.home.model.ManualSyncError
import com.elta.android.presentation.features.home.ui.adapter.items.UserEventItem
import com.elta.android.presentation.features.sync.control.bluetoothControl2
import com.elta.android.presentation.utils.toIcon
import com.elta.android.presentation.utils.toName
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.rx.bindProgress
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.rxkotlin.Singles
import me.dmdev.rxpm.action
import me.dmdev.rxpm.command
import me.dmdev.rxpm.skipWhileInProgress
import me.dmdev.rxpm.state
import me.dmdev.rxpm.widget.dialogControl
import timber.log.Timber
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import javax.inject.Inject

private const val OPEN_EVENT_SCREEN_DELAY_MILLIS = 400L
private const val META_SYNC = "meta_sync"
private const val SYNC_AFTER_CONNECTION_RESTORED_DELAY_MILLIS = 2800L

class HomeFlowPm @Inject constructor(
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val getDevicesUseCase: GetGlucometersUseCase,
    private val updateUserInfoUseCase: UpdateUserInfoUseCase,
    private val shouldSendFeedbackUseCase: ShouldSendFeedbackUseCase,
    private val syncWithGlucometerUseCase: SyncWithGlucometerUseCase,
    private val getAddableEventsUseCase: GetAddableEventsUseCase,
    private val syncWithBackendUseCase: SyncLocalChangesUseCase,
    private val logOutUseCase: LogOutUseCase,
    private val getGlucoseFormat: GetGlucoseFormatUseCase,
    private val getUpdatedProfileUseCase: GetUpdatedProfileUseCase,
    services: ServiceFacade
) : BaseFlowPm(services), ConnectionListener {

    val bottomSheetItems = state<List<ListItem>>()
    val closeBottomSheetCommand = command<Unit>()
    val showBottomSheetCommand = command<Unit>()
    val closeHelpBottomSheetCommand = command<Unit>()
    val showHelpBottomSheetCommand = command<Unit>()
    val pulseCommand = command<Boolean>()
    val homeAction = action<Boolean>()
    val menuItemSelectedAction = action<Int>()
    val menuItemRestoredAction = action<Int>()
    val selectedItemIdState = state(R.id.mainMenuItemView)
    val glucoseFormat = state<GlucoseFormat>()

    val manualSyncError = state<ManualSyncError>()
    val manualSyncErrorBottomSheetCommand = command<Unit>()
    val manualSyncErrorAction = action<Unit>()
    val closeBottomSheetErrorAction = action<Unit>()
    val closeManualSyncErrorBottomSheetCommand = command<Unit>()

    val btControl = bluetoothControl2()

    val googlePlayDialogControl = dialogControl<DialogData, DialogResult>()
    val feedbackDialogControl = dialogControl<DialogData, DialogResult>()
    val likeAppDialogControl = dialogControl<DialogData, DialogResult>()

    val firstSyncAction = action<Unit>()
    private val isFirstSync = state<Boolean>()
    private val loadEvents = action<Unit>()
    private val startSyncAction = action<Unit>()
    private val startAutoSyncAction = action<Unit>()
    private val syncProgressState = state(false)

    private val syncWithBackendProgressState = state(false)
    private val startSyncWithBackendAction = action<Unit>()

    private val feedbackAction = action<Unit>()
    private val likeAppDialogAction = action<Int>()
    private val feedbackDialogAction = action<Unit>()
    private val googlePlayDialogAction = action<Unit>()
    private val feedbackDialogData by lazy { Dialogs.FeedbackData(resources) }
    private val googlePlayDialogData by lazy { Dialogs.GooglePlayRateData(resources) }

    override fun onCreate() {
        super.onCreate()

        bindSyncAction()
        bindSyncWithBackendAction()
        bindLikeAppDialog()
        bindGooglePlayRateDialog()
        bindFeedbackDialog()
        bindFeedbackAction()
        initGlucoseFormat()

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

        menuItemRestoredAction.observable
            .subscribe(selectedItemIdState.consumer)
            .untilDestroy()

        homeAction.observable
            .doOnNext {
                (if (it) showBottomSheetCommand else closeBottomSheetCommand).consumer.accept(Unit)
            }
            .filter { it }
            .trackEvent(AnalyticsEventType.NEW_EVENT_OPEN)
            .subscribe()
            .untilDestroy()

        lifecycleObservable.filter { it == Lifecycle.CREATED }
            .map { Unit }
            .doOnNext(startAutoSyncAction.consumer)
            .doOnNext(loadEvents.consumer)
            .subscribe()
            .untilDestroy()

        bus.events<Events.HomeModelChanged>()
            .map { it.model.isFirstEntrance || !it.model.hasEvents }
            .subscribe(pulseCommand.consumer)
            .untilDestroy()

        bus.events<Events.EventsChanged>()
            .filter { it.isCreated }
            .map { Unit }
            .subscribe(feedbackAction.consumer)
            .untilDestroy()

        bus.events<Events.EventsChanged>()
            .flatMap {
                getGlucoseFormat.execute()
                    .toObservable()
            }
            .doOnError { Log.d("error", "bus.events<Events.EventsChanged> error") }
            .subscribe(glucoseFormat.consumer)

        bus.events<Events.DeviceChanged>()
            .flatMapSingle {
                getUserInfoUseCase.execute()
                    .map { it.isFirstSync == true }
            }
            .doOnError { Log.d("error", "bus.events<Events.DeviceChanged> error") }
            .subscribe(isFirstSync.consumer)

        observeClicks()
    }

    override fun navigateToLaunchScreen() {
        router.newTabs(
            arrayOf(
                Screens.MainTab,
                Screens.DiaryTab,
                Screens.StatisticTab,
                Screens.ProfileTab
            )
        )
        router.navigateToTab(Screens.MainTab)
    }

    private fun initGlucoseFormat() {
        getUserInfoUseCase.execute()
            .toObservable()
            .map { it.isFirstSync == true }
            .subscribe(
                { isFirstSync.consumer.accept(it) },
                { Timber.e(it) })
            .untilDestroy()

        getGlucoseFormat.execute()
            .subscribe(glucoseFormat.consumer)
            .untilDestroy()
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
            .delay(OPEN_EVENT_SCREEN_DELAY_MILLIS, TimeUnit.MILLISECONDS)
            .flatMapCompletable { meta ->
                when (meta) {
                    is EventType -> {
                        if (meta is EventType.Bread) {
                            getUpdatedProfileUseCase.execute()
                                .map { events -> events.diabetes.toCalculatorFlow() }
                                .map { EventType.Bread(it) }
                                .doOnSuccess {
                                    router.startFlow(Screens.EventsCreationScreen(it))
                                }
                                .ignoreElement()
                        } else {
                            Completable.fromCallable {
                                router.startFlow(Screens.EventsCreationScreen(meta))
                            }
                        }
                    }

                    META_SYNC -> {
                        Completable.fromCallable {
                            if (isFirstSync.valueOrNull == true) {
                                showHelpBottomSheetCommand.consumer.accept(Unit)
                            } else {
                                startSyncAction.consumer.accept(Unit)
                            }
                        }
                    }

                    else -> Completable.complete()

                }

            }
            .subscribe()
            .untilDestroy()

        menuItemSelectedAction.observable
            .map { it to handleBottomMenuClick(it) }
            .trackEvent { AnalyticsEvent(it.second.second) }
            .doOnNext { router.navigateToTab(it.second.first) }
            .map { it.first }
            .subscribe(selectedItemIdState.consumer)
            .untilDestroy()
    }

    private fun bindSyncAction() {
        firstSyncAction.observable
            .doOnNext(startSyncAction.consumer)
            .doOnNext(closeHelpBottomSheetCommand.consumer)
            .map { false }
            .doOnNext(isFirstSync.consumer)
            .flatMapCompletable {
                updateUserInfoUseCase.execute(UpdateUserInfoUseCase.Params(UserInfo(isFirstSync = it)))
            }
            .subscribe()
            .untilDestroy()

        manualSyncErrorAction.observable
            .subscribe(closeManualSyncErrorBottomSheetCommand.consumer)
            .untilDestroy()

        closeBottomSheetErrorAction.observable
            .subscribe(closeManualSyncErrorBottomSheetCommand.consumer)
            .untilDestroy()

        Observable.merge(
            manualSyncErrorAction.observable,
            startSyncAction.observable
        )
            .skipWhileInProgress(syncProgressState.observable)
            .flatMap {
                syncWithGlucometer(isAuto = false)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        startAutoSyncAction.observable
            .skipWhileInProgress(syncProgressState.observable)
            .flatMap { autoSyncObservable() }
            .retry()
            .subscribe()
            .untilDestroy()
    }

    private fun bindSyncWithBackendAction() {
        syncWithBackendProgressState.observable
            .subscribe { inProgress ->
                bus.event(Events.BackendSyncProgress(inProgress))
            }
            .untilDestroy()

        startSyncWithBackendAction.observable
            .skipWhileInProgress(syncWithBackendProgressState.observable)
            .flatMapCompletable {
                syncWithBackendUseCase.execute()
                    .bindProgress(syncWithBackendProgressState.consumer)
                    .doOnSubscribe { bus.event(Events.Sync.Server.Started) }
                    .doOnComplete { bus.event(Events.Sync.Server.Success) }
                    .doOnComplete {
                        bus.event(Events.EventsChanged(false))
                        bus.event(Events.ProfileUpdated)
                    }
                    .doOnError(::handleError)
            }
            .subscribe()
            .untilDestroy()

        networkStateCommand.observable
            .delay(SYNC_AFTER_CONNECTION_RESTORED_DELAY_MILLIS, TimeUnit.MILLISECONDS)
            .filter { it }
            .map { Unit }
            .skipWhileInProgress(syncProgressState.observable)
            .subscribe { startSyncWithBackendAction.consumer.accept(it) }
            .untilDestroy()
    }

    private fun bindFeedbackAction() {
        feedbackAction.observable
            .flatMap {
                shouldSendFeedbackUseCase.execute(Unit)
                    .filter { it.isSendFeedback }
                    .map { it.step }
                    .doOnSuccess(likeAppDialogAction.consumer)
                    .doOnError(::handleError)
                    .toObservable()
            }
            .retry()
            .subscribe()
            .untilDestroy()
    }

    private fun bindGooglePlayRateDialog() {
        googlePlayDialogAction.observable
            .switchMapMaybe { googlePlayDialogControl.showForResult(googlePlayDialogData) }
            .filter { it == DialogResult.POSITIVE }
            .flatMapCompletable {
                updateUserInfoUseCase.execute(createUserInfoParams())
                    .doOnComplete {
                        router.navigateTo(Screens.PlayMarketScreen)
                    }
            }
            .subscribe()
            .untilDestroy()
    }

    private fun bindLikeAppDialog() {
        likeAppDialogAction.observable
            .switchMapMaybe { step ->
                likeAppDialogControl.showForResult(Dialogs.LikeAppRateData(resources, step))
            }
            .doOnNext { result ->
                when (result) {
                    DialogResult.POSITIVE -> googlePlayDialogAction.consumer.accept(Unit)
                    DialogResult.NEGATIVE -> feedbackDialogAction.consumer.accept(Unit)
                    else -> {
                    }
                }
            }
            .subscribe()
            .untilDestroy()
    }

    private fun bindFeedbackDialog() {
        feedbackDialogAction.observable
            .switchMapMaybe { feedbackDialogControl.showForResult(feedbackDialogData) }
            .filter { it == DialogResult.POSITIVE }
            .map { Screens.Feedback }
            .doOnNext(router::startFlow)
            .subscribe()
            .untilDestroy()
    }

    private fun handleBottomMenuClick(id: Int) =
        when (id) {
            R.id.mainMenuItemView -> Screens.MainTab to AnalyticsEventType.HOMEPAGE
            R.id.notesMenuItemView -> Screens.DiaryTab to AnalyticsEventType.DIARY_OPEN
            R.id.statsMenuItemView -> Screens.StatisticTab to AnalyticsEventType.STATISTICS_OPEN
            R.id.profileMenuItemView -> Screens.ProfileTab to AnalyticsEventType.PROFILE_OPEN
            else -> throw IllegalArgumentException("$id is not supported.")
        }

    private fun EventType.toListItem() =
        UserEventItem(
            titleRes = this.toName(),
            iconRes = this.toIcon(),
            meta = this
        )

    private fun handleSyncCompleted(events: Int) {
        if (events > 0) bus.event(Events.EventsChanged(true)) //TODO: тут обязательно добавить снек, что новых событий не найдено. ПОТОМУ ЧТО ПО ТЗ ИМЕННО ТАК!
    }

    private fun createUserInfoParams(): UpdateUserInfoUseCase.Params =
        UpdateUserInfoUseCase.Params(UserInfo(isFeedbackSent = true))

    private fun autoSyncObservable(): Observable<Unit> =
        Singles.zip(getUserInfoUseCase.execute(), getDevicesUseCase.execute())
            .flatMapObservable { (info, devices) ->
                if (devices.find { it.first.isPrimary } != null && info.isFirstHomeEntrance != true) {
                    syncWithGlucometer(isAuto = true)
                        .map { Unit }
                        .doOnError(::handleSyncAutoError)
                        .doOnComplete { startSyncWithBackendAction.consumer.accept(Unit) }
                } else {
                    Observable.fromCallable {
                        startSyncWithBackendAction.consumer.accept(Unit)
                    }
                }
            }

    private fun syncWithGlucometer(isAuto: Boolean): Observable<Int> =
        syncWithGlucometerUseCase.execute(SyncWithGlucometerUseCase.Params())
            .bindProgress(syncProgressState.consumer)
            .doOnSubscribe { bus.event(Events.Sync.Glucometer.Started) }
            .doOnComplete {
                bus.event(Events.Sync.Glucometer.Success)
            }
            .doOnError { error ->
                if (isAuto) {
                    handleAutoSyncError(error)
                } else {
                    handleManualSyncError(error)
                }
            }
            .onErrorResumeNext { error: Throwable ->
                observableSyncError(error, isAuto)
            }
            .doOnNext(::handleSyncCompleted)

    private fun observableSyncError(
        error: Throwable,
        isAuto: Boolean
    ): ObservableSource<out Int> = when (error) {
        is BluetoothNotEnabledError -> bluetoothEnableAndRepeat(isAuto)
        is LocationNotEnabledError -> locationEnableAndRepeat(isAuto)
        is GlucometerSyncError ->
            when (error.cause) {
                is BluetoothNotEnabledError -> bluetoothEnableAndRepeat(isAuto)
                is LocationPermissionNotGrantedError ->
                    requestLocatePermissionAndRepeat(isAuto)

                is LocationNotEnabledError -> locationEnableAndRepeat(isAuto)

                else -> {
                    Observable.error(error)
                }
            }

        else -> Observable.error(error)
    }

    private fun locationEnableAndRepeat(isAuto: Boolean) = btControl.requestEnableLocation()
        .filter { it }
        .flatMapObservable { syncWithGlucometer(isAuto) }

    private fun requestLocatePermissionAndRepeat(isAuto: Boolean) =
        btControl.requestLocationPermissions()
            .filter { it }
            .flatMapObservable { syncWithGlucometer(isAuto) }

    private fun bluetoothEnableAndRepeat(isAuto: Boolean) = btControl.requestEnableBluetooth()
        .filter { it }
        .flatMapObservable { syncWithGlucometer(isAuto) }


    private fun handleManualSyncError(error: Throwable) {
        bus.event(Events.Sync.Glucometer.Nothing)

        when (error) {
            is BluetoothNotEnabledError, LocationNotEnabledError -> {
                manualSyncError.accept(ManualSyncError.ErrorSync)
            }

            is PrimaryGlucometerNotFoundError -> {
                openConnectScreen()
            }

            is GlucometerOfflineError,  -> {
                manualSyncError.accept(ManualSyncError.NotFound)
                manualSyncErrorBottomSheetCommand.accept(Unit)
            }

            is GlucometerSyncError -> {
                val manualSyncError = if (error.cause is TimeoutException) {
                    ManualSyncError.NotFound
                } else {
                    ManualSyncError.ErrorSync
                }
                this.manualSyncError.accept(manualSyncError)
                manualSyncErrorBottomSheetCommand.accept(Unit)
            }

            else -> handleError(error)
        }
    }

    private fun handleAutoSyncError(error: Throwable) {
        when {
            error is BluetoothNotEnabledError || error is LocationNotEnabledError ||
                    error.cause is BluetoothNotEnabledError || error.cause is LocationNotEnabledError ||
                    error.cause is LocationPermissionNotGrantedError ->
                bus.event(Events.Sync.Glucometer.Error)

            error is GlucometerSyncError && (error.cause is GlucometerOfflineError || error.cause is TimeoutException) ->
                bus.event(Events.Sync.Glucometer.ErrorWithMessage)

            error is PrimaryGlucometerNotFoundError -> openConnectScreen()

            else -> handleError(error)
        }
    }

    private fun handleSyncAutoError(error: Throwable) {
        when (error) {
            is PrimaryGlucometerNotFoundError -> {
                startSyncWithBackendAction.consumer.accept(Unit)
            }

            is GlucometerSyncError -> startSyncWithBackendAction.consumer.accept(Unit)
            else -> handleError(error)
        }
    }

    override fun handleError(error: Throwable) {
        if (error is InvalidRefreshTokenError) {
            logOutUseCase.execute()
                .subscribe()
                .untilDestroy()
        }
        bus.event(Events.Sync.Server.Error)
        super.handleError(error)
    }

    private fun openConnectScreen() {

        //TODO: тест всех комманд глюкометра. Работает 100%.
//        testUseCase.execute()
//            .subscribe()
//            .untilDestroy()

        bus.event(Events.Sync.Glucometer.Error)
        router.startFlow(Screens.ConnectStartScreen(isOnBoarding = false))
    }

}
