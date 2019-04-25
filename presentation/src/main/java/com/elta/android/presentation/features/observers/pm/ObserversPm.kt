package com.elta.android.presentation.features.observers.pm

import com.elta.android.presentation.core.pm.BaseListPm
import com.elta.android.presentation.core.pm.ServiceFacade
import javax.inject.Inject

class ObserversPm @Inject constructor(
    services: ServiceFacade
) : BaseListPm(services) {

    override fun onCreate() {
        super.onCreate()
    }

    override fun onBind() {
        super.onBind()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onUnbind() {
        super.onUnbind()
    }
}