package com.elta.android.data.features.auth.repository

import com.elta.android.data.features.auth.datasource.AuthDataSource
import com.elta.android.data.features.auth.datasource.social.SocialNetworkDataSource
import com.elta.android.data.features.auth.dto.LoginDto
import com.elta.android.data.features.auth.storage.TokenStorage
import com.elta.android.domain.features.auth.model.SocialNetwork
import com.elta.android.domain.features.auth.model.SocialUser
import com.elta.android.domain.features.auth.repository.AuthRepository
import com.nullgr.core.rx.applyScheduler
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class AuthDataRepository @Inject constructor(
    private val tokenStorage: TokenStorage,
    private val source: AuthDataSource,
    private val socialNetworkSource: SocialNetworkDataSource,
    private val schedulersFacade: SchedulersFacade
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

    override fun linkSocialNetwork(network: SocialNetwork): Completable =
        socialNetworkSource.getToken(network)
            .switchMapCompletable { token ->
                source.linkSocialNetwork(network.name, token)
                    .applyScheduler(schedulersFacade)
            }

    override fun unLinkSocialNetwork(network: SocialNetwork): Completable =
        source.unLinkSocialNetwork(network.name)

    override fun loginWithSocialNetwork(network: SocialNetwork): Single<Boolean> =
        socialNetworkSource.getToken(network)
            .switchMapCompletable { token ->
                source.loginSocialNetwork(network.name, token)
                    .applyScheduler(schedulersFacade)
            }
            .andThen(checkEmail())

    override fun loginToSocialNetwork(network: SocialNetwork): Completable =
        socialNetworkSource.getToken(network)
            .flatMapCompletable {
                Completable.complete()
            }

    override fun getSocialUser(network: SocialNetwork): Single<SocialUser> {

    }
}