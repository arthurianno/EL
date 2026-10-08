package com.elta.android.presentation.features.registration.main.viewmodel

import android.content.Context
import com.elta.android.domain.features.auth.interactor.RegisterUseCase
import com.elta.android.presentation.utils.LocaleHelper
import javax.inject.Inject
import kotlinx.coroutines.rx2.await

class RegistrationSession @Inject constructor(
    private val registerUseCase: RegisterUseCase,
    private val telemetry: RegistrationTelemetry,
    private val context: Context
) {
    suspend fun register(email: String, password: String) {
        registerUseCase.execute(
            RegisterUseCase.Params(
                email = email,
                password = password,
                languageTag = LocaleHelper.getLanguage(context),
                countryCode = LocaleHelper.getRegion(context)
            )
        ).await()
        telemetry.registrationCompleted()
    }
}
