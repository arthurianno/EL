package com.elta.android.data.features.auth.datasource

import com.elta.android.data.common.checkNetwork
import com.elta.android.data.features.auth.api.SocialApi
import com.elta.android.data.features.auth.api.request.SocialNetworkRequest
import com.elta.android.data.features.auth.dto.LoginDto
import com.nullgr.core.hardware.NetworkChecker
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class AuthSocialRemoteDataSource @Inject constructor(
    private val checker: NetworkChecker,
    private val api: SocialApi
) : AuthSocialDataSource {

    override fun linkSocialNetwork(name: String, token: String): Completable =
        api.linkSocialNetwork(name, SocialNetworkRequest(token)).checkNetwork(checker)

    override fun unLinkSocialNetwork(name: String): Completable =
        api.unLinkSocialNetwork(name).checkNetwork(checker)

    override fun loginSocialNetwork(name: String, token: String): Single<LoginDto> =
        api.loginSocialNetwork(name, SocialNetworkRequest(token)).checkNetwork(checker)
}