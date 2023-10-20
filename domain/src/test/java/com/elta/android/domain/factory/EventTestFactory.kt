package com.elta.android.domain.factory

import com.elta.android.domain.features.diary.events.model.ActivityType
import com.elta.android.domain.features.diary.events.model.EventV2
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.MealTag
import com.elta.android.domain.features.diary.events.model.Medicament
import com.elta.android.domain.features.diary.events.model.State
import org.threeten.bp.ZonedDateTime
import java.util.UUID

object EventTestFactory {

    fun create(
        type: EventType,
        value: Double? = null,
        duration: Long = 2 * 60 * 60 + 30 * 60,
        activityType: ActivityType? = null,
        mealTag: MealTag = MealTag.BEFOREMEAL,
        medicament: Medicament? = null,
        tagId: String? = null,
        date: ZonedDateTime = ZonedDateTime.now()
    ): EventV2 =
        EventV2(
            id = UUID.randomUUID().toString(),
            additionTime = date,
            tagId = tagId,
            tag = null,
            temperature = null,
            note = "Test note",
            modificationTime = null,
            value = value,
            name = "Test name",
            kind = "Test kind",
            duration = duration,
            activityType = activityType,
            mealTag = mealTag,
            medicament = medicament,
            type = type,
            state = State.CREATED,
            glucometerSerialNumber = null,
            dishes = emptyList()
        )
}
