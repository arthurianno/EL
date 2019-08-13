package com.elta.android.presentation.features.home.pm

import com.elta.android.common.errors.BluetoothNotEnabledError
import com.elta.android.common.errors.GlucometerOfflineError
import com.elta.android.common.errors.GlucometerSyncError
import com.elta.android.common.errors.LocationNotEnabledError
import com.elta.android.common.errors.LocationPermissionNotGrantedError
import com.elta.android.common.errors.PrimaryGlucometerNotFoundError
import com.elta.android.domain.features.devices.interactor.GetGlucometersUseCase
import com.elta.android.domain.features.devices.interactor.SyncWithGlucometerUseCase
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.home.interactor.GetAddableEventsUseCase
import com.elta.android.domain.features.feedback.interactor.ShouldSendFeedbackUseCase
import com.elta.android.domain.features.sync.interactor.SyncLocalChangesUseCase
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
import com.elta.android.presentation.core.pm.widgets.snackBarControl
import com.elta.android.presentation.core.ui.dialog.DialogData
import com.elta.android.presentation.core.ui.dialog.DialogResult
import com.elta.android.presentation.core.ui.snack_bar_view.SnackBarData
import com.elta.android.presentation.features.home.ui.adapter.items.UserEventItem
import com.elta.android.presentation.features.sync.control.bluetoothControl2
import com.elta.android.presentation.messages.SnackBarMessageData
import com.elta.android.presentation.utils.toIcon
import com.elta.android.presentation.utils.toName
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.rx.bindProgress
import io.reactivex.Observable
import io.reactivex.rxkotlin.Singles
import me.dmdev.rxpm.skipWhileInProgress
import me.dmdev.rxpm.widget.dialogControl
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class HomeFlowPm @Inject constructor(
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val getDevicesUseCase: GetGlucometersUseCase,
    private val updateUserInfoUseCase: UpdateUserInfoUseCase,
    private val shouldSendFeedbackUseCase: ShouldSendFeedbackUseCase,
    private val syncWithGlucometerUseCase: SyncWithGlucometerUseCase,
    private val getAddableEventsUseCase: GetAddableEventsUseCase,
    private val syncWithBackendUseCase: SyncLocalChangesUseCase,
    services: ServiceFacade
) : BaseFlowPm(services), ConnectionListener {

    val bottomSheetItems = State<List<ListItem>>()
    val closeBottomSheetCommand = Command<Unit>()
    val showBottomSheetCommand = Command<Unit>()
    val pulseCommand = Command<Boolean>()
    val homeAction = Action<Boolean>()
    val menuItemSelectedAction = Action<Int>()
    val menuItemRestoredAction = Action<Int>()
    val selectedItemIdState = State(R.id.mainMenuItemView)

    val btControl = bluetoothControl2()
    val retryDeviceNotFoundControl = snackBarControl<SnackBarData>()

    val googlePlayDialogControl = dialogControl<DialogData, DialogResult>()
    val feedbackDialogControl = dialogControl<DialogData, DialogResult>()
    val likeAppDialogControl = dialogControl<DialogData, DialogResult>()

    private val loadEvents = Action<Unit>()
    private val startSyncAction = Action<Unit>()
    private val startAutoSyncAction = Action<Unit>()
    private val syncProgressState = State(false)
    private val showRetrySyncAction = Action<Unit>()

    private val syncWithBackendProgressState = State(false)
    private val startSyncWithBackendAction = Action<Unit>()

    private val feedbackAction = Action<Unit>()
    private val likeAppDialogAction = Action<Int>()
    private val feedbackDialogAction = Action<Unit>()
    private val googlePlayDialogAction = Action<Unit>()
    private val feedbackDialogData by lazy { Dialogs.FeedbackData(resources) }
    private val googlePlayDialogData by lazy { Dialogs.GooglePlayRateData(resources) }

    private val deviceNotFound: SnackBarData by lazy {
        SnackBarMessageData.WithButton(
            resources.getString(R.string.sync_connect_device_not_found),
            resources.getString(R.string.sync_connect_button_retry),
            SNACK_BAR_DURATION
        )
    }

    override fun onCreate() {
        super.onCreate()

        bindSyncAction()
        bindSyncWithBackendAction()
        bindLikeAppDialog()
        bindGooglePlayRateDialog()
        bindFeedbackDialog()
        bindFeedbackAction()

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
            .map { it to handleBottomMenuClick(it) }
            .trackEvent { AnalyticsEvent(it.second.second) }
            .doOnNext { router.navigateToTab(it.second.first) }
            .map { it.first }
            .subscribe(selectedItemIdState.consumer)
            .untilDestroy()
    }

    private fun bindSyncAction() {
        startSyncAction.observable
            .skipWhileInProgress(syncProgressState.observable)
            .flatMap {
                syncWithGlucometer(auto = false)
                    .doOnError(::handleSyncError)
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

        showRetrySyncAction.observable
            .switchMapMaybe {
                retryDeviceNotFoundControl.showForResult(deviceNotFound)
            }
            .subscribe(startSyncAction.consumer)
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
                    .doOnError { bus.event(Events.Sync.Server.Error) }
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
            .delay(SYNC_AFTER_CONNECTION_RESTORED_DELAY, TimeUnit.MILLISECONDS)
            .filter { it }
            .map { Unit }
            .skipWhileInProgress(syncProgressState.observable)
            .subscribe(startSyncWithBackendAction.consumer)
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

    private fun handleAddEventClick(meta: Any) {
        if (meta is EventType) {
            router.startFlow(Screens.EventsCreationScreen(meta))
        } else if (meta == META_SYNC) {
            startSyncAction.consumer.accept(Unit)
        }
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
        if (events > 0) bus.event(Events.EventsChanged(true))
    }

    private fun createUserInfoParams(): UpdateUserInfoUseCase.Params =
        UpdateUserInfoUseCase.Params(UserInfo(isFeedbackSent = true))

    private fun autoSyncObservable(): Observable<Unit> =
        Singles.zip(getUserInfoUseCase.execute(), getDevicesUseCase.execute())
            .flatMapObservable { pair ->
                val info = pair.first
                val devices = pair.second

                val primary = devices.find { it.isPrimary }

                if (primary != null && info.isFirstHomeEntrance == false)
                    syncWithGlucometer(auto = true)
                        .map { Unit }
                        .doOnError(::handleSyncAutoError)
                        .doOnComplete { startSyncWithBackendAction.consumer.accept(Unit) }
                else
                    Observable.fromCallable {
                        startSyncWithBackendAction.consumer.accept(Unit)
                    }
            }

    private fun syncWithGlucometer(auto: Boolean): Observable<Int> =
        syncWithGlucometerUseCase.execute(SyncWithGlucometerUseCase.Params())
            .bindProgress(syncProgressState.consumer)
            .doOnSubscribe { bus.event(Events.Sync.Glucometer.Started) }
            .doOnComplete { bus.event(Events.Sync.Glucometer.Success) }
            .doOnError { error ->
                if (auto) {
                    if (error.cause is BluetoothNotEnabledError ||
                        error.cause is LocationNotEnabledError ||
                        error.cause is LocationPermissionNotGrantedError) {
                        bus.event(Events.Sync.Glucometer.Error)
                    } else {
                        bus.event(Events.Sync.Glucometer.ErrorWithMessage)
                    }
                } else {
                    bus.event(Events.Sync.Glucometer.Error)
                }
            }
            .onErrorResumeNext { error: Throwable ->
                when (error) {
                    is GlucometerSyncError ->
                        when (error.cause) {
                            is BluetoothNotEnabledError ->
                                btControl.requestEnableBluetooth()
                                    .filter { it }
                                    .flatMapObservable { syncWithGlucometer(auto) }
                            is LocationPermissionNotGrantedError ->
                                btControl.requestLocationPermissions()
                                    .filter { it }
                                    .flatMapObservable { syncWithGlucometer(auto) }
                            is LocationNotEnabledError ->
                                btControl.requestEnableLocation()
                                    .filter { it }
                                    .flatMapObservable { syncWithGlucometer(auto) }
                            else -> Observable.error(error)
                        }
                    else -> Observable.error(error)
                }
            }
            .doOnNext(::handleSyncCompleted)

    private fun handleSyncError(error: Throwable) {
        when (error) {
            is PrimaryGlucometerNotFoundError -> router.startFlow(Screens.FromOtherSyncFlow)
            is GlucometerSyncError ->
                when (error.cause) {
                    is GlucometerOfflineError -> showRetrySyncAction.consumer.accept(Unit)
                    else -> handleError(error)
                }
            else -> handleError(error)
        }
    }

    private fun handleSyncAutoError(error: Throwable) {
        when (error) {
            is PrimaryGlucometerNotFoundError -> startSyncWithBackendAction.consumer.accept(Unit)
            is GlucometerSyncError -> startSyncWithBackendAction.consumer.accept(Unit)
            else -> handleError(error)
        }
    }

    companion object {
        private const val OPEN_EVENT_SCREEN_DELAY = 400L
        private const val META_SYNC = "meta_sync"
        private const val SYNC_AFTER_CONNECTION_RESTORED_DELAY = 2800L
        private const val SNACK_BAR_DURATION = 5000
    }
}