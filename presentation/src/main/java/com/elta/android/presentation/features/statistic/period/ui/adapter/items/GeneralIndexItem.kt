package com.elta.android.presentation.features.statistic.period.ui.adapter.items

import com.nullgr.core.adapter.items.ListItem

data class GeneralIndexItem(
    val icon: Int,
    val title: String,
    val description: String,
    val value: String,
    val isTheLast: Boolean = false
) : ListItem {

    override fun getUniqueProperty(): Any = title

    override fun getChangePayload(other: ListItem): Any {
        if (other is GeneralIndexItem) {
            return mutableSetOf<Payload>().apply {
                if (icon != other.icon) add(Payload.ICON_CHANGED)
                if (title != other.title) add(Payload.TITLE_CHANGED)
                if (description != other.description) add(Payload.DESCRIPTION_CHANGED)
                if (isTheLast != other.isTheLast) add(Payload.POSITION_CHANGED)
            }
        }
        return super.getChangePayload(other)
    }

    enum class Payload {
        ICON_CHANGED,
        TITLE_CHANGED,
        DESCRIPTION_CHANGED,
        POSITION_CHANGED
    }
}