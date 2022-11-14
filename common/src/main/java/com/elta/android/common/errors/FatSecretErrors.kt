package com.elta.android.common.errors

sealed class FatSecretErrors(
    message: String?,
    cause: Throwable?
) : RuntimeException(message, cause) {
    class TokenError(
        message: String? = null,
        cause: Throwable? = null
    ) : FatSecretErrors(message, cause)
}
