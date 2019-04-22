package com.elta.android.presentation.features.profile.settings.reminders.all.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.domain.features.reminder.model.Reminder
import com.elta.android.presentation.R
import com.elta.android.presentation.features.profile.settings.reminders.all.ui.adapter.items.ReminderHeaderItem
import com.elta.android.presentation.features.profile.settings.reminders.all.ui.adapter.items.ReminderItem
import com.elta.android.presentation.utils.toString
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.resources.ResourceProvider
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class ReminderMapper @Inject constructor(
    private val resources: ResourceProvider
) : Mapper<List<Reminder>, List<ListItem>> {

    private val dateFormat by lazy { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    override fun mapFromObject(source: List<Reminder>): List<ListItem> =
        arrayListOf<ListItem>().apply {
            add(ReminderHeaderItem(resources.getString(R.string.profile_reminders_header_text)))
            addAll(source.map { mapFromReminder(it) })
        }

    private fun mapFromReminder(source: Reminder): ListItem =
        with(source) {
            ReminderItem(
                id = id,
                type = R.drawable.ic_notification_bg,
                title = title,
                description = formatSchedule(),
                action = R.drawable.ic_arrow_left
            )
        }

    private fun Reminder.formatSchedule(): String {
        return try {
            if (time == null) {
                scheduleType.toString(resources)
            } else {
                val time = dateFormat.format(time)
                "${scheduleType.toString(resources)} в $time"
            }
        } catch (e: Exception) {
            ""
        }
    }
}