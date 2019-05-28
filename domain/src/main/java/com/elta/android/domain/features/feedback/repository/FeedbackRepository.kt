package com.elta.android.domain.features.feedback.repository

import io.reactivex.Completable

interface FeedbackRepository {

    fun sendFeedback(name: String, email: String, message: String): Completable
}