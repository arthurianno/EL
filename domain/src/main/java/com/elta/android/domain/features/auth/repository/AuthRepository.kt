package com.elta.android.domain.features.auth.repository

import com.elta.android.domain.features.auth.model.SocialNetwork
import com.elta.android.domain.features.auth.model.SocialUser
import io.reactivex.Completable
import io.reactivex.Single

interface AuthRepository {

    fun register(email: String, password: String): Completable

    fun login(email: String, password: String): Single<Boolean>

    fun checkEmail(): Single<Boolean>

    fun sendConfirmationLink(): Completable

    fun sendResetPasswordLink(email: String): Completable

    fun resetPassword(token: String, newPassword: String): Completable

    fun linkSocialNetwork(network: SocialNetwork): Completable

    fun unLinkSocialNetwork(network: SocialNetwork): Completable

    fun loginWithSocialNetwork(network: SocialNetwork): Single<Boolean>

    fun loginToSocialNetwork(network: SocialNetwork): Completable

    fun getSocialUser(network: SocialNetwork): Single<SocialUser>
}