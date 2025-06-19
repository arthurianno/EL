package com.elta.android.data.features.auth.repository

import com.elta.android.common.di.qualifires.Cache
import com.elta.android.data.features.auth.datasource.AuthDataSource
import com.elta.android.data.features.auth.model.EmailStatusNetworkResponse
import com.elta.android.data.features.auth.model.LoginNetworkResponse
import com.elta.android.data.features.auth.model.TokenOwnerNetworkResponse
import com.elta.android.data.features.auth.model.TokensNetworkResponse
import com.elta.android.data.features.auth.storage.TokenStorage
import com.elta.android.data.features.common.storage.PreferencesHolder
import com.elta.android.data.features.common.storage.UserHolder
import com.elta.android.data.features.user.datasource.ProfileDataSource
import com.elta.android.data.features.user.mapper.toNetwork
import com.elta.android.domain.features.auth.repository.AuthRepository
import com.elta.android.domain.features.user.model.GlucoseFormat
import com.elta.android.domain.features.user.model.Profile
import com.elta.android.domain.features.user.repository.ProfileRepository
import com.elta.android.domain.features.userinfo.model.UserInfo
import com.elta.android.domain.features.userinfo.repository.UserInfoRepository
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class AuthDataRepository @Inject constructor(
    private val userHolder: UserHolder,
    private val tokenStorage: TokenStorage,
    private val preferencesHolder: PreferencesHolder,
    private val source: AuthDataSource,
    private val userInfoRepository: UserInfoRepository,
    private val profileRepository: ProfileRepository,
    @Cache private val cachedSource: ProfileDataSource,
) : AuthRepository {

    override fun register(email: String, password: String): Completable =
        source.register(email, password)
            .doOnSuccess { response ->
                saveUserCredentials(response, email)
            }
            .flatMapCompletable {
                //TODO: тех долг. Функция не должна принимать на вход network data
                cachedSource.updateProfile(Profile(email = email, glucoseFormat = GlucoseFormat.CAPILLARY).toNetwork())
            }
            .andThen(userInfoRepository.updateUserInfo(createNewUserInfo()))

    override fun login(email: String, password: String, activateAccount: Boolean): Single<Boolean> =
        source.login(email, password, activateAccount)
            .doOnSuccess { response ->
                saveUserCredentials(response.tokens, email)
            }
            .map(LoginNetworkResponse::isEmailConfirmed)
            .flatMap { isConfirm -> //TODO: тех долг. Вынести получение настроек
                profileRepository.getProfileSettings(fromCache = false)
                    .map { isConfirm }
                    .onErrorReturn { isConfirm }
            }
            .flatMap {
                val userInfo = UserInfo(
                    isUserLoggedIn = tokenStorage.isUserLoggedIn(),
                    isEmailConfirmed = it
                )
                userInfoRepository
                    .updateUserInfo(userInfo)
                    .toSingleDefault(it)
            }

    override fun isEmailConfirmed(): Single<Boolean> =
        userInfoRepository.getUserInfo()
            .map { it.isEmailConfirmed }
            .flatMap { isConfirmed ->
                when (isConfirmed) {
                    true -> Single.just(isConfirmed)
                    else -> source.isEmailConfirmed()
                        .flatMap { email ->
                            Completable
                                .fromAction { tokenStorage.refresh() }
                                .toSingleDefault(email)
                        }
                        .map(EmailStatusNetworkResponse::isEmailConfirmed)
                        .flatMap {
                            val userInfo = UserInfo(
                                isUserLoggedIn = tokenStorage.isUserLoggedIn(),
                                isEmailConfirmed = it
                            )
                            userInfoRepository
                                .updateUserInfo(userInfo)
                                .toSingleDefault(it)
                        }
                }
            }

    override fun sendConfirmationLink(): Completable =
        source.sendConfirmationLink()

    override fun sendResetPasswordLink(email: String): Completable =
        source.sendResetPasswordLink(email)

    override fun resetPassword(token: String, newPassword: String): Completable =
        source.resetPassword(token, newPassword)

    override fun changePassword(currentPassword: String, newPassword: String): Completable =
        source.changePassword(currentPassword, newPassword)

    override fun checkTokenOwner(token: String): Single<Boolean> =
        source.checkTokenOwner(token)
            .map(TokenOwnerNetworkResponse::isOwner)

    override fun confirmEmail(token: String): Completable =
        source.confirmEmail(token)
            .andThen(Completable.fromCallable { tokenStorage.refresh() })

    override fun logout(): Completable =
        Completable.fromAction {
            tokenStorage.accessToken = null
            tokenStorage.refreshToken = null
            userHolder.currentUser = null
            preferencesHolder.manualGlucoseRemind = true
        }

    override fun deleteAccount(): Completable =
        source.deleteAccount()

    private fun saveUserCredentials(tokens: TokensNetworkResponse, email: String) {
        saveTokens(tokens)
        userHolder.currentUser = email.hashCode().toLong()
    }

    private fun saveTokens(tokens: TokensNetworkResponse) {
        tokenStorage.accessToken = tokens.accessToken
        tokenStorage.refreshToken = tokens.refreshToken
    }

    private fun createNewUserInfo(): UserInfo =
        UserInfo(
            isUserLoggedIn = tokenStorage.isUserLoggedIn(),
            isFeedbackSent = false,
            isEmailConfirmed = false
        )
}
