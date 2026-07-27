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
import com.elta.android.domain.features.multiLangsConfig.model.ErrorType
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
import com.elta.android.presentation.BuildConfig
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
import com.elta.android.presentation.utils.LocaleHelper
import com.elta.android.presentation.utils.OneSignalTags
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
private const val LANG_FLOW_TAG = "LangFlow"

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
            .doOnNext { Log.i(LANG_FLOW_TAG, "AppPm.coldStartAction triggered") }
            .flatMapSingle {
                getUserInfo.execute()
                    .onErrorResumeNext { error ->
                        if (error is UnauthorizedError) {
                            Single.just(
                                UserInfo(
                                    isUserLoggedIn = false,
                                    isEmailConfirmed = false
                                )
                            )
                        } else {
                            Single.error(error)
                        }
                    }
                    .flatMap { userInfo ->
                        if (userInfo.isUserLoggedIn == true) {
                            getProfileSettings.execute()
                                .map { userInfo to it.isOnboarded }
                        } else {
                            Single.just(userInfo to false)
                        }
                    }
                    .flatMap { info ->
                        if (info.first.isUserLoggedIn == true) {
                            getUserId.execute()
                                .doOnSuccess { userId ->
                                    firebaseStorage.userLogin = userId
                                    crashlyticsReport.setUserId(userId.hideEmail())
                                    OneSignalTags.login(userId, context)
                                }
                                .doOnError { Timber.e(it, "OneSignal login failed") }
                                .map { info }
                                .onErrorReturnItem(info)
                        } else {
                            Single.just(info)
                        }
                    }
                    .doOnSuccess { info ->
                        Log.i(
                            LANG_FLOW_TAG,
                            "AppPm.coldStart resolved: isLoggedIn=${info.first.isUserLoggedIn}, isEmailConfirmed=${info.first.isEmailConfirmed}, isOnboarded=${info.second}, appLanguage=${LocaleHelper.getLanguage(context)}"
                        )
                        when {
                            info.first.isUserLoggedIn != true -> {
                                Log.i(LANG_FLOW_TAG, "AppPm.coldStart route: guest")
                                router.newRootFlow(getGuestStartScreen())
                            }

                            info.first.isEmailConfirmed != true -> {
                                Log.i(LANG_FLOW_TAG, "AppPm.coldStart route: activate profile")
                                router.newRootChain(
                                    Screens.GreetingFlow,
                                    Screens.ActivateProfile
                                )
                            }

                            !info.second -> {
                                Log.i(LANG_FLOW_TAG, "AppPm.coldStart route: onboarding")
                                router.newRootFlow(Screens.OnBoardingFlow)
                            }
                            else -> {
                                // fixme Variant A : improved_enabling_location

                                val improvedEnablingLocation = getFeatureConfigUseCase.invoke().improvedEnablingLocation
                                val screen = if (improvedEnablingLocation) Screens.HomeFlow
                                else Screens.HomeFlowVariantA
                                Log.i(LANG_FLOW_TAG, "AppPm.coldStart route: home=${screen::class.java.simpleName}")
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
            .doOnNext { Log.i("AppPM", "🎬 preloadScreensAction triggered!") }
            .flatMapSingle {
                Log.i("AppPM", "🚀 Проверяем, нужно ли обновлять конфигурации...")

                // Сначала проверяем, нужно ли обновлять конфиги
                rxSingle { shouldRefreshScreensUseCase.invoke() }
            }
            .flatMapSingle { shouldRefresh ->
                if (shouldRefresh) {
                    Log.i("AppPM", "✅ Прошло 24+ часа, начинаем загрузку конфигураций экранов...")

                    rxSingle { getAllScreensUseCase.invoke() }
                        .doOnSuccess { Log.i("AppPM", "📦 getAllScreensUseCase завершен успешно") }
                        .doOnError { error -> Log.e("AppPM", "❌ getAllScreensUseCase ошибка: ${error.message}") }
                } else {
                    Log.i("AppPM", "⏭️ Обновление не требуется, используем кэш")
                    // Возвращаем успешный результат без загрузки
                    Single.just(Resource.Success(emptyList<com.elta.android.domain.features.multiLangsConfig.model.ScreenEntity>()))
                }
            }
            .flatMapSingle { result ->
                Log.i("AppPM", "🔀 Обрабатываем результат: ${result.javaClass.simpleName}")
                when (result) {
                    is Resource.Success -> {
                        // Если список не пустой - значит были загружены данные с сервера
                        if (result.data.isNotEmpty()) {
                            Log.i("AppPM", "✅ Получен результат: ${result.data.size} экранов")
                            Log.i("AppPM", "📋 Список слагов: ${result.data.joinToString(", ") { it.slug }}")

                            // Обновляем время последнего обновления
                            rxSingle { updateLastRefreshTimeUseCase.invoke() }
                                .doOnSuccess { Log.i("AppPM", "⏰ Время последнего обновления сохранено") }
                                .subscribe()
                                .untilDestroy()
                        } else {
                            Log.i("AppPM", "📦 Используем кэшированные конфигурации")
                        }

                        // Предзагружаем изображения (если данные есть)
                        if (result.data.isNotEmpty()) {
                            rxSingle(Dispatchers.IO) {
                                val totalStartTime = System.currentTimeMillis()
                                val uniqueImages = result.data
                                    .mapNotNull { it.backgroundImageUrl }
                                    .distinct()

                                Log.i("AppPM", "🖼️ Найдено ${uniqueImages.size} уникальных изображений для предзагрузки")
                                if (uniqueImages.isNotEmpty()) {
                                    Log.i("AppPM", "📸 URLs изображений:")
                                    uniqueImages.forEachIndexed { i, url ->
                                        Log.i("AppPM", "   ${i + 1}. $url")
                                    }
                                }

                                var successCount = 0
                                var failCount = 0
                                val imageLoader = imageLoader(context)

                                uniqueImages.forEachIndexed { index, imageUrl ->
                                    Log.d("AppPM", "⏳ Загружаем изображение ${index + 1}/${uniqueImages.size}...")
                                    val (success, duration) = ImageCacheHelper.prefetchImage(
                                        imageUrl,
                                        context,
                                        imageLoader
                                    )
                                    if (success) {
                                        successCount++
                                        Log.i("AppPM", "✅ Изображение ${index + 1}/${uniqueImages.size} загружено за ${duration}ms")
                                } else {
                                    failCount++
                                    Log.w("AppPM", "⚠️ Изображение ${index + 1}/${uniqueImages.size} не загружено за ${duration}ms")
                                }
                            }

                            val totalDuration = System.currentTimeMillis() - totalStartTime
                            Log.i("AppPM", "🏁 Предзагрузка завершена: $successCount успешно, $failCount неудачно, за ${totalDuration}ms")
                            imagesLoadedState.consumer.accept(true)
                            imagesLoadedCommand.consumer.accept(Unit)
                            Log.i("AppPM", "✅ imagesLoadedCommand вызван!")
                            Unit
                        }.doOnSuccess { Log.i("AppPM", "✅ rxSingle с предзагрузкой завершен") }
                        } else {
                            // Если используем кэш - просто отмечаем что изображения загружены
                            imagesLoadedState.consumer.accept(true)
                            imagesLoadedCommand.consumer.accept(Unit)
                            Log.i("AppPM", "✅ Используем кэшированные данные, пропускаем загрузку изображений")
                            Single.just(Unit)
                        }
                    }

                    is Resource.Error -> {
                        Log.e("AppPM", "❌ Ошибка загрузки конфигураций: ${result.message}")
                        imagesLoadedState.consumer.accept(true)
                        imagesLoadedCommand.consumer.accept(Unit)
                        Single.just(Unit)
                    }

                    is Resource.Loading -> {
                        Log.d("AppPM", "⏳ Загрузка конфигураций...")
                        Single.just(Unit)
                    }
                }
            }
            .doOnNext { Log.i("AppPM", "✅ Вся цепочка preloadScreensAction завершена") }
            .onErrorReturn { error ->
                Log.e("AppPM", "❌ Ошибка в preloadScreensAction: ${error.message}", error)
                imagesLoadedState.consumer.accept(true)
                imagesLoadedCommand.consumer.accept(Unit)
                Unit
            }
            .subscribe(
                { Log.i("AppPM", "✅ preloadScreensAction subscribe onNext") },
                { error -> Log.e("AppPM", "❌ preloadScreensAction subscribe onError: ${error.message}", error) }
            )
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

                    Events.Sync.Glucometer.Nothing,
                    Events.Sync.Glucometer.InvalidTime -> {
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

    private fun getGuestStartScreen() : com.elta.android.presentation.core.navigation.support.SupportAppScreen {
        val shouldShowLanguageSelection =
            BuildConfig.SHOW_LANGUAGE_SELECTION &&
                LocaleHelper.shouldShowLanguageSelection(context)
        val selectedLanguage = LocaleHelper.getLanguage(context)
        val nextScreen = if (shouldShowLanguageSelection) {
            Screens.LanguageSelection(isFirstLaunch = true)
        } else {
            Screens.GreetingFlow
        }
        Log.i(
            LANG_FLOW_TAG,
            "AppPm.getGuestStartScreen: shouldShowLanguageSelection=$shouldShowLanguageSelection, selectedLanguage=$selectedLanguage, next=${nextScreen::class.java.simpleName}"
        )
        return nextScreen
    }
}
