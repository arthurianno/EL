package com.elta.android.data.features.auth.datasource

import com.elta.android.common.errors.EmailAlreadyConfirmedError
import com.elta.android.data.common.checkNetwork
import com.elta.android.data.features.auth.api.AuthApi
import com.elta.android.data.features.auth.api.request.AuthRequest
import com.elta.android.data.features.auth.api.request.RefreshRequest
import com.elta.android.data.features.auth.api.request.ResetPasswordLinkRequest
import com.elta.android.data.features.auth.api.request.ResetPasswordRequest
import com.elta.android.data.features.auth.api.request.TokenRequest
import com.elta.android.data.features.auth.dto.EmailStatusDto
import com.elta.android.data.features.auth.dto.LoginDto
import com.elta.android.data.features.auth.dto.TokenOwnerDto
import com.elta.android.data.features.auth.dto.TokensDto
import com.nullgr.core.hardware.NetworkChecker
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class AuthRemoteDataSource @Inject constructor(
    private val checker: NetworkChecker,
    private val api: AuthApi
) : AuthDataSource {

    override fun register(email: String, password: String): Single<TokensDto> =
        api.register(AuthRequest(email, password)).checkNetwork(checker)

    override fun login(email: String, password: String): Single<LoginDto> =
        api.login(AuthRequest(email, password)).checkNetwork(checker)

    override fun refresh(accessToken: String, refreshToken: String): Single<TokensDto> =
        api.refresh(RefreshRequest(accessToken, refreshToken)).checkNetwork(checker)

    override fun isEmailConfirmed(): Single<EmailStatusDto> =
        api.isEmailConfirmed().checkNetwork(checker)

    override fun sendConfirmationLink(): Completable =
        api.sendConfirmationLink().checkNetwork(checker)

    override fun sendResetPasswordLink(email: String): Completable =
        api.sendPasswordResetLink(ResetPasswordLinkRequest(email)).checkNetwork(checker)

    override fun resetPassword(token: String, newPassword: String): Completable =
        api.resetPassword(ResetPasswordRequest(token, newPassword)).checkNetwork(checker)

    override fun checkTokenOwner(token: String): Single<TokenOwnerDto> =
        api.checkTokenOwner(TokenRequest(token)).checkNetwork(checker)

    override fun confirmEmail(token: String): Completable =
        api.confirmEmail(TokenRequest(token)).checkNetwork(checker)
            .onErrorComplete { error -> error is EmailAlreadyConfirmedError }
}