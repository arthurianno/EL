package com.elta.android.data.features.feedback.datasource

import io.reactivex.Completable

interface FeedbackDataSource {

    fun sendFeedback(name: String, email: String, message: String): Completable
}