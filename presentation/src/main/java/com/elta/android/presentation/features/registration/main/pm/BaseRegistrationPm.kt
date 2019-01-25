package com.elta.android.presentation.features.registration.main.pm

import com.elta.android.presentation.core.pm.ServiceFacade

abstract class BaseRegistrationPm(services: ServiceFacade) : BaseAuthPm(services) {

    val privacyPolicyAcceptAction = Action<Boolean>()
    val privacyPolicyClickAction = Action<Unit>()
    val openPrivacyPolicyCommand = Command<Unit>()

    protected val privacyPolicyAcceptedState = State<Boolean>()

    override fun onCreate() {
        super.onCreate()

        privacyPolicyClickAction.observable
            .subscribe(openPrivacyPolicyCommand.consumer)
            .untilDestroy()

        privacyPolicyAcceptAction.observable
            .subscribe(privacyPolicyAcceptedState.consumer)
            .untilDestroy()
    }
}