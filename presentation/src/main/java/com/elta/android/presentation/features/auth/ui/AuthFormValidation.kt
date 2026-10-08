package com.elta.android.presentation.features.auth.ui

import com.elta.android.domain.features.auth.interactor.isEmailValid
import com.elta.android.domain.features.auth.interactor.isPasswordValid

internal fun emailInputError(value: String, message: String): String? =
    message.takeIf { value.isNotEmpty() && !isEmailValid(value) }

internal fun passwordInputError(value: String, message: String): String? =
    message.takeIf { value.isNotEmpty() && !isPasswordValid(value) }
