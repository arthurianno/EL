package com.elta.android.data.features.auth.repository

import com.elta.android.data.features.auth.datasource.AuthDataSource
import com.elta.android.data.features.auth.dto.LoginDto
import com.elta.android.data.features.auth.storage.TokenStorage
import com.elta.android.domain.features.auth.repository.AuthRepository
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class AuthDataRepository @Inject constructor(
    private val tokenStorage: TokenStorage,
    private val source: AuthDataSource
) : AuthRepository {

    override fun register(email: String, password: String): Completable =
        source.register(email, password)
            .doOnSuccess { tokens ->
                tokenStorage.accessToken = tokens.accessToken
                tokenStorage.refreshToken = tokens.refreshToken
            }
            .flatMapCompletable {
                Completable.complete()
            }

    override fun login(email: String, password: String): Single<Boolean> =
        source.login(email, password)
            .doOnSuccess { response ->
                val tokens = response.tokens
                tokenStorage.accessToken = tokens.accessToken
                tokenStorage.refreshToken = tokens.refreshToken
            }
            .map(LoginDto::isEmailConfirmed)

    override fun checkEmail(): Single<Boolean> =
        source.isEmailConfirmed()
            .andThen(Single.just(true))
            .onErrorReturn { false }

    override fun sendConfirmationLink(): Completable =
        source.sendConfirmationLink()

    override fun sendResetPasswordLink(email: String): Completable =
        source.sendResetPasswordLink(email)

    override fun resetPassword(token: String, newPassword: String): Completable =
        source.resetPassword(token, newPassword)
}