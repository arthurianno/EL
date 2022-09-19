package com.elta.android.data.features.userinfo.datasource

import com.elta.android.data.features.common.cache.Cache
import com.elta.android.data.features.common.cache.CommonConditions
import com.elta.android.data.features.common.storage.UserHolder
import com.elta.android.data.features.userinfo.cache.dto.UserInfoCacheDto
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class UserInfoCachedDataSource @Inject constructor(
    private val userHolder: UserHolder,
    private val cache: Cache<UserInfoCacheDto>
) : UserInfoDataSource {

    override fun getUserInfo(): Single<UserInfoCacheDto> =
        Single.fromCallable {
            userHolder.currentUser?.let {
                cache.get(CommonConditions.ById(it))
                    ?: throw NoSuchElementException("Current user is empty.")
            } ?: throw NoSuchElementException("Current user is not exist.")
        }

    override fun updateUserInfo(userInfo: UserInfoCacheDto): Completable =
        Completable.fromCallable {
            userHolder.currentUser?.let {
                cache.get(CommonConditions.ById(it))?.let { cachedInfo ->
                    val updatedInfo = cachedInfo.copy(
                        id = userInfo.id,
                        isEmailConfirmed = userInfo.isEmailConfirmed || cachedInfo.isEmailConfirmed,
                        isFeedbackSent = userInfo.isFeedbackSent || cachedInfo.isFeedbackSent,
                        isUserLoggedIn = userInfo.isUserLoggedIn || cachedInfo.isUserLoggedIn,
                        isOnboardingPassed = userInfo.isOnboardingPassed ||
                            cachedInfo.isOnboardingPassed,
                        isFirstHomeEntrance = userInfo.isFirstHomeEntrance && cachedInfo.isFirstHomeEntrance
                    )
                    cache.update(listOf(updatedInfo))
                } ?: cache.add(listOf(userInfo))
            }
        }
}
