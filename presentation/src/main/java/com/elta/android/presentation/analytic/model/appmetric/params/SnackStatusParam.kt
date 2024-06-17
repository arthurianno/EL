package com.elta.android.presentation.analytic.model.appmetric.params

import androidx.annotation.StringDef

@StringDef(
    SnackStatusParam.SUCCESS,
    SnackStatusParam.DEVICE_NOT_FOUND,
    SnackStatusParam.SYNCHRONIZATION_ERROR
)
@Retention(AnnotationRetention.RUNTIME)
annotation class SnackStatusParam {
    companion object {
        const val SUCCESS = "success"
        const val DEVICE_NOT_FOUND = "error_device_not_found"
        const val SYNCHRONIZATION_ERROR = "synchronization_error"
    }
}
