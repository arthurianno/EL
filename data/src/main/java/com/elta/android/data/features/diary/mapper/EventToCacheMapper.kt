package com.elta.android.data.features.diary.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.diary.cache.dto.EventCachedDto
import com.elta.android.data.features.diary.dto.ActivityTypeDto
import com.elta.android.data.features.diary.dto.InsulinTypeDto
import com.elta.android.data.features.diary.dto.MealTagDto
import com.elta.android.data.features.diary.dto.event.ActivityDataDto
import com.elta.android.data.features.diary.dto.event.BreadDataDto
import com.elta.android.data.features.diary.dto.event.EventDto
import com.elta.android.data.features.diary.dto.event.GlucoseDataDto
import com.elta.android.data.features.diary.dto.event.InsulinDataDto
import com.elta.android.data.features.diary.dto.event.MedicamentsDataDto
import com.elta.android.data.features.diary.dto.event.WeightDataDto
import com.nullgr.core.date.CommonFormats.FORMAT_STANDARD_DATE_FULL_MILLIS_UTC
import com.nullgr.core.date.dateFromTimestamp
import com.nullgr.core.date.toDate
import java.util.Date
import javax.inject.Inject

class EventToCacheMapper @Inject constructor() : Mapper<EventDto, EventCachedDto> {

    override fun mapFromObject(source: EventDto): EventCachedDto =
        with(source) {
            EventCachedDto(
                id = id.hashCode().toLong(),
                secondaryId = id,
                type = data.type.name,
                additionTime = additionTime.toDate(FORMAT_STANDARD_DATE_FULL_MILLIS_UTC) as Date,
                tagId = tagId,
                note = note,
                modificationTime = modificationTime?.dateFromTimestamp(),
                value = value,
                name = name,
                duration = duration,
                activityType = activityType?.name,
                mealTag = mealTag?.name,
                insulinType = insulinType?.name
            )
        }

    val EventDto.value: Double?
        get() = when (data) {
            is BreadDataDto -> data.value
            is GlucoseDataDto -> data.value
            is InsulinDataDto -> data.value
            is WeightDataDto -> data.value
            else -> null
        }

    val EventDto.name: String?
        get() = when (data) {
            is BreadDataDto -> data.kind
            is MedicamentsDataDto -> data.name
            else -> null
        }

    val EventDto.duration: String?
        get() = when (data) {
            is ActivityDataDto -> data.duration
            else -> null
        }

    val EventDto.activityType: ActivityTypeDto?
        get() = when (data) {
            is ActivityDataDto -> data.activityType
            else -> null
        }

    val EventDto.mealTag: MealTagDto?
        get() = when (data) {
            is GlucoseDataDto -> data.mealTag
            else -> null
        }

    val EventDto.insulinType: InsulinTypeDto?
        get() = when (data) {
            is InsulinDataDto -> data.insulinType
            else -> null
        }
}