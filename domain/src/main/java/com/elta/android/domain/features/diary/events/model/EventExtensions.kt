package com.elta.android.domain.features.diary.events.model

import com.elta.android.domain.features.diary.events.model.form.ActivityValidator
import com.elta.android.domain.features.diary.events.model.form.BreadValidator
import com.elta.android.domain.features.diary.events.model.form.FormValidator
import com.elta.android.domain.features.diary.events.model.form.GlucoseValidator
import com.elta.android.domain.features.diary.events.model.form.InsulinValidator
import com.elta.android.domain.features.diary.events.model.form.MedicamentsValidator
import com.elta.android.domain.features.diary.events.model.form.WeightValidator
import com.elta.android.domain.features.diary.tags.model.Tag
import org.threeten.bp.ZonedDateTime

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
