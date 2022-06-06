package com.elta.android.presentation.features.registration.main.pm

import com.elta.android.domain.features.user.model.SocialNetworkType
import com.elta.android.presentation.core.pm.ServiceFacade
import me.dmdev.rxpm.action

abstract class BaseSocialPm(services: ServiceFacade) : BaseRegistrationPm(services) {

    val fbAction = action<Unit>()
    val vkAction = action<Unit>()
    val okAction = action<Unit>()

    protected val socialAction = action<SocialNetworkType>()

    override fun onCreate() {
        super.onCreate()

        fbAction.observable
            .subscribe { socialAction.consumer.accept(SocialNetworkType.FB) }
            .untilDestroy()

        vkAction.observable
            .subscribe { socialAction.consumer.accept(SocialNetworkType.VK) }
            .untilDestroy()

        okAction.observable
            .subscribe { socialAction.consumer.accept(SocialNetworkType.OK) }
            .untilDestroy()
    }
}
