package com.elta.android.presentation.features.registration.main.pm

import com.elta.android.presentation.Screens
import com.elta.android.presentation.analytic.model.analytics.AnalyticsEventType
import com.elta.android.presentation.core.pm.ServiceFacade
import me.dmdev.rxpm.action
import me.dmdev.rxpm.command
import me.dmdev.rxpm.state

abstract class BaseRegistrationPm(services: ServiceFacade) : BaseAuthPm(services) {

    val privacyPolicyAcceptAction = action<Boolean>()
    val privacyPolicyClickAction = action<Unit>()
    val personalDataClickAction = action<Unit>()
    val backHandleAction = action<Unit>()
    val openPrivacyPolicyCommand = command<Unit>()
    val openPersonalDataCommand = command<Unit>()

    protected val privacyPolicyAcceptedState = state<Boolean>()

    override fun onCreate() {
        super.onCreate()

        privacyPolicyClickAction.observable
            .trackEvent(AnalyticsEventType.TERMS_OF_USE)
            .subscribe(openPrivacyPolicyCommand.consumer)
            .untilDestroy()

        personalDataClickAction.observable
            .subscribe(openPersonalDataCommand.consumer)
            .untilDestroy()

        privacyPolicyAcceptAction.observable
            .subscribe(privacyPolicyAcceptedState.consumer)
            .untilDestroy()

        backHandleAction.observable
            .subscribe { router.navigateTo(Screens.GreetingFlow) }
            .untilDestroy()
    }
}
