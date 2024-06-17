package com.elta.android.presentation.analytic.model.appmetric.params

import androidx.annotation.StringDef

@StringDef(
    EmiasStatusParam.REGISTERED,
    EmiasStatusParam.NOT_REGISTERED
)
@Retention(AnnotationRetention.RUNTIME)
annotation class EmiasStatusParam {
    companion object {
        const val REGISTERED = "registered"
        const val NOT_REGISTERED = "not_registered"
    }
}
