package com.elta.android.presentation.analytic.model.appmetric.params

import androidx.annotation.StringDef

@StringDef(
    TurningResultParam.ALLOW,
    TurningResultParam.REJECT
)
@Retention(AnnotationRetention.RUNTIME)
annotation class TurningResultParam {
    companion object {
        const val REJECT = "reject"
        const val ALLOW = "allow"
    }
}
