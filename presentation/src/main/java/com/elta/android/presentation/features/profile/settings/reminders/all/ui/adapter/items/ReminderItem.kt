package com.elta.android.presentation.features.profile.settings.reminders.all.ui.adapter.items

import androidx.annotation.DrawableRes
import com.nullgr.core.adapter.items.ListItem

data class ReminderItem(
    val id: String,
    @DrawableRes
    val type: Int,
    val title: String,
    val description: String,
    @DrawableRes
    val action: Int
) : ListItem {

    override fun getUniqueProperty() = id

    override fun getChangePayload(other: ListItem): Any {
        if (other is ReminderItem) {
            return mutableSetOf<Payload>().apply {
                if (title != other.title) add(Payload.TITLE_CHANGED)
                if (description != other.description) add(Payload.DESCRIPTION_CHANGED)
            }
        }
        return super.getChangePayload(other)
    }

    enum class Payload {
        TITLE_CHANGED,
        DESCRIPTION_CHANGED
    }
}
