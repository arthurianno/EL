package com.elta.android.presentation.features.profile.settings.reminders.all.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.common.utils.CommonFormats
import com.elta.android.common.utils.toStringWithFormat
import com.elta.android.domain.features.reminder.model.Reminder
import com.elta.android.presentation.R
import com.elta.android.presentation.features.profile.settings.reminders.all.model.ReminderHeaderItem
import com.elta.android.presentation.features.profile.settings.reminders.all.model.ReminderItem
import com.elta.android.presentation.utils.toString
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.resources.ResourceProvider
import javax.inject.Inject

class ReminderMapper @Inject constructor(
    private val resources: ResourceProvider
) : Mapper<List<Reminder>, List<ListItem>> {

    override fun mapFromObject(source: List<Reminder>): List<ListItem> =
        arrayListOf<ListItem>().apply {
            if (source.isNotEmpty()) {
                add(ReminderHeaderItem(resources.getString(R.string.profile_reminders_header_text)))
            }
            addAll(source.toListItems())
        }

    private fun Reminder.toListItem(): ListItem =
        ReminderItem(
            id = id,
            type = R.drawable.ic_notification_bg,
            title = title,
            description = formatSchedule(),
            action = R.drawable.ic_arrow_left
        )

    private fun List<Reminder>.toListItems(): List<ListItem> =
        map { it.toListItem() }

    private fun Reminder.formatSchedule(): String =
        runCatching {
            "${scheduleType.toString(resources)} в ${date.toStringWithFormat(CommonFormats.FORMAT_TIME)}"
        }.getOrDefault("")
}
