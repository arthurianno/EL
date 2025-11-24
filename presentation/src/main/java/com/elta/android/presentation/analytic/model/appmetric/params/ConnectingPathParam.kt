package com.elta.android.presentation.analytic.model.appmetric.params

import androidx.annotation.StringDef

@StringDef(
    ConnectingPathParam.ONBOARDING,
    ConnectingPathParam.SYNCHRONIZATION,
    ConnectingPathParam.MY_DEVICES
)
@Retention(AnnotationRetention.RUNTIME)
annotation class ConnectingPathParam {
    companion object {
        const val ONBOARDING = "onboarding"
        const val SYNCHRONIZATION = "synchronization"
        const val MY_DEVICES = "my_devices"
    }
}
