package com.elta.android.presentation.features.statistic.period.ui.adapter.items

import android.graphics.drawable.Drawable
import com.nullgr.core.adapter.items.ListItem

data class GlucoseIndexItem(
    val type: GlucoseIndexItem.Type,
    val bg: Drawable?,
    val value: String,
    val unit: String,
    val description: String
) : ListItem {

    override fun getUniqueProperty(): Any = type

    override fun getChangePayload(other: ListItem): Any {
        if (other is GlucoseIndexItem) {
            return mutableSetOf<Payload>().apply {
                if (type != other.type) add(Payload.TYPE_CHANGED)
                if (bg != other.bg) add(Payload.BG_CHANGED)
                if (unit != other.unit) add(Payload.UNIT_CHANGED)
                if (value != other.value) add(Payload.VALUE_CHANGED)
            }
        }
        return super.getChangePayload(other)
    }

    enum class Payload {
        TYPE_CHANGED, BG_CHANGED, UNIT_CHANGED, VALUE_CHANGED
    }

    enum class Type {
        AVERAGE, TOTAL, HIGH, NORMAL, LOW
    }
}
