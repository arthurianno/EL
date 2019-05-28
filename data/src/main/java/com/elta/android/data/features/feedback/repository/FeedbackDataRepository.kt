package com.elta.android.data.features.feedback.repository

import com.elta.android.data.features.feedback.datasource.FeedbackDataSource
import com.elta.android.domain.features.feedback.repository.FeedbackRepository
import io.reactivex.Completable
import javax.inject.Inject

class FeedbackDataRepository @Inject constructor(
    private val remoteSource: FeedbackDataSource
) : FeedbackRepository {

    override fun sendFeedback(name: String, email: String, message: String): Completable =
        remoteSource.sendFeedback(name, email, message)
}