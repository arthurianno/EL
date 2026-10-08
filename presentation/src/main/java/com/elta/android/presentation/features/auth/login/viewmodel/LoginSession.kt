package com.elta.android.presentation.features.auth.login.viewmodel

import com.elta.android.domain.features.auth.interactor.LoginUseCase
import com.elta.android.domain.features.auth.interactor.RestoreProfileUseCase
import com.elta.android.domain.features.user.interactor.GetUserIdUseCase
import javax.inject.Inject
import kotlinx.coroutines.rx2.await

class LoginSession @Inject constructor(
    private val login: LoginUseCase,
    private val restoreProfile: RestoreProfileUseCase,
    private val getUserId: GetUserIdUseCase,
    private val telemetry: LoginTelemetry,
    private val postAuth: LoginPostAuth
) {
    suspend fun authenticate(email: String, password: String): Boolean {
        val isActivated = login.execute(LoginUseCase.Params(email, password)).await()
        telemetry.loginSucceeded()
        if (isActivated) {
            telemetry.accountIdentified(getUserId.execute().await())
        }
        return isActivated
    }

    suspend fun restore(email: String, password: String): Boolean =
        restoreProfile.execute(RestoreProfileUseCase.Params(email, password)).await()

    internal suspend fun complete(isActivated: Boolean): LoginDestination =
        postAuth.complete(isActivated)
}
