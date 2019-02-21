package com.elta.android.data.features.auth.repository

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.auth.datasource.AuthSocialDataSource
import com.elta.android.data.features.auth.datasource.social.datasource.SocialDataSourceFactory
import com.elta.android.data.features.auth.dto.LoginDto
import com.elta.android.data.features.auth.dto.SocialUserDto
import com.elta.android.data.features.auth.dto.TokensDto
import com.elta.android.data.features.auth.storage.TokenStorage
import com.elta.android.domain.features.auth.model.SocialNetwork
import com.elta.android.domain.features.auth.model.SocialUser
import com.elta.android.domain.features.auth.repository.SocialRepository
import com.nullgr.core.rx.applyScheduler
import com.nullgr.core.rx.schedulers.SchedulersFacade
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class SocialDataRepository @Inject constructor(
    private val socialFactory: SocialDataSourceFactory,
    private val socialUserDtoMapper: Mapper<SocialUserDto, SocialUser>,
    private val schedulersFacade: SchedulersFacade,
    private val tokenStorage: TokenStorage,
    private val source: AuthSocialDataSource
) : SocialRepository {

    override fun linkSocialNetwork(network: SocialNetwork): Completable =
        socialFactory.getDataSource(network).getToken().take(1)
            .switchMapCompletable { token ->
                source.linkSocialNetwork(network.name, token)
                    .applyScheduler(schedulersFacade)
            }

    override fun unLinkSocialNetwork(network: SocialNetwork): Completable =
        source.unLinkSocialNetwork(network.name)

    override fun loginWithSocialNetwork(network: SocialNetwork): Single<Boolean> =
        socialFactory.getDataSource(network).getToken().take(1)
            .switchMapSingle { token ->
                source.loginSocialNetwork(network.name, token)
                    .applyScheduler(schedulersFacade)
                    .doOnSuccess { response ->
                        saveTokens(response.tokens)
                        // TODO: set current user
                    }
            }
            .map(LoginDto::isEmailConfirmed)
            .single(false)

    override fun loginToSocialNetwork(network: SocialNetwork): Completable =
        socialFactory.getDataSource(network).getToken().take(1)
            .flatMapCompletable {
                Completable.complete()
            }

    override fun getSocialUser(network: SocialNetwork): Single<SocialUser> =
        socialFactory.getDataSource(network).getSocialUser()
            .map(socialUserDtoMapper::mapFromObject)

    private fun saveTokens(tokens: TokensDto) {
        tokenStorage.accessToken = tokens.accessToken
        tokenStorage.refreshToken = tokens.refreshToken
    }
}