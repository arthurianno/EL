package com.elta.android.domain.features.auth.repository

import com.elta.android.domain.features.auth.model.SocialNetwork
import com.elta.android.domain.features.auth.model.SocialUser
import io.reactivex.Completable
import io.reactivex.Single

interface SocialRepository {

    fun linkSocialNetwork(network: SocialNetwork): Completable

    fun unLinkSocialNetwork(network: SocialNetwork): Completable

    fun loginWithSocialNetwork(network: SocialNetwork): Single<Boolean>

    fun loginToSocialNetwork(network: SocialNetwork): Completable

    fun getSocialUser(network: SocialNetwork): Single<SocialUser>
}