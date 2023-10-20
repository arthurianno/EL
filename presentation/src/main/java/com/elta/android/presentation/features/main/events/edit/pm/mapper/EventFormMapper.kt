package com.elta.android.presentation.features.main.events.edit.pm.mapper

import com.elta.android.domain.features.diary.events.model.ActivityType
import com.elta.android.domain.features.diary.events.model.EventV2
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.Medicament
import com.elta.android.presentation.features.main.events.mapper.toPickerValues
import com.elta.android.presentation.utils.NumberFormatter
import com.elta.android.presentation.utils.toIcon
import com.elta.android.presentation.utils.toName
import com.elta.android.presentation.widgets.selector.model.SelectorOption
import com.nullgr.core.resources.ResourceProvider

fun EventV2.getPickerValues(): Pair<Int, Int>? =
    when (type) {
        EventType.ACTIVITY -> duration.toPickerValues()
        EventType.MEDICAMENTS -> null
        else -> value.toPickerValues()
    }

fun EventV2.getValue(): Double = value ?: 0.0

fun EventV2.getFormattedTemperature(): String =
    temperature?.let { NumberFormatter.format(it) } ?: "-"

fun EventV2.getFormInputText(): String? =
    when (type) {
        EventType.BREAD -> kind
        EventType.MEDICAMENTS -> name
        else -> null
    }

fun EventV2.getTag(res: ResourceProvider): SelectorOption? {
    if (tag == null) return null
    return SelectorOption(
        text = tag.toName(res),
        icon = res.getDrawable(tag.toIcon()),
        meta = tag
    )
}

fun EventV2.getSelectorOption(res: ResourceProvider): SelectorOption? =
    when (type) {
        EventType.ACTIVITY -> activityType.toSelectorOption(res)
        EventType.INSULIN -> medicament?.toSelectorOption()
        else -> null
    }

private fun ActivityType?.toSelectorOption(res: ResourceProvider): SelectorOption? {
    if (this == null) return null
    return SelectorOption(
        text = res.getString(toName()),
        icon = res.getDrawable(toIcon()),
        meta = this
    )
}

private fun Medicament.toSelectorOption(): SelectorOption =
    SelectorOption(
        text = "${insulinType.name}($name)",
        meta = this
    )
