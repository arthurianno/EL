package com.elta.android.presentation.features.greeting.pm

import android.content.Context
import android.util.Log
import coil.imageLoader
import com.elta.android.domain.features.multiLangsConfig.interactor.GetScreenConfigFromCache
import com.elta.android.domain.features.multiLangsConfig.model.Resource
import com.elta.android.domain.features.multiLangsConfig.model.ScreenEntity
import com.elta.android.presentation.Screens
import com.elta.android.presentation.analytic.core.appmetric.AppMetricTracker
import com.elta.android.presentation.analytic.model.analytics.AnalyticsEventType
import com.elta.android.presentation.analytic.model.appmetric.AppMetricEvent
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ScreenConfigurable
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.utils.cacheHelper.ImageCacheHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import me.dmdev.rxpm.action
import me.dmdev.rxpm.state
import javax.inject.Inject

class GreetingPm @Inject constructor(
    private val appMetric: AppMetricTracker,
    services: ServiceFacade,
    private val context: Context
) : BasePm(services), ScreenConfigurable {

    val menuAction = action<Unit>()

    override val screenConfigKey = "login-screen"
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val registrationAction = action<Unit>()



    override fun onCreate() {
        super.onCreate()
        loadScreenConfig(context)
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
