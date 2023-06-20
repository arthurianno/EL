package com.elta.android.presentation.features.profile.settings.reminders.all.model

import com.nullgr.core.adapter.items.ListItem

data class ReminderHeaderItem(val title: String) : ListItem {

    override fun getUniqueProperty() = title
}
