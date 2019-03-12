package com.elta.android.domain.factory

import com.elta.android.domain.features.diary.events.model.ActivityType
import com.elta.android.domain.features.diary.events.model.Event
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.InsulinType
import com.elta.android.domain.features.diary.events.model.MealTag
import com.elta.android.domain.features.diary.events.model.State
import java.util.Date
import java.util.UUID

object EventTestFactory {

    fun create(
        type: EventType,
        value: Double? = null,
        activityType: ActivityType? = null,
        mealTag: MealTag? = null,
        insulinType: InsulinType? = null,
        tagId: String? = null
    ): Event =
        Event(
            id = UUID.randomUUID().toString(),
            additionTime = Date(),
            additionTimeString = "",
            tagId = tagId,
            tag = null,
            note = "Test note",
            modificationTime = Date(),
            value = value,
            name = "Test name",
            kind = "Test kind",
            duration = 2 * 60 * 60 + 30 * 60,
            activityType = activityType,
            mealTag = mealTag,
            insulinType = insulinType,
            type = type,
            state = State.CREATED
        )
}