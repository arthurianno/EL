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

@Suppress("MagicNumber", "ForEachOnRange")
object EventMockedFactory {

    private val ids = arrayListOf<String>().apply {
        (0..40).forEach {
            add("ID_TEST_$it")
        }
    }

    private var index = 0
    private val id: String
        get() = ids[index++ % ids.size]

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
            id = id,
            additionTime = Date().toStringIso(),
            tagId = tagId,
            note = "Test note",
            modificationTime = Date().time,
            state = state,
            data = EventDataDto(
                value = value,
                name = "Test name",
                kind = "Test kind",
                duration = 2 * 60 * 60 + 30 * 60, // 2h 30m
                activityType = activityType,
                mealTag = mealTag,
                insulinType = insulinType,
                type = type
            )
        )
}