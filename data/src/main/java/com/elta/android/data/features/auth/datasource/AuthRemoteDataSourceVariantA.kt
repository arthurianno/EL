package com.elta.android.data.features.auth.datasource

import com.elta.android.common.errors.EmailAlreadyConfirmedError
import com.elta.android.data.features.auth.api.AuthApiVariantA
import com.elta.android.data.features.auth.model.AuthNetworkRequestVariantA
import com.elta.android.data.features.auth.model.ChangePasswordNetworkRequest
import com.elta.android.data.features.auth.model.EmailStatusNetworkResponse
import com.elta.android.data.features.auth.model.LoginNetworkResponse
import com.elta.android.data.features.auth.model.ResetPasswordLinkNetworkRequest
import com.elta.android.data.features.auth.model.ResetPasswordNetworkRequest
import com.elta.android.data.features.auth.model.TokenNetworkRequest
import com.elta.android.data.features.auth.model.TokenOwnerNetworkResponse
import com.elta.android.data.features.auth.model.TokensNetworkResponse
import com.elta.android.data.features.common.network.ApiLocaleResolver
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

// fixme Variant A : recovery_account
class AuthRemoteDataSourceVariantA @Inject constructor(
    private val api: AuthApiVariantA
) : AuthDataSourceVariantA {

    override fun register(email: String, password: String): Single<TokensNetworkResponse> =
        api.register(AuthNetworkRequestVariantA(email, password))

    override fun login(email: String, password: String): Single<LoginNetworkResponse> =
        api.login(AuthNetworkRequestVariantA(email, password))

    override fun isEmailConfirmed(): Single<EmailStatusNetworkResponse> =
        api.isEmailConfirmed()

    override fun sendConfirmationLink(): Completable =
        api.sendConfirmationLink()

    override fun sendResetPasswordLink(email: String): Completable =
        api.sendPasswordResetLink(
            ResetPasswordLinkNetworkRequest(
                email = email,
                languageTag = ApiLocaleResolver.languageTag()
            )
        )

    override fun resetPassword(token: String, newPassword: String): Completable =
        api.resetPassword(ResetPasswordNetworkRequest(token, newPassword))

    override fun changePassword(currentPassword: String, newPassword: String): Completable =
        api.changePassword(ChangePasswordNetworkRequest(currentPassword, newPassword))

    override fun checkTokenOwner(token: String): Single<TokenOwnerNetworkResponse> =
        api.checkTokenOwner(TokenNetworkRequest(token))

    override fun confirmEmail(token: String): Completable =
        api.confirmEmail(TokenNetworkRequest(token))
            .onErrorComplete { error -> error is EmailAlreadyConfirmedError }

    override fun deleteAccount(): Completable =
        api.deleteAccount()
}
