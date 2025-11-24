package com.elta.android.presentation.features.greeting.pm

import com.elta.android.presentation.Screens
import com.elta.android.presentation.analytic.core.appmetric.AppMetricTracker
import com.elta.android.presentation.analytic.model.analytics.AnalyticsEventType
import com.elta.android.presentation.analytic.model.appmetric.AppMetricEvent
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import me.dmdev.rxpm.action
import javax.inject.Inject

class GreetingPm @Inject constructor(
    private val appMetric: AppMetricTracker,
    services: ServiceFacade
) : BasePm(services) {

    val menuAction = action<Unit>()
    val registrationAction = action<Unit>()

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
}
