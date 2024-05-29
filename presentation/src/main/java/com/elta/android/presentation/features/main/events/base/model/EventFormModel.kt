package com.elta.android.presentation.features.main.events.base.model

import com.elta.android.domain.features.diary.events.model.ActivityType
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.medicines.model.InsulinMedicament
import com.elta.android.domain.features.diary.medicines.model.Medicament
import com.elta.android.domain.features.diary.tags.model.Tag
import com.elta.android.presentation.features.profile.settings.dialogs.glucose.model.toStringFormat
import org.threeten.bp.ZonedDateTime

data class EventFormModel(
    var eventType: EventType? = null,
    var pickerValue: Double? = null,
    var inputValue: Double? = null,
    var additionalValue: String? = null,
    var tag: Tag? = null,
    var isDateChanged: Boolean = false,
    var date: ZonedDateTime? = null,
    var noteValue: String? = null,
    var meta: Any? = null
) {

    val kind: String?
        get() = if (eventType !is EventType.Bread || inputValue == null) null else inputValue?.toStringFormat()

    val name: String?
        get() = if (eventType == EventType.Medicaments) {
            if (additionalValue.isNullOrBlank()) null else additionalValue
        } else {
            inputValue?.toStringFormat()
        }

    val value: Double?
        get() = when {
            eventType is EventType.Activity || eventType is EventType.Medicaments -> null
            pickerValue == 0.0 -> null
            else -> pickerValue
        }

    val duration: Long?
        get() = when {
            eventType !is EventType.Activity -> null
            pickerValue?.toLong() == 0L -> null
            else -> pickerValue?.toLong()
        }

    val note: String?
        get() = if (noteValue.isNullOrBlank()) null else noteValue?.trim()

    val activityType: ActivityType?
        get() = meta as? ActivityType

    val insulinMedicament: InsulinMedicament?
        get() = meta as? InsulinMedicament

    val medicament: Medicament?
        get() = ((meta as? Pair<*, *>)?.first as? Medicament) ?: meta as? Medicament

    val tabletsNumber: Double?
        get() = when (eventType) {
            EventType.Medicaments -> inputValue
            else -> null
        }
}
