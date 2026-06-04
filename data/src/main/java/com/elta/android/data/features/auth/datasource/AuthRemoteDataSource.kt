package com.elta.android.data.features.auth.datasource

import com.elta.android.common.errors.EmailAlreadyConfirmedError
import com.elta.android.data.features.auth.api.AuthApi
import com.elta.android.data.features.auth.model.AuthNetworkRequest
import com.elta.android.data.features.auth.model.ChangePasswordNetworkRequest
import com.elta.android.data.features.auth.model.EmailStatusNetworkResponse
import com.elta.android.data.features.auth.model.LoginNetworkResponse
import com.elta.android.data.features.auth.model.RegisterNetworkRequest
import com.elta.android.data.features.auth.model.ResetPasswordLinkNetworkRequest
import com.elta.android.data.features.auth.model.ResetPasswordNetworkRequest
import com.elta.android.data.features.auth.model.TokenNetworkRequest
import com.elta.android.data.features.auth.model.TokenOwnerNetworkResponse
import com.elta.android.data.features.auth.model.TokensNetworkResponse
import com.elta.android.data.features.common.network.ApiLocaleResolver
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class AuthRemoteDataSource @Inject constructor(
    private val api: AuthApi
) : AuthDataSource {

    override fun register(
        email: String,
        password: String,
        languageTag: String?,
        countryCode: String?
    ): Single<TokensNetworkResponse> =
        api.register(
            RegisterNetworkRequest(
                email = email,
                password = password,
                activateAccount = false,
                languageTag = languageTag,
                countryCode = countryCode
            )
        )

    override fun login(email: String, password: String, activateAccount: Boolean): Single<LoginNetworkResponse> =
        api.login(AuthNetworkRequest(email, password, activateAccount))

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
