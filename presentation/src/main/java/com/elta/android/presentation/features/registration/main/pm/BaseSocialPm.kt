package com.elta.android.presentation.features.registration.main.pm

import com.elta.android.domain.features.auth.model.SocialNetwork
import com.elta.android.presentation.core.pm.ServiceFacade

abstract class BaseSocialPm(services: ServiceFacade) : BaseAuthPm(services) {

    val fbAction = Action<Unit>()
    val vkAction = Action<Unit>()
    val okAction = Action<Unit>()

    private val socialAction = Action<SocialNetwork>()
}