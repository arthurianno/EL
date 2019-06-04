package com.elta.android.domain.factory

import com.elta.android.domain.features.diary.events.model.ActivityType
import com.elta.android.domain.features.diary.events.model.Event
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.InsulinType
import com.elta.android.domain.features.diary.events.model.MealTag
import com.elta.android.domain.features.diary.events.model.State
import org.threeten.bp.ZonedDateTime
import java.util.UUID

object EventTestFactory {

    fun create(
        type: EventType,
        value: Double? = null,
        duration: Long = 2 * 60 * 60 + 30 * 60,
        activityType: ActivityType? = null,
        mealTag: MealTag? = null,
        insulinType: InsulinType? = null,
        tagId: String? = null,
        date: ZonedDateTime = ZonedDateTime.now()
    ): Event =
        Event(
            id = UUID.randomUUID().toString(),
            additionTime = date,
            tagId = tagId,
            tag = null,
            temperature = null,
            note = "Test note",
            modificationTime = ZonedDateTime.now(),
            value = value,
            name = "Test name",
            kind = "Test kind",
            duration = duration,
            activityType = activityType,
            mealTag = mealTag,
            insulinType = insulinType,
            type = type,
            state = State.CREATED
        )
}