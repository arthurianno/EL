package com.elta.android.presentation.features.greeting.pm

import android.content.Context
import android.util.Log
import coil.imageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.elta.android.domain.features.multiLangsConfig.interactor.GetScreenConfigFromCache
import com.elta.android.domain.features.multiLangsConfig.model.Resource
import com.elta.android.domain.features.multiLangsConfig.model.ScreenEntity
import com.elta.android.presentation.Screens
import com.elta.android.presentation.analytic.core.appmetric.AppMetricTracker
import com.elta.android.presentation.analytic.model.analytics.AnalyticsEventType
import com.elta.android.presentation.analytic.model.appmetric.AppMetricEvent
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.dmdev.rxpm.action
import me.dmdev.rxpm.state
import javax.inject.Inject

class GreetingPm @Inject constructor(
    private val appMetric: AppMetricTracker,
    services: ServiceFacade,
    private val getScreenFromCacheUseCase: GetScreenConfigFromCache,
    private val context: Context
) : BasePm(services) {

    val menuAction = action<Unit>()

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val registrationAction = action<Unit>()
    val screenConfigState = state<ScreenEntity?>()
    val imagePreloadState = state<Boolean>()



    init {
        coroutineScope.launch {
            when (val result = getScreenFromCacheUseCase("registration-screen")) {
                is Resource.Success -> {
                    val screenEntity = result.data
                    screenConfigState.consumer.accept(screenEntity)

                    // Предзагружаем картинку
                    val imageUrl = screenEntity.backgroundImageUrl
                    if (imageUrl != null) {
                        preloadImage(imageUrl)
                    } else {
                        imagePreloadState.consumer.accept(true) // нет картинки = сразу готово
                    }
                }
                is Resource.Error -> {
                    Log.e("GreetingPm", "Error loading screen config: ${result.message}")
                }
                is Resource.Loading -> {
                    Log.d("GreetingPm", "Loading screen config...")
                }
            }
        }
    }


    private suspend fun preloadImage(url: String) = withContext(Dispatchers.Main) {
        val request = ImageRequest.Builder(context)
            .data(url)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .listener(
                onSuccess = { _, _ ->
                    imagePreloadState.consumer.accept(true)
                },
                onError = { _, _ ->
                    imagePreloadState.consumer.accept(true) // всё равно показываем
                }
            )
            .build()

        context.imageLoader.execute(request)
    }

    override fun onCreate() {
        super.onCreate()

        menuAction.observable
            .doOnNext { appMetric.trackEvent(AppMetricEvent.AuthorizationClick) }
            .subscribe { router.navigateTo(Screens.AuthFlow) }
            .untilDestroy()

        registrationAction.observable
            .trackEvent(AnalyticsEventType.REGISTER_OPEN)
            .doOnNext { appMetric.trackEvent(AppMetricEvent.RegistrationClick) }
            .subscribe { router.navigateTo(Screens.RegistrationFlow) }
            .untilDestroy()
    }

    override fun onDestroy() {
        coroutineScope.cancel()
        super.onDestroy()
    }
}
