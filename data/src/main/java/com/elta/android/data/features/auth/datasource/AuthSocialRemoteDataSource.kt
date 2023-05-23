package com.elta.android.data.features.auth.datasource

import com.elta.android.data.features.auth.api.SocialApi
import com.elta.android.data.features.auth.model.LoginNetworkResponse
import com.elta.android.data.features.auth.model.SocialNetworkRequest
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class AuthSocialRemoteDataSource @Inject constructor(
    private val api: SocialApi
) : AuthSocialDataSource {

    override fun linkSocialNetwork(name: String, token: String): Completable =
        api.linkSocialNetwork(name, SocialNetworkRequest(token))

    override fun unLinkSocialNetwork(name: String): Completable =
        api.unLinkSocialNetwork(name)

    override fun loginSocialNetwork(name: String, token: String): Single<LoginNetworkResponse> =
        api.loginSocialNetwork(name, SocialNetworkRequest(token))
}
