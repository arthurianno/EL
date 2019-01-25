package com.elta.android.presentation.features.registration.main.pm

import com.elta.android.domain.features.auth.model.SocialNetwork
import com.elta.android.presentation.core.pm.ServiceFacade

abstract class BaseSocialPm(services: ServiceFacade) : BaseRegistrationPm(services) {

    val fbAction = Action<Unit>()
    val vkAction = Action<Unit>()
    val okAction = Action<Unit>()

    protected val socialAction = Action<SocialNetwork>()

    override fun onCreate() {
        super.onCreate()

        fbAction.observable
            .subscribe { socialAction.consumer.accept(SocialNetwork.FB) }
            .untilDestroy()

        vkAction.observable
            .subscribe { socialAction.consumer.accept(SocialNetwork.VK) }
            .untilDestroy()

        okAction.observable
            .subscribe { socialAction.consumer.accept(SocialNetwork.OK) }
            .untilDestroy()
    }
}