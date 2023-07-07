package com.elta.android.presentation.features.app.pm

import android.net.Uri
import com.elta.android.common.errors.UnauthorizedError
import com.elta.android.common.logger.FirebaseStorage
import com.elta.android.domain.features.user.interactor.GetUserIdUseCase
import com.elta.android.domain.features.user.model.ExitFromApp
import com.elta.android.domain.features.userinfo.interactor.GetProfileSettingsUseCase
import com.elta.android.domain.features.userinfo.interactor.GetUserInfoUseCase
import com.elta.android.domain.features.userinfo.model.UserInfo
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.Events
import com.elta.android.presentation.R
import com.elta.android.presentation.Screens
import com.elta.android.presentation.analytics.model.AnalyticsEvent
import com.elta.android.presentation.analytics.model.AnalyticsEventParam
import com.elta.android.presentation.analytics.model.AnalyticsEventType
import com.elta.android.presentation.core.bus.clicks
import com.elta.android.presentation.core.bus.events
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.core.pm.listeners.ConnectionListener
import com.elta.android.presentation.utils.dynamiclinks.DynamicLinkNavigationMapper
import com.elta.android.presentation.utils.dynamiclinks.NotificationNavigationMapper
import com.elta.android.presentation.widgets.status.Status
import com.elta.android.presentation.widgets.status.Visibility
import com.github.terrakok.cicerone.Screen
import com.nullgr.core.resources.ResourceProvider
import io.reactivex.Observable
import me.dmdev.rxpm.action
import me.dmdev.rxpm.state
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val EMPTY_STATUS_DELAY_MILLIS = 400L
private const val STATUS_DELAY_MILLIS = 2000L

class AppPm @Inject constructor(
    private val getUserInfo: GetUserInfoUseCase,
    private val getProfileSettings: GetProfileSettingsUseCase,
    private val getUserId: GetUserIdUseCase,
    private val firebaseStorage: FirebaseStorage,
    services: ServiceFacade
) : BasePm(services), ConnectionListener {

    val coldStartAction = action<Unit>()
    val notificationStartAction = action<Uri>()
    val deepLinkAction = action<Uri>()
    val coldStartDeepLinkAction = action<Uri>()
    val onStopAction = action<String>()

    val syncStatusState = state<Status>()
    val syncStatusVisibility = state<Visibility>(Visibility.Hide)

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
                    .doOnSuccess { info ->
                        getUserId.execute()
                            .doOnSuccess { firebaseStorage.userLogin = it }
                            .subscribe()
                        when {
                            info.first.isUserLoggedIn != true -> router.newRootFlow(Screens.GreetingFlow)
                            info.first.isEmailConfirmed != true -> router.newRootChain(
                                Screens.GreetingFlow,
                                Screens.ActivateProfile
                            )

                            !info.second -> router.newRootFlow(Screens.OnBoardingFlow)
                            else -> router.newRootFlow(Screens.HomeFlow)
                        }
                    }
                    .doOnError(::handleError)
                    .bindProgress()
            }
            .retry()
            .subscribe()
            .untilDestroy()

        deepLinkAction.observable
            .map { DynamicLinkNavigationMapper.deepLinkToScreen(it) }
            .doOnNext { router.navigateTo(it as Screen) }
            .subscribe()
            .untilDestroy()

        coldStartDeepLinkAction.observable
            .map { DynamicLinkNavigationMapper.deepLinkToScreen(it) }
            .doOnNext { router.newRootChain(Screens.GreetingFlow, it as Screen) }
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

        lifecycleObservable.filter { it == Lifecycle.CREATED }.map { Unit }
            .trackEvent(AnalyticsEventType.APP_LAUNCH)
            .subscribe()
            .untilDestroy()

        bus.events<Events.Sync>()
            .concatMap { event ->
                val delay = when {
                    !syncStatusState.hasValue() -> EMPTY_STATUS_DELAY_MILLIS // first start add delay to make smoooth
                    event is Events.Sync.Glucometer.Error -> 0L
                    event is Events.Sync.Glucometer.Started -> 0L
                    else -> STATUS_DELAY_MILLIS
                }
                Observable.just(event).delay(delay, TimeUnit.MILLISECONDS)
            }
            .doOnNext {
                when (it) {
                    is Events.Sync.Glucometer.Error -> setStatusVisibility(Visibility.Hide)
                    is Events.Sync.Glucometer.ErrorWithMessage -> {
                        setStatus(SyncStatus.Glucometer.Error(resources))
                        setStatusVisibility(Visibility.Show)
                    }

                    is Events.Sync.Glucometer.Started -> {
                        setStatus(SyncStatus.Glucometer.Started(resources))
                        setStatusVisibility(Visibility.Show)
                    }

                    is Events.Sync.Glucometer.Success -> {
                        setStatus(SyncStatus.Glucometer.Success(resources))
                        setStatusVisibility(Visibility.HideWithDelay)
                    }

                    is Events.Sync.Server.Error -> {
                        setStatus(SyncStatus.Server.Success(resources))
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
            NotificationNavigationMapper.notificationDataToScreen(pair.first)?.let { it ->
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

    private sealed class SyncStatus : Status {
        sealed class Glucometer : SyncStatus() {
            data class Started(
                val resources: ResourceProvider,
                override val text: String = resources.getString(R.string.sync_with_glucometer_in_progress),
                override val color: Int = resources.getColor(R.color.color_background_sync_started)
            ) : SyncStatus()

            data class Success(
                val resources: ResourceProvider,
                override val text: String = resources.getString(R.string.sync_with_glucometer_completed),
                override val color: Int = resources.getColor(R.color.color_background_sync_finished)
            ) : SyncStatus()

            data class Error(
                val resources: ResourceProvider,
                override val text: String = resources.getString(R.string.sync_with_glucometer_error),
                override val color: Int = resources.getColor(R.color.color_background_sync_error)
            ) : SyncStatus()
        }

        sealed class Server : SyncStatus() {
            data class Started(
                val resources: ResourceProvider,
                override val text: String = resources.getString(R.string.sync_with_backend_in_progress),
                override val color: Int = resources.getColor(R.color.color_background_backend_sync_started)
            ) : SyncStatus()

            data class Success(
                val resources: ResourceProvider,
                override val text: String = resources.getString(R.string.sync_with_backend_complete),
                override val color: Int = resources.getColor(R.color.color_background_backend_sync_finished)
            ) : SyncStatus()
        }
    }
}
