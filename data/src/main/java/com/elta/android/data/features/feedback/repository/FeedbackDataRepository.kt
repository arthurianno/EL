package com.elta.android.data.features.feedback.repository

import com.elta.android.data.features.feedback.datasource.FeedbackDataSource
import com.elta.android.data.features.userinfo.datasource.UserInfoDataSource
import com.elta.android.data.features.userinfo.dto.UserInfoDto
import com.elta.android.domain.features.feedback.repository.FeedbackRepository
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class FeedbackDataRepository @Inject constructor(
    private val remoteSource: FeedbackDataSource,
    private val userInfoDataSource: UserInfoDataSource
) : FeedbackRepository {

    override fun sendFeedback(name: String, email: String, message: String): Completable =
        remoteSource.sendFeedback(name, email, message).andThen(setFeedbackWasSent())

    override fun isFeedbackWasSent(): Single<Boolean> =
        userInfoDataSource.getUserInfo()
            .map { it.isFeedbackSent }

    override fun setFeedbackWasSent(): Completable =
        Single.just(UserInfoDto(isFeedbackSent = true))
            .flatMapCompletable {
                userInfoDataSource.updateUserInfo(it)
            }
}