package com.elta.android.domain.features.diary.events.model

import com.elta.android.domain.features.calculator.model.Dish
import com.elta.android.domain.features.diary.events.model.form.ActivityValidator
import com.elta.android.domain.features.diary.events.model.form.BreadValidator
import com.elta.android.domain.features.diary.events.model.form.FormValidator
import com.elta.android.domain.features.diary.events.model.form.GlucoseValidator
import com.elta.android.domain.features.diary.events.model.form.InsulinMedicamentValidator
import com.elta.android.domain.features.diary.events.model.form.ManualGlucoseValidator
import com.elta.android.domain.features.diary.events.model.form.MedicamentsValidator
import com.elta.android.domain.features.diary.events.model.form.WeightValidator
import com.elta.android.domain.features.diary.medicines.model.InsulinMedicament
import com.elta.android.domain.features.diary.medicines.model.Medicament
import com.elta.android.domain.features.diary.tags.model.Tag
import com.elta.android.domain.features.user.interactor.round
import com.elta.android.domain.features.user.model.GlucoseFormat
import org.threeten.bp.ZonedDateTime

const val GLUCOSE_DEFAULT_VALUE = 0.0
const val GLUCOSE_PLASMA_COEFFICIENT = 1.12

fun EventType.getValidator(glucoseFormat: GlucoseFormat?): FormValidator =
    when (this) {
        is EventType.Bread -> BreadValidator(calculatorFlow)
        is EventType.Glucose -> {
            when (this.inputType) {
                GlucoseInputType.MANUAL -> ManualGlucoseValidator(glucoseFormat)
                GlucoseInputType.AUTO -> GlucoseValidator
            }
        }
        EventType.Insulin -> InsulinMedicamentValidator
        EventType.Medicaments -> MedicamentsValidator
        EventType.Activity -> ActivityValidator
        EventType.Weight -> WeightValidator
        EventType.Glycatedhemoglobin -> throw IllegalArgumentException("$this doesn't support validation.")
    }

@Suppress("LongParameterList")
fun EventV2.isChanged(
    value: Double? = null,
    kind: String? = null,
    name: String? = null,
    duration: Long? = null,
    date: ZonedDateTime? = null,
    tagId: String? = null,
    insulinMedicament: InsulinMedicament? = null,
    dishes: List<Dish>? = null,
    medicament: Medicament? = null,
    activity: ActivityType? = null,
    note: String? = null,
    tabletsNumber: Double?,
    mealTag: MealTag? = null
): Boolean =
    this.value != value ||
            this.kind != kind ||
            this.name != name ||
            this.duration != duration ||
            this.additionTime != date ||
            this.tagId != tagId ||
            this.insulinMedicament != insulinMedicament ||
            this.medicament != medicament ||
            this.tabletsNumber != tabletsNumber ||
            this.dishes != dishes ||
            this.activityType != activity ||
            this.note != note ||
            this.mealTag != mealTag

fun EventV2.addTag(tags: List<Tag>): EventV2 =
    this.copy(tag = tags.firstOrNull { tagId == it.id })

fun EventV2.glucoseValue(format: GlucoseFormat): Double = run {
    value?.toGlucoseFormat(format) ?: GLUCOSE_DEFAULT_VALUE
}

fun Double.toGlucoseFormat(format: GlucoseFormat?): Double = run {
    when (format) {
        GlucoseFormat.CAPILLARY -> this
        GlucoseFormat.PLASMA -> this * GLUCOSE_PLASMA_COEFFICIENT
        else -> GLUCOSE_DEFAULT_VALUE
    }
}.round(1)

fun Double.toCapillaryGlucoseFormat(format: GlucoseFormat?): Double = run {
    when (format) {
        GlucoseFormat.CAPILLARY -> this
        GlucoseFormat.PLASMA -> this / GLUCOSE_PLASMA_COEFFICIENT
        else -> GLUCOSE_DEFAULT_VALUE
    }
}.round(1)

fun EventV2.modifyValue(format: GlucoseFormat): EventV2 =
    if (type is EventType.Glucose) {
        copy(value = glucoseValue(format))
    } else {
        this
    }

fun List<EventV2>.modifyValues(format: GlucoseFormat): List<EventV2> =
    map { it.modifyValue(format) }
