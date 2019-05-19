package com.elta.android.data.features.auth.repository

import com.elta.android.data.features.auth.datasource.AuthDataSource
import com.elta.android.data.features.auth.dto.EmailStatusDto
import com.elta.android.data.features.auth.dto.LoginDto
import com.elta.android.data.features.auth.dto.TokenOwnerDto
import com.elta.android.data.features.auth.dto.TokensDto
import com.elta.android.data.features.auth.storage.TokenStorage
import com.elta.android.data.features.common.storage.UserHolder
import com.elta.android.domain.features.auth.repository.AuthRepository
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class AuthDataRepository @Inject constructor(
    private val userHolder: UserHolder,
    private val tokenStorage: TokenStorage,
    private val source: AuthDataSource
) : AuthRepository {

    override fun register(email: String, password: String): Completable =
        source.register(email, password)
            .doOnSuccess { response ->
                saveUserCredentials(response, email)
            }
            .flatMapCompletable {
                Completable.complete()
            }

    override fun login(email: String, password: String): Single<Boolean> =
        source.login(email, password)
            .doOnSuccess { response ->
                saveUserCredentials(response.tokens, email)
            }
            .map(LoginDto::isEmailConfirmed)

    override fun isEmailConfirmed(): Single<Boolean> =
        source.isEmailConfirmed()
            .map(EmailStatusDto::isEmailConfirmed)

    override fun sendConfirmationLink(): Completable =
        source.sendConfirmationLink()

    override fun sendResetPasswordLink(email: String): Completable =
        source.sendResetPasswordLink(email)

    override fun resetPassword(token: String, newPassword: String): Completable =
        source.resetPassword(token, newPassword)

    override fun changePassword(currentPassword: String, newPassword: String): Completable =
        source.changePassword(currentPassword, newPassword)

    override fun checkTokenOwner(token: String): Single<Boolean> =
        source.checkTokenOwner(token)
            .map(TokenOwnerDto::isOwner)

    override fun confirmEmail(token: String): Completable =
        source.confirmEmail(token)
            .andThen(Completable.fromCallable { tokenStorage.refresh() })

    override fun isUserLoggedIn(): Single<Boolean> =
        Single.just(!tokenStorage.accessToken.isNullOrEmpty() &&
            !tokenStorage.refreshToken.isNullOrEmpty())

    private fun saveUserCredentials(tokens: TokensDto, email: String) {
        saveTokens(tokens)
        userHolder.currentUser = email.hashCode().toLong()
    }

    private fun saveTokens(tokens: TokensDto) {
        tokenStorage.accessToken = tokens.accessToken
        tokenStorage.refreshToken = tokens.refreshToken
    }
}