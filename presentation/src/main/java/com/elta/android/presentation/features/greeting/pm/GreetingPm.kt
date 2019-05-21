package com.elta.android.presentation.features.greeting.pm

import com.elta.android.presentation.analytics.model.AnalyticsEventType
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import javax.inject.Inject

class GreetingPm @Inject constructor(
    services: ServiceFacade
) : BasePm(services) {

    val menuAction = Action<Unit>()
    val registrationAction = Action<Unit>()

    override fun onCreate() {
        super.onCreate()

        menuAction.observable
            .subscribe { router.navigateTo(Screens.AuthFlow) }
            .untilDestroy()

        registrationAction.observable
            .trackEvent(AnalyticsEventType.REGISTER_OPEN)
            .subscribe { router.navigateTo(Screens.RegistrationFlow) }
            .untilDestroy()
    }
}