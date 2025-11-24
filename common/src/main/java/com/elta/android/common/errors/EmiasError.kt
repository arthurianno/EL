package com.elta.android.common.errors

sealed class EmiasError: Exception() {
    object UserInEmiasNotFound : EmiasError()
    object AgreementForEmiasUsageNotFound : EmiasError()
    object OmsAlreadyLinked : EmiasError()
    object EmiasInternalError : EmiasError()

    companion object {
        const val USER_IN_EMIAS_NOT_FOUND = "user-in-emias-not-found"
        const val AGREEMENT_FOR_EMIAS_USAGE_NOT_FOUND = "agreement-for-emias-usage-not-found"
        const val OMS_ALREADY_LINKED = "oms-already-linked"
        const val EMIAS_INTERNAL_ERROR = "emias-internal-error"
    }

}

