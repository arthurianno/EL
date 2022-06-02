package com.elta.android.presentation.features.main.events.edit.pm.mapper

import com.elta.android.domain.features.diary.events.model.ActivityType
import com.elta.android.domain.features.diary.events.model.Event
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.InsulinType
import com.elta.android.presentation.utils.NumberFormatter
import com.elta.android.presentation.utils.toIcon
import com.elta.android.presentation.utils.toName
import com.elta.android.presentation.widgets.selector.model.SelectorOption
import com.nullgr.core.resources.ResourceProvider
import java.util.concurrent.TimeUnit

fun Event.getPickerValues(): Pair<Int, Int>? =
    when (type) {
        EventType.ACTIVITY -> duration.toPickerValues()
        EventType.MEDICAMENTS -> null
        else -> value.toPickerValues()
    }

fun Event.getValue(): Double =
    when (value == null) {
        true -> 0.0
        else -> checkNotNull(value)
    }

fun Event.getFormattedTemperature(): String =
    temperature?.let { NumberFormatter.format(it) } ?: "-"

fun Event.getFormInputText(): String? =
    when (type) {
        EventType.BREAD -> kind
        EventType.MEDICAMENTS -> name
        else -> null
    }

fun Event.getTag(res: ResourceProvider): SelectorOption? {
    if (tag == null) return null
    return SelectorOption(
        text = tag.toName(res),
        icon = res.getDrawable(tag.toIcon()),
        meta = tag
    )
}

fun Event.getSelectorOption(res: ResourceProvider): SelectorOption? =
    when (type) {
        EventType.ACTIVITY -> activityType.toSelectorOption(res)
        EventType.INSULIN -> insulinType.toSelectorOption(res)
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

private fun InsulinType?.toSelectorOption(res: ResourceProvider): SelectorOption? {
    if (this == null) return null
    return SelectorOption(res.getString(this.toName()), meta = this)
}

private fun Long?.toPickerValues(): Pair<Int, Int> {
    if (this == null) return 0 to 0
    val hours = TimeUnit.SECONDS.toHours(this)
    val minutes = TimeUnit.SECONDS.toMinutes(this) - TimeUnit.HOURS.toMinutes(hours)
    return hours.toInt() to minutes.toInt()
}

private fun Double?.toPickerValues(): Pair<Int, Int> {
    if (this == null) return 0 to 0
    val tokens = this.toString().split(".")
    val left = tokens[0].toInt()
    val right = tokens[1].toInt()
    return left to right
}
