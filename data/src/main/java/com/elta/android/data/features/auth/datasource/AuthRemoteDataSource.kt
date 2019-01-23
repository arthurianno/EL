package com.elta.android.data.features.auth.datasource

import com.elta.android.data.common.checkNetwork
import com.elta.android.data.features.auth.api.AuthApi
import com.elta.android.data.features.auth.api.request.AuthRequest
import com.elta.android.data.features.auth.api.request.RefreshRequest
import com.elta.android.data.features.auth.dto.LoginDto
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

    override fun isEmailConfirmed(): Completable =
        api.checkEmail().checkNetwork(checker)

    override fun sendConfirmationLink(): Completable =
        api.sendConfirmationLink().checkNetwork(checker)
}