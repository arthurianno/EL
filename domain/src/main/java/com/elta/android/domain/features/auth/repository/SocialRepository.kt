package com.elta.android.domain.features.auth.repository

import com.elta.android.domain.features.user.model.SocialNetworkType
import com.elta.android.domain.features.auth.model.SocialUser
import io.reactivex.Completable
import io.reactivex.Single

interface SocialRepository {

    fun linkSocialNetwork(network: SocialNetworkType): Completable

    fun unLinkSocialNetwork(network: SocialNetworkType): Completable

    fun loginWithSocialNetwork(network: SocialNetworkType): Single<Boolean>

    fun loginToSocialNetwork(network: SocialNetworkType): Completable

    fun getSocialUser(network: SocialNetworkType): Single<SocialUser>
}