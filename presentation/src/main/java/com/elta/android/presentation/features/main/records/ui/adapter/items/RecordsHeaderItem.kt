package com.elta.android.presentation.features.main.records.ui.adapter.items

import android.graphics.drawable.Drawable
import com.nullgr.core.adapter.items.ListItem

data class RecordsHeaderItem(
    val background: Drawable?,
    val glucoseLevel: Double?,
    val glucoseLevelIndex: Double?,
    val glucoseLevelIndexIcon: Int?,
    val breadLevel: Double?,
    val insulinLevel: Double?
) : ListItem {

    override fun getChangePayload(other: ListItem): Any {
        if (other is RecordsHeaderItem) {
            return mutableSetOf<Payload>().apply {
                if (isGlucoseChanged(other)) add(Payload.GLUCOSE_LEVEL_CHANGED)
                if (breadLevel != other.breadLevel) add(Payload.BREAD_LEVEL_CHANGED)
                if (insulinLevel != other.insulinLevel) add(Payload.INSULIN_CHANGED)
            }
        }
        return super.getChangePayload(other)
    }

    private fun isGlucoseChanged(other: RecordsHeaderItem): Boolean =
        glucoseLevel != other.glucoseLevel ||
            glucoseLevelIndex != other.glucoseLevelIndex ||
            glucoseLevelIndexIcon != other.glucoseLevelIndexIcon

    enum class Payload {
        GLUCOSE_LEVEL_CHANGED,
        BREAD_LEVEL_CHANGED,
        INSULIN_CHANGED
    }
}