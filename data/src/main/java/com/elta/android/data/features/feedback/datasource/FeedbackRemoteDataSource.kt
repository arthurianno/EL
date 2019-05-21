package com.elta.android.data.features.feedback.datasource

import com.elta.android.data.features.feedback.api.FeedbackApi
import com.elta.android.data.features.feedback.request.FeedbackRequest
import io.reactivex.Completable
import javax.inject.Inject

class FeedbackRemoteDataSource @Inject constructor(
    private val api: FeedbackApi
) : FeedbackDataSource {

    override fun sendFeedback(name: String, email: String, message: String): Completable =
        api.sendFeedback(FeedbackRequest(name, email, message))
}