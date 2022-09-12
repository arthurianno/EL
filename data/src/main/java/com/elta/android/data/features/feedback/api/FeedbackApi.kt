package com.elta.android.data.features.feedback.api

import com.elta.android.data.features.feedback.request.FeedbackRequest
import io.reactivex.Completable
import retrofit2.http.Body
import retrofit2.http.PUT

interface FeedbackApi {

    @PUT("api/notifications/v1/emails/feedback")
    fun sendFeedback(@Body request: FeedbackRequest): Completable
}
