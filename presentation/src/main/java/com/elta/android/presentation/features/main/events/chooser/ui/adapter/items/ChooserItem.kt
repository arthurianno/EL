package com.elta.android.presentation.features.main.events.chooser.ui.adapter.items

import com.nullgr.core.adapter.items.ListItem

data class ChooserItem(
    val id: String,
    val title: String,
    val iconId: Int?,
    val meta: Any,
    var isSelected: Boolean = false
) : ListItem {

    override fun getUniqueProperty() = id

    override fun getChangePayload(other: ListItem): Any {
        if (other is ChooserItem) {
            return mutableSetOf<Payload>().apply {
                if (isSelected != other.isSelected) add(Payload.SELECTION_CHANGED)
            }
        }
        return super.getChangePayload(other)
    }

    enum class Payload {
        SELECTION_CHANGED
    }
}