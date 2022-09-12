package com.elta.android.presentation.features.profile.main.ui.adapter.items

import com.nullgr.core.adapter.items.ListItem

data class MainProfileIndicatorItem(
    val glucoseLevelMin: String,
    val glucoseLevelMax: String,
    val diabetesType: String,
    val weight: String,
    val hemoglobin: String
) : ListItem {

    override fun getChangePayload(other: ListItem): Any {
        if (other is MainProfileIndicatorItem) {
            return mutableSetOf<Payload>().apply {
                if (glucoseLevelMin != other.glucoseLevelMin ||
                    glucoseLevelMax != other.glucoseLevelMax
                ) add(Payload.GLUCOSE_LEVEL_CHANGED)
                if (diabetesType != other.diabetesType) add(Payload.DIABETES_CHANGED)
                if (weight != other.weight) add(Payload.WEIGHT_CHANGED)
                if (hemoglobin != other.hemoglobin) add(Payload.HEMOGLOBIN_CHANGED)
            }
        }
        return super.getChangePayload(other)
    }

    enum class Payload {
        GLUCOSE_LEVEL_CHANGED,
        DIABETES_CHANGED,
        WEIGHT_CHANGED,
        HEMOGLOBIN_CHANGED
    }

    enum class Type {
        GLUCOSE_LEVEL,
        DIABETES,
        WEIGHT,
        HEMOGLOBIN
    }
}
