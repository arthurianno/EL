package com.elta.android.data.features.auth.repository

import com.elta.android.common.di.qualifires.Remote
import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.auth.datasource.AuthSocialDataSource
import com.elta.android.data.features.auth.datasource.social.datasource.SocialDataSourceFactory
import com.elta.android.data.features.auth.dto.LoginDto
import com.elta.android.data.features.auth.dto.SocialUserDto
import com.elta.android.data.features.auth.dto.TokensDto
import com.elta.android.data.features.auth.storage.TokenStorage
import com.elta.android.data.features.user.datasource.ProfileDataSource
import com.elta.android.domain.features.auth.model.SocialUser
import com.elta.android.domain.features.auth.repository.SocialRepository
import com.elta.android.domain.features.user.model.SocialNetworkType
import com.elta.android.domain.features.userinfo.model.UserInfo
import com.elta.android.domain.features.userinfo.repository.UserInfoRepository
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
    private val authSocialSource: AuthSocialDataSource,
    @Remote private val profileSource: ProfileDataSource,
    private val userInfoRepository: UserInfoRepository
) : SocialRepository {

    override fun linkSocialNetwork(network: SocialNetworkType): Completable =
        socialFactory.getDataSource(network).getToken().take(1)
            .switchMapCompletable { token ->
                authSocialSource.linkSocialNetwork(network.name, token)
                    .applyScheduler(schedulersFacade)
            }.andThen(
                profileSource.getUserProfile()
                    .flatMapCompletable { Completable.complete() }
            )

    override fun unLinkSocialNetwork(network: SocialNetworkType): Completable =
        authSocialSource.unLinkSocialNetwork(network.name)
            .andThen(socialFactory.getDataSource(network).logout())
            .andThen(profileSource.getUserProfile().flatMapCompletable { Completable.complete() })
            .applyScheduler(schedulersFacade)

    override fun loginWithSocialNetwork(network: SocialNetworkType): Single<Boolean> =
        socialFactory.getDataSource(network).getToken().take(1)
            .switchMapSingle { token ->
                authSocialSource.loginSocialNetwork(network.name, token)
                    .applyScheduler(schedulersFacade)
                    .doOnSuccess { saveTokens(it.tokens) }
                    .flatMap { login ->
                        profileSource.getUserProfile()
                            .map { login }
                    }
            }
            .map(LoginDto::isEmailConfirmed)
            .flatMapSingle {
                userInfoRepository.updateUserInfo(
                    createUserInfoWithEmailStatus(it)
                ).toSingleDefault(it)
            }
            .single(false)

    override fun loginToSocialNetwork(network: SocialNetworkType): Completable =
        socialFactory.getDataSource(network).getToken().take(1)
            .flatMapCompletable {
                Completable.complete()
            }

    override fun getSocialUser(network: SocialNetworkType): Single<SocialUser> =
        socialFactory.getDataSource(network).getSocialUser()
            .map(socialUserDtoMapper::mapFromObject)

    private fun saveTokens(tokens: TokensDto) {
        tokenStorage.accessToken = tokens.accessToken
        tokenStorage.refreshToken = tokens.refreshToken
    }

    private fun createUserInfoWithEmailStatus(isEmailConfirmed: Boolean): UserInfo =
        UserInfo(
            isUserLoggedIn = tokenStorage.isUserLoggedIn(),
            isEmailConfirmed = isEmailConfirmed
        )
}