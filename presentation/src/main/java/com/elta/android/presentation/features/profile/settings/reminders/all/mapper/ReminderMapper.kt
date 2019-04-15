package com.elta.android.presentation.features.profile.settings.reminders.all.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.domain.features.reminder.model.PeriodicType
import com.elta.android.domain.features.reminder.model.Reminder
import com.elta.android.presentation.R
import com.elta.android.presentation.features.profile.settings.reminders.all.ui.adapter.items.ReminderHeaderItem
import com.elta.android.presentation.features.profile.settings.reminders.all.ui.adapter.items.ReminderItem
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
                description = formatPeriodic(),
                action = R.drawable.ic_arrow_left
            )
        }

    private fun Reminder.formatPeriodic(): String {
        return try {
            if (time == null) {
                periodic.toString(resources)
            } else {
                val time = dateFormat.format(time)
                "${periodic.toString(resources)} в $time"
            }
        } catch (e: Exception) {
            ""
        }
    }

    private fun PeriodicType.toString(resources: ResourceProvider): String =
        when (this) {
            PeriodicType.NONE -> resources.getString(R.string.profile_reminders_periodic_not_repeat)
            PeriodicType.DAY -> resources.getString(R.string.profile_reminders_periodic_day)
            PeriodicType.WEEK -> resources.getString(R.string.profile_reminders_periodic_week)
            PeriodicType.MONTH -> resources.getString(R.string.profile_reminders_periodic_month)
            PeriodicType.YEAR -> resources.getString(R.string.profile_reminders_periodic_year)
        }
}