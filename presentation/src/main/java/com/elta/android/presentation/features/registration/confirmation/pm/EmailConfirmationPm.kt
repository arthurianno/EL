package com.elta.android.presentation.features.registration.confirmation.pm

import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.nullgr.core.rx.zipWithTimer
import javax.inject.Inject

class EmailConfirmationPm @Inject constructor(
    services: ServiceFacade
) : BasePm(services){

    private val token = State<String>()

    override fun onCreate() {
        super.onCreate()
    }

    fun passToken(token: String) {
        this.token.consumer.accept(token)
    }
}