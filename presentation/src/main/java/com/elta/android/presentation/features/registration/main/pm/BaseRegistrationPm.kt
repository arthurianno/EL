package com.elta.android.presentation.features.registration.main.pm

import com.elta.android.presentation.analytics.model.AnalyticsEventType
import com.elta.android.presentation.core.pm.ServiceFacade

abstract class BaseRegistrationPm(services: ServiceFacade) : BaseAuthPm(services) {

    val privacyPolicyAcceptAction = Action<Boolean>()
    val privacyPolicyClickAction = Action<Unit>()
    val personalDataClickAction = Action<Unit>()
    val openPrivacyPolicyCommand = Command<Unit>()
    val openPersonalDataCommand = Command<Unit>()

    protected val privacyPolicyAcceptedState = State<Boolean>()

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
    }
}