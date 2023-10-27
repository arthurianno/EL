package com.elta.android.data.features.diary.events.mapper // ktlint-disable filename

import com.elta.android.common.utils.toIsoDate
import com.elta.android.data.features.calculator.mapper.toDomain
import com.elta.android.data.features.calculator.model.ProductResponse
import com.elta.android.data.features.diary.events.dto.EventTypeDto
import com.elta.android.data.features.diary.events.dto.v2.EventV2Dto
import com.elta.android.data.features.diary.events.dto.v2.MedicamentDto
import com.elta.android.domain.features.diary.events.model.ActivityType
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.EventV2
import com.elta.android.domain.features.diary.events.model.MealTag
import com.elta.android.domain.features.diary.events.model.Medicament
import com.elta.android.domain.features.diary.events.model.MedicamentInsulinStatistic
import com.elta.android.domain.features.diary.events.model.MedicamentInsulinType
import com.elta.android.domain.features.diary.events.model.State

private const val ELEMENT_SEPARATOR = "~"
private const val FIELD_SEPARATOR = "|"

internal fun String?.toProductsList(): List<ProductResponse>? =
    runCatching {
        this?.let {
            it.split(ELEMENT_SEPARATOR).map { element ->
                element.split(FIELD_SEPARATOR).run {
                    ProductResponse(
                        id = component1(),
                        name = component2(),
                        type = component3(),
                        servingAmount = component4().toDouble(),
                        servingId = component5(),
                        servingName = get(5),
                        breadUnits = get(6).toDouble(),
                        brandName = get(7),
                        calories = get(8).toDouble(),
                        proteins = get(9).toDouble(),
                        fats = get(10).toDouble(),
                        carbohydrates = get(11).toDouble(),
                        isVerified = get(12).toBooleanStrictOrNull() ?: false
                    )
                }
            }
        }
    }.getOrNull()

internal fun List<ProductResponse>?.toCache(): String? =
    this?.joinToString(separator = ELEMENT_SEPARATOR) {
        it.run {
            id + FIELD_SEPARATOR + name + FIELD_SEPARATOR + type + FIELD_SEPARATOR +
                servingAmount.toString() + FIELD_SEPARATOR + servingId + FIELD_SEPARATOR +
                servingName + FIELD_SEPARATOR + breadUnits + FIELD_SEPARATOR +
                    brandName + FIELD_SEPARATOR + calories + FIELD_SEPARATOR +
                    proteins + FIELD_SEPARATOR + fats + FIELD_SEPARATOR + carbohydrates + FIELD_SEPARATOR + isVerified
        }
    }

fun Medicament.toDto(): MedicamentDto {
    return MedicamentDto(
        id = id,
        name = name,
        insulinType = insulinType.toDto(),
        deleted = deleted
    )
}
private fun MedicamentInsulinType.toDto(): MedicamentDto.MedicamentInsulinTypeDto = MedicamentDto.MedicamentInsulinTypeDto(
    code = code,
    id = id,
    name = name,
)

fun EventV2Dto.toDomain(): EventV2 = EventV2(
    id = id,
    type = EventType.valueOf(data.type.name),
    additionTime = additionTime.toIsoDate(),
    tagId = tagId,
    tag = null,
    note = note,
    modificationTime = modificationTime,
    value = data.value,
    kind = data.kind,
    name = data.name,
    temperature = data.temperature,
    duration = data.duration,
    activityType = data.activityType?.let { ActivityType.valueOf(it.name) },
    mealTag = data.mealTag?.let { MealTag.valueOf(it.name) },
    medicament = data.insulinMedicament?.toDomain(),
    state = State.valueOf(state.name),
    glucometerSerialNumber = data.glucometerSerialNumber,
    dishes = data.products?.toDomain().orEmpty()
)

fun MedicamentDto.toDomain(): Medicament {
    return Medicament(
        id = id,
        name = name,
        insulinType = insulinType.toDomain(),
        deleted = deleted
    )
}

fun MedicamentDto.MedicamentInsulinTypeDto.toDomain(): MedicamentInsulinType =
    MedicamentInsulinType(
        code = code,
        id = id,
        name = name
    )

internal fun EventType.toDb(): EventTypeDto = EventTypeDto.valueOf(this.name)
