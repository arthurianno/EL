package com.elta.android.domain.features.feedback.interactor

import com.elta.android.domain.features.feedback.model.FeedbackDataModel

const val NONE_STEP = -1
const val STEP_10 = 10
const val STEP_50 = 50
const val STEP_100 = 100

fun getFeedbackModel(events: Int): FeedbackDataModel =
    if (events == STEP_10 ||
        events == STEP_50 ||
        events % STEP_100 == 0
    ) {
        FeedbackDataModel(true, events)
    } else {
        FeedbackDataModel(false, NONE_STEP)
    }

fun noneFeedbackModel() = FeedbackDataModel(false, NONE_STEP)
