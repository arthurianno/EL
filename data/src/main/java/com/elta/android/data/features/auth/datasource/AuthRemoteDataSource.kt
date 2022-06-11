package com.elta.android.data.features.auth.datasource

import com.elta.android.common.errors.EmailAlreadyConfirmedError
import com.elta.android.data.features.auth.api.AuthApi
import com.elta.android.data.features.auth.api.request.AuthRequest
import com.elta.android.data.features.auth.api.request.ChangePasswordRequest
import com.elta.android.data.features.auth.api.request.ResetPasswordLinkRequest
import com.elta.android.data.features.auth.api.request.ResetPasswordRequest
import com.elta.android.data.features.auth.api.request.TokenRequest
import com.elta.android.data.features.auth.dto.EmailStatusDto
import com.elta.android.data.features.auth.dto.LoginDto
import com.elta.android.data.features.auth.dto.TokenOwnerDto
import com.elta.android.data.features.auth.dto.TokensDto
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class AuthRemoteDataSource @Inject constructor(
    private val api: AuthApi
) : AuthDataSource {

    override fun register(email: String, password: String): Single<TokensDto> =
        api.register(AuthRequest(email, password))

    override fun login(email: String, password: String): Single<LoginDto> =
        api.login(AuthRequest(email, password))

    override fun isEmailConfirmed(): Single<EmailStatusDto> =
        api.isEmailConfirmed()

    override fun sendConfirmationLink(): Completable =
        api.sendConfirmationLink()

    override fun sendResetPasswordLink(email: String): Completable =
        api.sendPasswordResetLink(ResetPasswordLinkRequest(email))

    override fun resetPassword(token: String, newPassword: String): Completable =
        api.resetPassword(ResetPasswordRequest(token, newPassword))

    override fun changePassword(currentPassword: String, newPassword: String): Completable =
        api.changePassword(ChangePasswordRequest(currentPassword, newPassword))

    override fun checkTokenOwner(token: String): Single<TokenOwnerDto> =
        api.checkTokenOwner(TokenRequest(token))

    override fun confirmEmail(token: String): Completable =
        api.confirmEmail(TokenRequest(token))
            .onErrorComplete { error -> error is EmailAlreadyConfirmedError }
}
