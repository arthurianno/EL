package com.elta.android.domain.features.feedback.repository

import io.reactivex.Completable
import io.reactivex.Single

interface FeedbackRepository {

    fun sendFeedback(name: String, email: String, message: String): Completable

    fun isFeedbackWasSent(): Single<Boolean>
}