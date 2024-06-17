package com.elta.android.presentation.analytic.model.appmetric.params

import androidx.annotation.StringDef

@StringDef(
    ConnectingTypeParam.PIN_ENTER,
    ConnectingTypeParam.DMC_SCAN,
)
@Retention(AnnotationRetention.RUNTIME)
annotation class ConnectingTypeParam {
    companion object {
        const val PIN_ENTER = "enter_pin"
        const val DMC_SCAN = "scan_dmc"
    }
}
