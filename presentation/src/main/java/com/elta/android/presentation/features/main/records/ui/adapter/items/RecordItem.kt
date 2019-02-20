package com.elta.android.presentation.features.main.records.ui.adapter.items

import com.nullgr.core.adapter.items.ListItem

data class RecordItem(
    val id: Any,
    val icon: Int,
    val title: String,
    val type: String,
    val count: String? = null,
    val date: String,
    val showLabel: Boolean
) : ListItem {

    override fun getUniqueProperty(): Any = id

    override fun getChangePayload(other: ListItem): Any {
        if (other is RecordItem) {
            return mutableSetOf<Payload>().apply {
                if (icon != other.icon) add(Payload.ICON_CHANGED)
                if (title != other.title) add(Payload.TITLE_CHANGED)
                if (type != other.type) add(Payload.TYPE_CHANGED)
                if (count != other.count) add(Payload.COUNT_CHANGED)
                if (date != other.date) add(Payload.DATE_CHANGED)
                if (showLabel != other.showLabel) add(Payload.LABEL_CHANGED)
            }
        }
        return super.getChangePayload(other)
    }

    enum class Payload {
        ICON_CHANGED,
        TITLE_CHANGED,
        TYPE_CHANGED,
        COUNT_CHANGED,
        DATE_CHANGED,
        LABEL_CHANGED
    }
}