package com.elta.android.data.features.feedback.api

import com.elta.android.data.features.feedback.request.FeedbackRequest
import io.reactivex.Completable

class MockedFeedbackApi : FeedbackApi {

    override fun sendFeedback(request: FeedbackRequest): Completable =
        Completable.complete()
}
