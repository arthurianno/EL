package com.elta.android.presentation.features.main.records.mapper

import com.elta.android.common.utils.CommonFormats
import com.elta.android.common.utils.toStringWithFormat
import com.elta.android.domain.features.diary.events.model.Event
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.home.model.EventsBlock
import com.elta.android.presentation.R
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordItem
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordsGroupItem
import com.elta.android.presentation.utils.NumberFormatter
import com.elta.android.presentation.utils.toIcon
import com.elta.android.presentation.utils.toIconWithBg
import com.elta.android.presentation.utils.toName
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.resources.ResourceProvider
import java.util.concurrent.TimeUnit

open class BaseRecordsMapper(
    protected val resources: ResourceProvider
) {

    protected fun EventsBlock.group(expand: Boolean): ListItem =
        RecordsGroupItem(
            id = tag?.id ?: "tag",
            icon = tag.toIcon(),
            name = tag.toName(resources),
            items = events.map { it.record() },
            isExpanded = expand
        )

    protected fun Event.record(): ListItem =
        RecordItem(
            id = id,
            icon = type.toIconWithBg(),
            title = this.toTitle(),
            type = resources.getString(type.toName()),
            count = formatValue(),
            date = formatDate(),
            showLabel = note != null,
            eventType = this.type
        )

    protected fun Event.formatValue(): String? =
        when (type) {
            EventType.INSULIN -> resources.getString(
                R.string.event_type_insulin_pattern,
                value.format()
                    ?: ""
            )
            EventType.BREAD -> resources.getString(
                R.string.event_type_bread_pattern,
                value.format()
                    ?: ""
            )
            EventType.WEIGHT -> resources.getString(
                R.string.event_type_weight_pattern,
                value.format()
                    ?: ""
            )
            EventType.GLUCOSE -> resources.getString(
                R.string.event_type_glucose_pattern,
                value.format()
                    ?: ""
            )
            EventType.ACTIVITY -> this.formatDuration(resources)
            else -> null
        }

    protected fun Event.formatDuration(resources: ResourceProvider): String =
        checkNotNull(duration).asTimeString(resources)

    @Suppress("SwallowedException", "TooGenericExceptionCaught")
    protected fun Event.formatDate(): String {
        return try {
            val time = additionTime?.toStringWithFormat(CommonFormats.FORMAT_TIME) ?: ""
            val offset = additionTime?.offset?.toString() ?: ""
            resources.getString(R.string.main_records_event_time_mask, time, offset)
        } catch (e: Exception) {
            ""
        }
    }

    protected fun Event.toTitle(): String =
        when (type) {
            EventType.INSULIN -> resources.getString(checkNotNull(insulinType).toName())
            EventType.ACTIVITY -> activityType?.let { resources.getString(it.toName()) }
                ?: resources.getString(R.string.event_type_activity_no_name)
            EventType.BREAD -> kind?.let { it }
                ?: resources.getString(R.string.event_type_bread_no_name)
            EventType.MEDICAMENTS -> checkNotNull(name)
            EventType.WEIGHT -> resources.getString(R.string.event_type_weight_no_name)
            EventType.GLUCOSE -> resources.getString(R.string.event_type_glucose_no_name)
            else -> ""
        }

    protected fun Long.asTimeString(resources: ResourceProvider): String {

        val days = TimeUnit.SECONDS.toDays(this)
        val hours = TimeUnit.SECONDS.toHours(this) - days * HOURS_IN_DAY
        val minutes =
            TimeUnit.SECONDS.toMinutes(this) - TimeUnit.SECONDS.toHours(this) * MINUTES_IN_HOUR
        val seconds =
            TimeUnit.SECONDS.toSeconds(this) - TimeUnit.SECONDS.toMinutes(this) * SECONDS_IN_MINUTE

        val time = StringBuilder().apply {
            if (days > ZERO) {
                append(resources.getString(R.string.activity_duration_day, days.toInt()))
                append(" ")
            }
            if (hours > ZERO) {
                append(resources.getString(R.string.activity_duration_hour, hours.toInt()))
                append(" ")
            }
            if (minutes > ZERO) {
                append(resources.getString(R.string.activity_duration_min, minutes.toInt()))
                append(" ")
            }
            if (seconds > ZERO && isEmpty()) {
                append(resources.getString(R.string.activity_duration_sec, seconds.toInt()))
            }
        }

        return time.toString()
    }

    protected fun Double?.format(): String? =
        when {
            this == null -> null
            else -> NumberFormatter.numberFormat.format(this)
        }

    private companion object {
        const val HOURS_IN_DAY = 24
        const val MINUTES_IN_HOUR = 60
        const val SECONDS_IN_MINUTE = 60
        const val ZERO = 0
    }
}
