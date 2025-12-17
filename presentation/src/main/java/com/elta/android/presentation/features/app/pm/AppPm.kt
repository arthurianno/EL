package com.elta.android.presentation.features.app.pm

import android.content.Context
import android.net.Uri
import android.util.Log
import coil.Coil
import coil.Coil.imageLoader
import com.elta.android.common.errors.UnauthorizedError
import com.elta.android.common.logger.FirebaseStorage
import com.elta.android.common.logger.crashlyrics.CrashlyticsReport
import com.elta.android.common.utils.hideEmail
import com.elta.android.domain.common.usecase.CleanupCachedFilesUseCase
import com.elta.android.domain.features.multiLangsConfig.interactor.GetAllScreensUseCase
import com.elta.android.domain.features.multiLangsConfig.interactor.ShouldRefreshScreenUseCase
import com.elta.android.domain.features.multiLangsConfig.interactor.UpdateLastRefreshTimeUseCase
import com.elta.android.domain.features.multiLangsConfig.model.Resource
import com.elta.android.domain.features.remoteconfig.interactor.FetchRemoteConfigUseCase
import com.elta.android.domain.features.remoteconfig.interactor.GetFeatureConfigUseCase
import com.elta.android.domain.features.rostech.interactor.RosTechUseCase
import com.elta.android.domain.features.user.interactor.GetProfileUseCase
import com.elta.android.domain.features.user.interactor.GetUserIdUseCase
import com.elta.android.domain.features.user.model.ExitFromApp
import com.elta.android.domain.features.userinfo.interactor.GetProfileSettingsUseCase
import com.elta.android.domain.features.userinfo.interactor.GetUserInfoUseCase
import com.elta.android.domain.features.userinfo.model.UserInfo
import com.elta.android.domain.features.version.interactor.CheckAppVersionUseCase
import com.elta.android.domain.features.version.model.VersionStatus
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.Events
import com.elta.android.presentation.Screens
import com.elta.android.presentation.analytic.core.appmetric.AppMetricTracker
import com.elta.android.presentation.analytic.model.analytics.AnalyticsEvent
import com.elta.android.presentation.analytic.model.analytics.AnalyticsEventParam
import com.elta.android.presentation.analytic.model.analytics.AnalyticsEventType
import com.elta.android.presentation.analytic.model.appmetric.AppMetricEvent
import com.elta.android.presentation.analytic.model.appmetric.params.SnackStatusParam
import com.elta.android.presentation.core.bus.clicks
import com.elta.android.presentation.core.bus.events
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.core.pm.listeners.ConnectionListener
import com.elta.android.presentation.features.app.model.SyncStatus
import com.elta.android.presentation.utils.cacheHelper.ImageCacheHelper
import com.elta.android.presentation.utils.dynamiclinks.DynamicLinkNavigationMapper
import com.elta.android.presentation.utils.dynamiclinks.NotificationNavigationMapper
import com.elta.android.presentation.widgets.status.Status
import com.elta.android.presentation.widgets.status.Visibility
import com.github.terrakok.cicerone.Screen
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.rx2.asObservable
import kotlinx.coroutines.rx2.rxSingle
import me.dmdev.rxpm.action
import me.dmdev.rxpm.command
import me.dmdev.rxpm.state
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.invoke

private const val EMPTY_STATUS_DELAY_MILLIS = 400L
private const val STATUS_DELAY_MILLIS = 2000L

class AppPm @Inject constructor(
    private val getUserInfo: GetUserInfoUseCase,
    private val getProfile: GetProfileUseCase,
    private val getProfileSettings: GetProfileSettingsUseCase,
    private val getUserId: GetUserIdUseCase,
    private val checkAppVersion: CheckAppVersionUseCase,
    private val cleanupFiles: CleanupCachedFilesUseCase,
    private val firebaseStorage: FirebaseStorage,
    private val crashlyticsReport: CrashlyticsReport,
    private val fetchRemoteConfigUseCase: FetchRemoteConfigUseCase,
    private val getFeatureConfigUseCase: GetFeatureConfigUseCase,
    private val getAllScreensUseCase: GetAllScreensUseCase,
    private val shouldRefreshScreensUseCase: ShouldRefreshScreenUseCase,  // <-- добавить
    private val updateLastRefreshTimeUseCase: UpdateLastRefreshTimeUseCase, // <-- добавить
    private val rosTech: RosTechUseCase,
    private val appMetric: AppMetricTracker,
    private val context: Context,
    services: ServiceFacade
) : BasePm(services), ConnectionListener {

    /**
     * Так как диплинки выключены, то используем свой механизм диплинков
     * с помощью интента и навигации
     */
    val consultantDeepLinkAction = action<Unit>()

    val imagesLoadedCommand = command<Unit>()
    private val imagesLoadedState = state<Boolean>(false)
    val newsDeepLinkAction = action<Unit>()

    val coldStartAction = action<Unit>()
    val notificationStartAction = action<Uri>()
    val deepLinkAction = action<Uri>()
    val coldStartDeepLinkAction = action<Uri>()
    val onStopAction = action<String>()
    private val checkAppVersionAction = action<Unit>()
    private val cleanupFilesAction = action<Unit>()
    private val fetchRemoteConfigAction = action<Unit>()
    private val preloadScreensAction = action<Unit>()

    val syncStatusState = state<Status>()
    val syncStatusVisibility = state<Visibility>(Visibility.Hide)

    val showOptionalUpdateDialogCommand = command<Unit>()

    @Suppress("LongMethod")
    override fun onCreate() {
        super.onCreate()

        coldStartAction.observable
            .skipWhileInProgress()
            .flatMapSingle {
                getUserInfo.execute()
                    .flatMap { userInfo ->
                        getProfileSettings.execute()
                            .map { userInfo to it.isOnboarded }
                    }
                    .doOnSuccess {
                        getUserId.execute()
                            .doOnSuccess {
                                firebaseStorage.userLogin = it
                                crashlyticsReport.setUserId(it.hideEmail())
                            }
                            .subscribe()
                            .untilDestroy()
                    }
                    .doOnSuccess { info ->
                        when {
                            info.first.isUserLoggedIn != true ->
                                router.newRootFlow(Screens.GreetingFlow)

                            info.first.isEmailConfirmed != true ->
                                router.newRootChain(
                                    Screens.GreetingFlow,
                                    Screens.ActivateProfile
                                )

                            !info.second -> router.newRootFlow(Screens.OnBoardingFlow)
                            else -> {
                                // fixme Variant A : improved_enabling_location

                                val improvedEnablingLocation = getFeatureConfigUseCase.invoke().improvedEnablingLocation
                                val screen = if (improvedEnablingLocation) Screens.HomeFlow
                                else Screens.HomeFlowVariantA
                                router.newRootFlow(screen)
                            }
                        }
                    }
                    .doOnError(::handleError)
            }
            .subscribe()
            .untilDestroy()

        networkStateCommand.observable
            .filter { it }
            .map { }
            .doOnNext(checkAppVersionAction.consumer)
            .subscribe()
            .untilDestroy()

        checkAppVersionAction.observable
            .flatMapSingle {
                checkAppVersion.execute()
                    .doOnSuccess { versionStatus ->
                        when (versionStatus) {
                            VersionStatus.OPTIONAL -> {
                                showOptionalUpdateDialogCommand.consumer.accept(Unit)
                            }

                            VersionStatus.MANDATORY -> {
                                setStatusVisibility(Visibility.Hide)
                                router.newRootFlow(Screens.ForcedUpdateScreen)
                            }

                            else -> {}
                        }
                    }
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        cleanupFilesAction.observable
            .flatMapSingle {
                Single.fromCallable { cleanupFiles() }
                    .onErrorReturn { error ->
                        Log.w("AppPM", "⚠️ Cleanup skipped: ${error.message}")
                        Unit
                    }
            }
            .subscribe()
            .untilDestroy()

        fetchRemoteConfigAction.observable
            .flatMapCompletable {
                fetchRemoteConfigUseCase.execute()
            }
            .subscribe()
            .untilDestroy()

        preloadScreensAction.observable
            .flatMapSingle {
                rxSingle { shouldRefreshScreensUseCase.invoke() }
                    .flatMap { shouldRefresh ->
                        if (shouldRefresh) {
                            Log.i("AppPM", "24 hours passed since last refresh, updating screens...")
                            rxSingle { getAllScreensUseCase.invoke() }
                                .doOnSuccess { result ->
                                    Log.i("AppPM" ,"Screens fetched, ${result} screens received.")
                                    when (result) {
                                        is Resource.Success -> {
                                            rxSingle { updateLastRefreshTimeUseCase.invoke() }
                                                .subscribe()
                                                .untilDestroy()
                                            rxSingle(Dispatchers.IO) {
                                                val totalStartTime = System.currentTimeMillis()
                                                val uniqueImages = result.data
                                                    .mapNotNull { it.backgroundImageUrl }
                                                    .distinct()

                                                Log.i(
                                                    "AppPM",
                                                    "Starting to prefetch ${uniqueImages.size} unique images"
                                                )

                                                var successCount = 0
                                                var failCount = 0
                                                val imageLoader = imageLoader(context)
                                                uniqueImages.forEachIndexed { index, imageUrl ->
                                                    val (success, duration) = ImageCacheHelper.prefetchImage(
                                                        imageUrl,
                                                        context,
                                                        imageLoader
                                                    )
                                                    if (success) {
                                                        successCount++
                                                        Log.i(
                                                            "AppPM",
                                                            "✅ Image ${index + 1}/${uniqueImages.size} prefetched in ${duration}ms"
                                                        )
                                                    } else {
                                                        failCount++
                                                        Log.w(
                                                            "AppPM",
                                                            "⚠️ Image ${index + 1}/${uniqueImages.size} failed in ${duration}ms"
                                                        )
                                                    }
                                                }
                                                val totalDuration =
                                                    System.currentTimeMillis() - totalStartTime
                                                Log.i(
                                                    "AppPM",
                                                    "Prefetch completed: $successCount succeeded, $failCount failed in ${totalDuration}ms"
                                                )
                                                Log.i("AppPM", "🚀 Calling imagesLoadedCommand...")
                                                imagesLoadedState.consumer.accept(true)
                                                imagesLoadedCommand.consumer.accept(Unit)
                                                Log.i("AppPM", "✅ imagesLoadedCommand called!")
                                            }
                                                .doOnError { error ->
                                                    Log.e(
                                                        "AppPM",
                                                        "Error prefetching images: ${error.message}"
                                                    )
                                                    Single.fromCallable {
                                                        imagesLoadedState.consumer.accept(true)
                                                        imagesLoadedCommand.consumer.accept(Unit)
                                                    }
                                                }
                                                .subscribe()
                                                .untilDestroy()
                                        }

                                        is Resource.Error -> {
                                            Log.i("AppPM", "Error: ${result.message}")
                                            imagesLoadedState.consumer.accept(true)
                                            imagesLoadedCommand.consumer.accept(Unit)
                                        }

                                        is Resource.Loading -> {
                                            Log.i("AppPM", "Loading")
                                        }
                                    }
                                }
                        } else {
                            Log.i("AppPM", "Skipping refresh, less than 24h passed")
                            Single.fromCallable {
                                imagesLoadedState.consumer.accept(true)
                                imagesLoadedCommand.consumer.accept(Unit)
                            }
                        }
                    }
            }
            .onErrorReturn { error ->
                Log.e("AppPM", "preloadScreensAction error: ${error.message}")
                imagesLoadedState.consumer.accept(true)
                imagesLoadedCommand.consumer.accept(Unit)
            }
            .subscribe()
            .untilDestroy()
        deepLinkAction.observable
            // fixme Variant A : improved_enabling_location
            .map { DynamicLinkNavigationMapper.deepLinkToScreen(it,getFeatureConfigUseCase.invoke().improvedEnablingLocation) }
            .doOnNext { router.navigateTo(it as Screen) }
            .subscribe()
            .untilDestroy()

        coldStartDeepLinkAction.observable
            .map { DynamicLinkNavigationMapper.deepLinkToScreen(
                it,
                // fixme Variant A : improved_enabling_location
                getFeatureConfigUseCase.invoke().improvedEnablingLocation
            ) }
            .doOnNext { router.newRootChain(Screens.GreetingFlow, it as Screen) }
            .subscribe()
            .untilDestroy()

        consultantDeepLinkAction.observable
            .concatMapSingle {
                Single.zip(
                    getUserId.execute(),
                    getProfile().asObservable().firstOrError()
                ) { id, profile ->
                    id to "${profile.firstName} ${profile.secondName}"
                }
            }
            .doOnNext { (id, userName) ->
                val improvedEnablingLocation = getFeatureConfigUseCase.invoke().improvedEnablingLocation
                val homeScreen = if (improvedEnablingLocation) Screens.HomeFlow
                else Screens.HomeFlowVariantA
                router.newRootChain(
                    homeScreen,
                    Screens.Support,
                    Screens.ConsultantScreen(id, userName)
                )
            }
            .doOnError(::handleError)
            .subscribe()
            .untilDestroy()

        newsDeepLinkAction.observable
            .flatMapSingle {
                getUserInfo.execute()
                    .map { userInfo -> userInfo.isUserLoggedIn }
            }
            .doOnNext { isLoggedIn ->
                if (isLoggedIn == true) {
                    val improvedEnablingLocation = getFeatureConfigUseCase.invoke().improvedEnablingLocation
                    val homeScreen = if (improvedEnablingLocation) Screens.HomeFlow
                    else Screens.HomeFlowVariantA
                    router.newRootChain(
                        homeScreen,
                        Screens.NewsScreen
                    )
                } else {
                    router.newRootChain(Screens.GreetingFlow, Screens.AuthFlow)
                }
            }
            .doOnError(::handleError)
            .subscribe()
            .untilDestroy()

        notificationStartAction.observable
            .flatMapSingle { uri ->
                getUserInfo.execute()
                    .map { Pair(uri, it) }
                    .doOnSuccess(::handleNotification)
                    .doOnError(::handleError)
            }
            .retry()
            .subscribe()
            .untilDestroy()

        onStopAction.observable
            .trackEvent {
                AnalyticsEvent(
                    AnalyticsEventType.APP_EXIT,
                    hashMapOf(AnalyticsEventParam.SCREEN_NAME to it)
                )
            }
            .subscribe()
            .untilDestroy()

        lifecycleObservable.filter { it == Lifecycle.CREATED }
            .map { }
            .take(1)
            .flatMapCompletable {
                rosTech.execute()
                    .doOnError { Timber.d("RosTech init Error") }
            }
            .subscribe()
            .untilDestroy()

        lifecycleObservable.filter { it == Lifecycle.CREATED }
            .doOnNext { appMetric.trackEvent(AppMetricEvent.AppStart) }
            .trackEvent(AnalyticsEventType.APP_LAUNCH)
            .subscribe()
            .untilDestroy()

        lifecycleObservable.filter { it == Lifecycle.CREATED }
            .map { }
            .doOnNext(cleanupFilesAction.consumer)
            .doOnNext(fetchRemoteConfigAction.consumer)
            .doOnNext(checkAppVersionAction.consumer)
            .doOnNext(preloadScreensAction.consumer)
            .subscribe()
            .untilDestroy()

        bus.events<Events.Sync>()
            .concatMap { event ->
                val delay = when {
                    !syncStatusState.hasValue() -> EMPTY_STATUS_DELAY_MILLIS // first start add delay to make smoooth
                    event is Events.Sync.Glucometer.Error ||
                            event is Events.Sync.Server.ErrorWithMessage ||
                            event is Events.Sync.Glucometer.Started ||
                            event is Events.Sync.Glucometer.Nothing -> 0L

                    else -> STATUS_DELAY_MILLIS
                }
                Observable.just(event).delay(delay, TimeUnit.MILLISECONDS)
            }
            .doOnNext {
                when (it) {
                    is Events.Sync.Glucometer.Error -> setStatusVisibility(Visibility.Hide)
                    is Events.Sync.Glucometer.ErrorWithMessage -> {
                        appMetric.trackEvent(
                            AppMetricEvent.SnackSynchronization(SnackStatusParam.SYNCHRONIZATION_ERROR)
                        )
                        setStatus(SyncStatus.Glucometer.Error(resources))
                        setStatusVisibility(Visibility.Show)
                        setStatusVisibility(Visibility.HideWithDelay)
                    }

                    is Events.Sync.Glucometer.Started -> {
                        setStatus(SyncStatus.Glucometer.Started(resources))
                        setStatusVisibility(Visibility.Show)
                    }

                    is Events.Sync.Glucometer.Success -> {
                        appMetric.trackEvent(AppMetricEvent.SnackSynchronization(SnackStatusParam.SUCCESS))
                        setStatus(SyncStatus.Glucometer.Success(resources))
                        setStatusVisibility(Visibility.HideWithDelay)
                    }

                    is Events.Sync.Glucometer.NoNewEvents -> {
                        setStatus(SyncStatus.Glucometer.NoNewEvents(resources))
                        setStatusVisibility(Visibility.HideWithDelay)
                    }

                    is Events.Sync.Server.Error -> {
                        setStatus(SyncStatus.Server.Error(resources))
                        setStatusVisibility(Visibility.HideWithDelay)
                    }

                    is Events.Sync.Server.ErrorWithMessage -> {
                        setStatus(SyncStatus.Server.ErrorWithMessage(resources))
                        setStatusVisibility(Visibility.Show)
                        setStatusVisibility(Visibility.HideWithDelay)
                    }

                    is Events.Sync.Server.Started -> {
                        setStatus(SyncStatus.Server.Started(resources))
                        setStatusVisibility(Visibility.Show)
                    }

                    is Events.Sync.Server.Success -> {
                        setStatus(SyncStatus.Server.Success(resources))
                        setStatusVisibility(Visibility.HideWithDelay)
                    }

                    Events.Sync.Glucometer.Nothing -> {
                        setStatusVisibility(Visibility.Hide)
                    }
                }
            }
            .subscribe()
            .untilDestroy()

        bus.events<Events.EmailNotConfirmed>()
            .doOnNext { appMetric.trackEvent(AppMetricEvent.ProfileVerificationError) }
            .doOnNext {
                setStatus(SyncStatus.Email(resources))
                setStatusVisibility(Visibility.Show)
                setStatusVisibility(Visibility.HideWithDelay)
            }
            .subscribe()
            .untilDestroy()

        bus.events<Events.NetworkProblemTryLater>()
            .doOnNext {
                setStatus(SyncStatus.NetworkProblemTryLater(resources))
                setStatusVisibility(Visibility.Show)
                setStatusVisibility(Visibility.HideWithDelay)
            }
            .subscribe()
            .untilDestroy()

        bus.clicks<Clicks.ProfileAdditionalClicked>()
            .map { it.item.type }
            .filter { it is ExitFromApp }
            .doOnNext {
                syncStatusVisibility.consumer.accept(Visibility.Hide)
            }
            .subscribe()
            .untilDestroy()
    }

    fun uploadLogs() {
        firebaseStorage.uploadLogFile()
    }

    override fun handleError(error: Throwable) {
        when (error) {
            is UnauthorizedError -> router.newRootFlow(Screens.GreetingFlow)
            is NoSuchElementException -> {
                router.newRootChain(
                    Screens.GreetingFlow,
                    Screens.AuthFlow
                )
            }

            else -> super.handleError(error)
        }
    }

    private fun handleNotification(pair: Pair<Uri, UserInfo>) {
        if (pair.second.isUserLoggedIn == true) {
            NotificationNavigationMapper.notificationDataToScreen(pair.first, getFeatureConfigUseCase.invoke().improvedEnablingLocation)?.let {
                router.newRootScreen(it)
            }
        } else {
            router.newRootChain(Screens.GreetingFlow, Screens.AuthFlow)
        }
    }

    private fun setStatus(status: SyncStatus) {
        syncStatusState.consumer.accept(status)
    }

    private fun setStatusVisibility(visibility: Visibility) {
        syncStatusVisibility.consumer.accept(visibility)
    }
}
