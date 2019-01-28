package com.elta.android.data.features.auth.datasource

import com.elta.android.data.features.auth.dto.LoginDto
import com.elta.android.data.features.auth.dto.TokensDto
import io.reactivex.Completable
import io.reactivex.Single

interface AuthDataSource {

    fun register(email: String, password: String): Single<TokensDto>

    fun login(email: String, password: String): Single<LoginDto>

    fun refresh(accessToken: String, refreshToken: String): Single<TokensDto>

    fun sendConfirmationLink(): Completable

    fun isEmailConfirmed(): Completable

    fun sendResetPasswordLink(email: String): Completable

    fun resetPassword(token: String, newPassword: String): Completable
}