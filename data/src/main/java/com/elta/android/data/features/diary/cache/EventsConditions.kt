package com.elta.android.data.features.diary.cache

import com.elta.android.data.features.common.cache.Condition

sealed class EventsConditions : Condition {

    object All : EventsConditions()
}