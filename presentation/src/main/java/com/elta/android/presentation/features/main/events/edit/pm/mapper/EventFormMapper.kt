package com.elta.android.presentation.features.main.events.edit.pm.mapper

import com.elta.android.domain.features.diary.events.model.ActivityType
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.EventV2
import com.elta.android.domain.features.diary.events.model.MealTag
import com.elta.android.domain.features.diary.medicines.model.InsulinMedicament
import com.elta.android.domain.features.diary.medicines.model.Medicament
import com.elta.android.presentation.features.main.events.extensions.getLocalizedName
import com.elta.android.presentation.features.main.events.mapper.toPickerValues
import com.elta.android.presentation.utils.NumberFormatter
import com.elta.android.presentation.utils.toIcon
import com.elta.android.presentation.utils.toName
import com.elta.android.presentation.widgets.selector.model.SelectorOption
import com.nullgr.core.resources.ResourceProvider

fun EventV2.getPickerValues(): Pair<Int, Int>? =
    when (type) {
        EventType.Activity -> duration.toPickerValues()
        EventType.Medicaments -> null
        else -> value.toPickerValues()
    }

fun EventV2.getValue(): Double = value ?: 0.0

fun EventV2.getFormattedTemperature(): String =
    temperature?.let { NumberFormatter.format(it) } ?: "-"

fun EventV2.getFormInputText(): String? =
    when (type) {
        is EventType.Bread -> kind
        EventType.Medicaments -> {
            tabletsNumber?.let {
                if (it > 0.0)
                    tabletsNumber.toString()
                else
                    null
            }
        }
        else -> null
    }

fun EventV2.getFormAdditionalText(): String? =
    when (type) {
        EventType.Medicaments -> name
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
        EventType.Activity -> activityType.toSelectorOption(res)
        EventType.Insulin -> insulinMedicament?.toSelectorOption(res)
        EventType.Medicaments -> medicament?.toSelectorOption()
        else -> null
    }

fun EventV2.getMedicament(): Medicament? =
    when(type) {
        EventType.Medicaments -> medicament
        else -> null
    }

fun EventV2.getMealTag(): MealTag? =
    when(type) {
        is EventType.Glucose -> mealTag
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

private fun InsulinMedicament.toSelectorOption(res: ResourceProvider): SelectorOption =
    SelectorOption(
        text = "${insulinType.getLocalizedName(res)}($name)",
        meta = this
    )

private fun Medicament.toSelectorOption(): SelectorOption =
    SelectorOption(
        text = name,
        meta = this
    )
