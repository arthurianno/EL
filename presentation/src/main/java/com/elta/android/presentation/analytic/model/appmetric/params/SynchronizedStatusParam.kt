package com.elta.android.presentation.analytic.model.appmetric.params

import androidx.annotation.StringDef

@StringDef(
    SynchronizedStatusParam.SUCCESS,
    SynchronizedStatusParam.ERROR,
)
@Retention(AnnotationRetention.RUNTIME)
annotation class SynchronizedStatusParam {
    companion object {
        const val SUCCESS = "succes"
        const val ERROR = "error"
    }
}
