package com.elta.android.domain.features.diary.events.model

import com.elta.android.domain.features.diary.events.model.form.ActivityValidator
import com.elta.android.domain.features.diary.events.model.form.BreadValidator
import com.elta.android.domain.features.diary.events.model.form.FormValidator
import com.elta.android.domain.features.diary.events.model.form.GlucoseValidator
import com.elta.android.domain.features.diary.events.model.form.MedicinesValidator
import com.elta.android.domain.features.diary.events.model.form.MedicamentsValidator
import com.elta.android.domain.features.diary.events.model.form.WeightValidator
import com.elta.android.domain.features.diary.tags.model.Tag
import com.elta.android.domain.features.user.interactor.round
import com.elta.android.domain.features.user.model.GlucoseFormat
import org.threeten.bp.ZonedDateTime

const val GLUCOSE_DEFAULT_VALUE = 0.0
const val GLUCOSE_PLASMA_COEFFICIENT = 1.12

fun EventType.getValidator(): FormValidator =
    when (this) {
        EventType.BREAD -> BreadValidator
        EventType.INSULIN -> MedicinesValidator
        EventType.MEDICAMENTS -> MedicamentsValidator
        EventType.ACTIVITY -> ActivityValidator
        EventType.WEIGHT -> WeightValidator
        EventType.GLUCOSE -> GlucoseValidator
        else -> throw IllegalArgumentException("${this.name} doesn't support validation.")
    }

@Suppress("LongParameterList")
fun EventV2.isChanged(
    value: Double? = null,
    kind: String? = null,
    name: String? = null,
    duration: Long? = null,
    date: ZonedDateTime? = null,
    tagId: String? = null,
    medicament: Medicament? = null,
    activity: ActivityType? = null,
    note: String? = null
): Boolean =
    this.value != value ||
            this.kind != kind ||
            this.name != name ||
            this.duration != duration ||
            this.additionTime != date ||
            this.tagId != tagId ||
            this.medicament != medicament ||
            this.activityType != activity ||
            this.note != note

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

fun EventV2.modifyValue(format: GlucoseFormat): EventV2 =
    if (type == EventType.GLUCOSE) {
        copy(value = glucoseValue(format))
    } else {
        this
    }

fun List<EventV2>.modifyValues(format: GlucoseFormat): List<EventV2> =
    map { it.modifyValue(format) }
