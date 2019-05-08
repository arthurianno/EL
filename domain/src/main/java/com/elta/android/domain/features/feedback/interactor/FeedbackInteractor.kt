package com.elta.android.domain.features.feedback.interactor

import com.elta.android.domain.features.diary.events.model.Event

private const val STEP_10 = 10
private const val STEP_50 = 50
private const val STEP_100 = 100

fun List<Event>.isFeedbackStep(): Boolean =
    count() == STEP_10 || count() == STEP_50 || count() % STEP_100 == 0