package com.elta.android.data.features.diary.events.api

import com.elta.android.common.utils.toIsoString
import com.elta.android.data.features.calculator.model.ProductResponse
import com.elta.android.data.features.common.dto.StateDto
import com.elta.android.data.features.diary.events.dto.ActivityTypeDto
import com.elta.android.data.features.diary.events.dto.EventTypeDto
import com.elta.android.data.features.diary.events.dto.MealTagDto
import com.elta.android.data.features.diary.events.dto.v2.EventDataV2Dto
import com.elta.android.data.features.diary.events.dto.v2.EventV2Dto
import com.elta.android.data.features.diary.events.dto.v2.InsulinMedicamentDto
import com.elta.android.data.features.diary.events.dto.v2.MedicamentDto
import com.elta.android.data.features.diary.events.extensions.countOrZero
import com.elta.android.domain.features.diary.medicines.model.Medicament
import org.threeten.bp.ZonedDateTime
import java.util.Date

@Suppress("MagicNumber", "ForEachOnRange", "LongParameterList")
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
        insulinMedicament: InsulinMedicamentDto? = null,
        medicament: MedicamentDto? = null,
        tabletsNumber: Double? = null,
        tagId: String? = null,
        note: String? = null,
        state: StateDto = StateDto.CREATED,
        products: List<ProductResponse>? = null
    ): EventV2Dto =
        EventV2Dto(
            id = id,
            additionTime = ZonedDateTime.now().toIsoString(),
            tagId = tagId,
            note = note,
            modificationTime = Date().time,
            state = state,
            data = EventDataV2Dto(
                temperature = 0.0,
                value = value,
                name = "Test name",
                kind = "Test kind",
                duration = 2 * 60 * 60 + 30 * 60, // 2h 30m
                activityType = activityType,
                mealTag = mealTag,
                insulinMedicament = insulinMedicament,
                medicament = medicament,
                tabletsNumber = tabletsNumber,
                type = type,
                glucometerSerialNumber = null,
                products = products,
                productsCount = products.countOrZero()
            )
        )
}
