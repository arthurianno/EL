package com.elta.android.data.features.auth.datasource

import com.elta.android.data.features.auth.api.AuthApi
import com.elta.android.data.features.auth.api.request.AuthRequest
import com.elta.android.data.features.auth.api.request.RefreshRequest
import com.elta.android.data.features.auth.dto.LoginDto
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

    override fun refresh(accessToken: String, refreshToken: String): Single<TokensDto> =
        api.refresh(RefreshRequest(accessToken, refreshToken))

    override fun isEmailConfirmed(): Completable =
        api.checkEmail()
}