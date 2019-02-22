package com.elta.android.data.features.diary.events.api

import com.elta.android.data.common.toStringIso
import com.elta.android.data.features.common.dto.StateDto
import com.elta.android.data.features.diary.events.dto.ActivityTypeDto
import com.elta.android.data.features.diary.events.dto.EventDataDto
import com.elta.android.data.features.diary.events.dto.EventDto
import com.elta.android.data.features.diary.events.dto.EventTypeDto
import com.elta.android.data.features.diary.events.dto.InsulinTypeDto
import com.elta.android.data.features.diary.events.dto.MealTagDto
import java.util.Date
import java.util.UUID

object EventMockedFactory {

    fun create(
        type: EventTypeDto,
        value: Double? = null,
        activityType: ActivityTypeDto? = null,
        mealTag: MealTagDto? = null,
        insulinType: InsulinTypeDto? = null,
        tagId: String? = null,
        state: StateDto = StateDto.CREATED
    ): EventDto =
        EventDto(
            id = UUID.randomUUID().toString(),
            additionTime = Date().toStringIso(),
            tagId = tagId,
            note = "Test note",
            modificationTime = Date().time,
            state = state,
            data = EventDataDto(
                value = value,
                name = "Test name",
                kind = "Test kind",
                duration = "00:00",
                activityType = activityType,
                mealTag = mealTag,
                insulinType = insulinType,
                type = type
            )
        )
}