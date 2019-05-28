package com.elta.android.data.features.feedback.repository

import com.elta.android.data.features.feedback.datasource.FeedbackDataSource
import com.elta.android.domain.features.feedback.repository.FeedbackRepository
import com.elta.android.domain.features.userinfo.repository.UserInfoRepository
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class FeedbackDataRepository @Inject constructor(
    private val remoteSource: FeedbackDataSource,
    private val userInfoRepository: UserInfoRepository
) : FeedbackRepository {

    override fun sendFeedback(name: String, email: String, message: String): Completable =
        remoteSource.sendFeedback(name, email, message)

    override fun isFeedbackWasSent(): Single<Boolean> =
        userInfoRepository.getUserInfo().map { it.isFeedbackSent }
}