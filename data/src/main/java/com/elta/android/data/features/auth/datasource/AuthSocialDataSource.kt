package com.elta.android.data.features.auth.datasource

import com.elta.android.data.features.auth.model.LoginNetworkResponse
import io.reactivex.Completable
import io.reactivex.Single

interface AuthSocialDataSource {

    fun linkSocialNetwork(name: String, token: String): Completable

    fun unLinkSocialNetwork(name: String): Completable

    fun loginSocialNetwork(name: String, token: String): Single<LoginNetworkResponse>
}
