package com.elta.android.data.features.diary.events.mapper // ktlint-disable filename

import com.elta.android.common.utils.toIsoDate
import com.elta.android.data.features.calculator.mapper.toDomain
import com.elta.android.data.features.calculator.model.ProductResponse
import com.elta.android.data.features.diary.events.dto.EventTypeDto.Companion.toEventType
import com.elta.android.data.features.diary.events.dto.GlucoseInputTypeDto
import com.elta.android.data.features.diary.events.dto.v2.EventV2Dto
import com.elta.android.data.features.diary.events.dto.v2.InsulinMedicamentDto
import com.elta.android.data.features.diary.events.dto.v2.MedicamentDto
import com.elta.android.domain.features.diary.events.model.ActivityType
import com.elta.android.domain.features.diary.events.model.EventV2
import com.elta.android.domain.features.diary.events.model.GlucoseInputType
import com.elta.android.domain.features.diary.events.model.MealTag
import com.elta.android.domain.features.diary.events.model.State
import com.elta.android.domain.features.diary.medicines.model.InsulinMedicament
import com.elta.android.domain.features.diary.medicines.model.Medicament
import com.elta.android.domain.features.diary.medicines.model.MedicamentInsulinType

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
                        breadUnits = get(6).toDoubleOrNull(),
                        brandName = get(7),
                        calories = get(8).toDoubleOrNull(),
                        proteins = get(9).toDoubleOrNull(),
                        fats = get(10).toDoubleOrNull(),
                        carbohydrates = get(11).toDoubleOrNull(),
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

fun InsulinMedicament.toDto(): InsulinMedicamentDto =
    InsulinMedicamentDto(
        id = id,
        name = name,
        insulinType = insulinType.toDto(),
        deleted = deleted
    )

fun Medicament.toDto(): MedicamentDto =
    MedicamentDto(
        id = id,
        name = name,
        deleted = isDeleted,
        other = isOther,
        touchedAt = touchedAt
    )

private fun MedicamentInsulinType.toDto(): InsulinMedicamentDto.MedicamentInsulinTypeDto =
    InsulinMedicamentDto.MedicamentInsulinTypeDto(
        code = code,
        id = id,
        name = name,
    )

fun EventV2Dto.toDomain(): EventV2 = EventV2(
    id = id,
    type = data.type.toEventType(data.value, data.inputType),
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
    insulinMedicament = data.insulinMedicament?.toDomain(),
    medicament = data.medicament?.toDomain(),
    tabletsNumber = data.tabletsNumber,
    state = State.valueOf(state.name),
    glucometerSerialNumber = data.glucometerSerialNumber,
    dishes = data.products?.toDomain().orEmpty(),
    glucoseInputType = data.inputType?.toDomain()
)

fun MedicamentDto.toDomain(): Medicament =
    Medicament(
        id = id,
        name = name,
        isDeleted = deleted,
        isOther = other,
        touchedAt = touchedAt
    )

fun InsulinMedicamentDto.toDomain(): InsulinMedicament =
    InsulinMedicament(
        id = id,
        name = name,
        insulinType = insulinType.toDomain(),
        deleted = deleted
    )

fun InsulinMedicamentDto.MedicamentInsulinTypeDto.toDomain(): MedicamentInsulinType =
    MedicamentInsulinType(
        code = code,
        id = id,
        name = name
    )

fun GlucoseInputTypeDto.toDomain(): GlucoseInputType =
    when (this) {
        GlucoseInputTypeDto.MANUAL -> GlucoseInputType.MANUAL
        GlucoseInputTypeDto.AUTO -> GlucoseInputType.AUTO
    }
