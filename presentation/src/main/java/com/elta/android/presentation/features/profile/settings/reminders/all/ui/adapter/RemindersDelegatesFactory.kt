package com.elta.android.presentation.features.profile.settings.reminders.all.ui.adapter

import com.elta.android.presentation.features.profile.settings.reminders.all.ui.adapter.delegates.ReminderDelegate
import com.elta.android.presentation.features.profile.settings.reminders.all.ui.adapter.delegates.ReminderHeaderDelegate
import com.elta.android.presentation.features.profile.settings.reminders.all.ui.adapter.items.ReminderHeaderItem
import com.elta.android.presentation.features.profile.settings.reminders.all.ui.adapter.items.ReminderItem
import com.nullgr.core.adapter.AdapterDelegate
import com.nullgr.core.adapter.AdapterDelegatesFactory
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.resources.ResourceProvider
import com.nullgr.core.rx.RxBus
import javax.inject.Inject

class RemindersDelegatesFactory @Inject constructor(
    private val bus: RxBus,
    private val resources: ResourceProvider
) : AdapterDelegatesFactory {

    override fun createDelegate(clazz: Class<ListItem>): AdapterDelegate =
        when (clazz) {
            ReminderHeaderItem::class.java -> ReminderHeaderDelegate()
            ReminderItem::class.java -> ReminderDelegate(bus, resources)
            else -> throw IllegalArgumentException("No delegate defined for ${clazz.simpleName}")
        }
}