package com.elta.android.presentation.features.main.records.ui.adapter.items

import com.nullgr.core.adapter.items.ListItem

data class RecordsHeaderItem(
    val glucoseLevel: Double?,
    val glucoseLevelIndex: Double?,
    val glucoseLevelIndexDirection: IndexDirection?,
    val xeLevel: Double?,
    val insulinLevel: Double?
) : ListItem {

    override fun getChangePayload(other: ListItem): Any {
        if (other is RecordsHeaderItem) {
            return mutableSetOf<Payload>().apply {
                if (isGlucoseChanged(other)) add(Payload.GLUCOSE_LEVEL_CHANGED)
                if (xeLevel != other.xeLevel) add(Payload.XE_LEVEL_CHANGED)
                if (insulinLevel != other.insulinLevel) add(Payload.INSULIN_CHANGED)
            }
        }
        return super.getChangePayload(other)
    }

    private fun isGlucoseChanged(other: RecordsHeaderItem): Boolean =
        glucoseLevel != other.glucoseLevel ||
            glucoseLevelIndex != other.glucoseLevelIndex ||
            glucoseLevelIndexDirection != other.glucoseLevelIndexDirection

    enum class Payload {
        GLUCOSE_LEVEL_CHANGED,
        XE_LEVEL_CHANGED,
        INSULIN_CHANGED
    }

    enum class IndexDirection {
        UP, DOWN
    }
}

val emptyRecordsHeaderItem = RecordsHeaderItem(
    glucoseLevel = null,
    glucoseLevelIndex = null,
    glucoseLevelIndexDirection = null,
    xeLevel = null,
    insulinLevel = null
)