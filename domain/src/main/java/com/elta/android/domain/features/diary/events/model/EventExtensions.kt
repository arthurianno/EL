package com.elta.android.domain.features.diary.events.model

import com.elta.android.domain.features.diary.events.model.form.ActivityValidator
import com.elta.android.domain.features.diary.events.model.form.BreadValidator
import com.elta.android.domain.features.diary.events.model.form.FormValidator
import com.elta.android.domain.features.diary.events.model.form.GlucoseValidator
import com.elta.android.domain.features.diary.events.model.form.InsulinValidator
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
        EventType.INSULIN -> InsulinValidator
        EventType.MEDICAMENTS -> MedicamentsValidator
        EventType.ACTIVITY -> ActivityValidator
        EventType.WEIGHT -> WeightValidator
        EventType.GLUCOSE -> GlucoseValidator
        else -> throw IllegalArgumentException("${this.name} doesn't support validation.")
    }

@Suppress("LongParameterList")
fun Event.isChanged(
    value: Double? = null,
    kind: String? = null,
    name: String? = null,
    duration: Long? = null,
    date: ZonedDateTime? = null,
    tagId: String? = null,
    insulinType: InsulinType? = null,
    medicament: String? = null,
    activity: ActivityType? = null,
    note: String? = null
): Boolean =
    this.value != value ||
        this.kind != kind ||
        this.name != name ||
        this.duration != duration ||
        this.additionTime != date ||
        this.tagId != tagId ||
        this.insulinType != insulinType ||
        this.medicament != medicament ||
        this.activityType != activity ||
        this.note != note

fun Event.addTag(tags: List<Tag>): Event =
    this.copy(tag = tags.firstOrNull { tagId == it.id })

fun Event.glucoseValue(format: GlucoseFormat): Double = run {
    value?.let {
        when (format) {
            GlucoseFormat.CAPILLARY -> it
            GlucoseFormat.PLASMA -> it * GLUCOSE_PLASMA_COEFFICIENT
        }
    } ?: GLUCOSE_DEFAULT_VALUE
}.round(2)

fun Event.modifyValue(format: GlucoseFormat): Event =
    if (type == EventType.GLUCOSE) {
        copy(value = glucoseValue(format))
    } else {
        this
    }

fun List<Event>.modifyValues(format: GlucoseFormat): List<Event> =
    map { it.modifyValue(format) }
