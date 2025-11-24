package com.elta.android.presentation.analytic.model.appmetric.params

import androidx.annotation.StringDef

@StringDef(
    ConnectingTypeParam.PIN_ENTER,
    ConnectingTypeParam.DMC_SCAN,
)
@Retention(AnnotationRetention.RUNTIME)
annotation class GlucoseFormatParam {
    companion object {
        const val PLASMA = "plasma"
        const val CAPILLARY = "cappilary"
    }
}
