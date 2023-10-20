package com.elta.android.presentation.features.main.records.mapper

import com.elta.android.common.utils.CommonFormats
import com.elta.android.common.utils.toStringWithFormat
import com.elta.android.domain.features.diary.events.model.EventV2
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

private const val HOURS_IN_DAY = 24
private const val MINUTES_IN_HOUR = 60
private const val SECONDS_IN_MINUTE = 60
private const val ZERO = 0

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

    private fun EventV2.record(): ListItem =
        RecordItem(
            id = id,
            icon = type.toIconWithBg(),
            title = this.toTitle(),
            type = insulinType(),
            count = formatValue(),
            date = formatDate(),
            showLabel = note != null,
            eventType = this.type,
            labelIcon = mealTag?.toIcon()
        )

    private fun EventV2.insulinType() =
        if (type == EventType.INSULIN) {
            resources.getString(type.toName()) + resources.getString(
                R.string.event_type_insulin_medicament,
                medicament?.name.orEmpty()
            )
        } else {
            resources.getString(type.toName())
        }

    private fun EventV2.formatValue(): String? =
        when (type) {
            EventType.INSULIN -> resources.getString(
                R.string.event_type_insulin_pattern,
                value.format().orEmpty()
            )

            EventType.BREAD -> resources.getString(
                R.string.event_type_bread_pattern,
                value.format().orEmpty()
            )

            EventType.WEIGHT -> resources.getString(
                R.string.event_type_weight_pattern,
                value.format().orEmpty()
            )

            EventType.GLUCOSE -> resources.getString(
                R.string.event_type_glucose_pattern,
                value.format().orEmpty()
            )

            EventType.ACTIVITY -> this.formatDuration(resources)
            else -> null
        }

    private fun EventV2.formatDuration(resources: ResourceProvider): String =
        checkNotNull(duration).asTimeString(resources)

    @Suppress("SwallowedException", "TooGenericExceptionCaught")
    private fun EventV2.formatDate(): String {
        return try {
            val time = additionTime.toStringWithFormat(CommonFormats.FORMAT_TIME)
            val offset = additionTime.offset?.toString().orEmpty()
            resources.getString(R.string.main_records_event_time_mask, time, offset)
        } catch (e: Exception) {
            ""
        }
    }

    private fun EventV2.toTitle(): String =
        when (type) {
            EventType.INSULIN -> medicament?.insulinType?.name.orEmpty()
            EventType.ACTIVITY -> activityType?.let { resources.getString(it.toName()) }
                ?: resources.getString(R.string.event_type_activity_no_name)

            EventType.BREAD -> kind ?: resources.getString(R.string.event_type_bread_no_name)
            EventType.MEDICAMENTS -> checkNotNull(name)
            EventType.WEIGHT -> resources.getString(R.string.event_type_weight_no_name)
            EventType.GLUCOSE -> resources.getString(R.string.event_type_glucose_no_name)
            else -> ""
        }

    private fun Long.asTimeString(resources: ResourceProvider): String {
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
            }
            if (seconds > ZERO && isEmpty()) {
                append(" ")
                append(resources.getString(R.string.activity_duration_sec, seconds.toInt()))
            }
        }

        return time.toString()
    }

    protected fun Double?.format(): String? =
        this?.let { NumberFormatter.numberFormat.format(it) }
}
