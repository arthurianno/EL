package com.elta.android.data.features.userinfo.datasource

import com.elta.android.common.errors.UnauthorizedError
import com.elta.android.data.features.common.cache.Cache
import com.elta.android.data.features.common.cache.CommonConditions
import com.elta.android.data.features.common.storage.UserHolder
import com.elta.android.data.features.userinfo.cache.dto.UserInfoDbEntity
import com.elta.android.domain.features.userinfo.model.UserInfo
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class UserInfoCachedDataSource @Inject constructor(
    private val userHolder: UserHolder,
    private val cache: Cache<UserInfoDbEntity>
) : UserInfoDataSource {

    override fun getUserInfo(): Single<UserInfoDbEntity> =
        Single.fromCallable {
            userHolder.currentUser?.let {
                cache.get(CommonConditions.ById(it))
                    ?: UserInfoDbEntity(
                        id = it,
                        isUserLoggedIn = true,
                        isFeedbackSent = false,
                        isEmailConfirmed = true,
                        isFirstHomeEntrance = true,
                        isFirstSync = false
                    ).also { newUser ->
                        cache.add(listOf(newUser))
                    }
            } ?: throw UnauthorizedError()
        }

    override fun updateUserInfo(userInfo: UserInfo): Completable =
        Completable.fromCallable {
            userHolder.currentUser?.let {
                cache.get(CommonConditions.ById(it))?.let { cachedInfo ->
                    val updatedInfo = cachedInfo.copy(
                        isEmailConfirmed = userInfo.isEmailConfirmed ?: cachedInfo.isEmailConfirmed,
                        isFeedbackSent = userInfo.isFeedbackSent ?: cachedInfo.isFeedbackSent,
                        isUserLoggedIn = userInfo.isUserLoggedIn ?: cachedInfo.isUserLoggedIn,
                        isFirstHomeEntrance = userInfo.isFirstHomeEntrance
                            ?: cachedInfo.isFirstHomeEntrance,
                        isFirstSync = userInfo.isFirstSync ?: cachedInfo.isFirstSync
                    )
                    cache.update(listOf(updatedInfo))
                } ?: cache.add(
                    listOf(
                        UserInfoDbEntity(
                            id = it,
                            isUserLoggedIn = true,
                            isFeedbackSent = false,
                            isEmailConfirmed = userInfo.isEmailConfirmed ?: false,
                            isFirstHomeEntrance = true,
                            isFirstSync = false
                        )
                    )
                )
            }
        }
}
