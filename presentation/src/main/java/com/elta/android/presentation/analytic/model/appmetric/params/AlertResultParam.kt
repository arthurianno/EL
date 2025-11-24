package com.elta.android.presentation.analytic.model.appmetric.params

import androidx.annotation.StringDef

@StringDef(
    AlertResultParam.ALLOW,
    AlertResultParam.PROHIBIT,
)
@Retention(AnnotationRetention.RUNTIME)
annotation class AlertResultParam {
    companion object {
        const val ALLOW = "allow"
        const val PROHIBIT = "prohibit"
    }
}
