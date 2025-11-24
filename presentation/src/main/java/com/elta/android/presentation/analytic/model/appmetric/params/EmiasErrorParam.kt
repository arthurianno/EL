package com.elta.android.presentation.analytic.model.appmetric.params

import androidx.annotation.StringDef

@StringDef(
    EmiasErrorParam.USER_NOT_FOUND,
    EmiasErrorParam.NO_AGREEMENT,
    EmiasErrorParam.ACCOUNT_ALREADY_LINKED,
    EmiasErrorParam.INTERNAL_ERROR
)
@Retention(AnnotationRetention.RUNTIME)
annotation class EmiasErrorParam {
    companion object {
        const val USER_NOT_FOUND = "user_is_not_found"
        const val NO_AGREEMENT = "no_agreement"
        const val ACCOUNT_ALREADY_LINKED = "account_is_linked"
        const val INTERNAL_ERROR = "server_error"
    }
}
