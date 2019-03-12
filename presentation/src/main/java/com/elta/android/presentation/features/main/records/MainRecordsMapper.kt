package com.elta.android.presentation.features.main.records

import android.graphics.drawable.Drawable
import com.elta.android.common.mapper.Mapper
import com.elta.android.domain.features.diary.events.model.Event
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.home.model.EventsBlock
import com.elta.android.domain.features.diary.home.model.GlucoseLevel
import com.elta.android.domain.features.diary.home.model.GlucoseLevelDirection
import com.elta.android.domain.features.diary.home.model.HomeModel
import com.elta.android.presentation.R
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordItem
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordsGroupItem
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordsHeaderItem
import com.elta.android.presentation.utils.toIcon
import com.elta.android.presentation.utils.toIconWithBg
import com.elta.android.presentation.utils.toName
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.resources.ResourceProvider
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MainRecordsMapper @Inject constructor(
    private val resources: ResourceProvider
) : Mapper<HomeModel, List<ListItem>> {

    private val numberFormat by lazy {
        DecimalFormat("#.#").apply {
            minimumFractionDigits = 1
            decimalFormatSymbols = DecimalFormatSymbols(Locale.getDefault()).apply {
                decimalSeparator = ','
            }
        }
    }

    override fun mapFromObject(source: HomeModel): List<ListItem> =
        arrayListOf<ListItem>().apply {
            add(source.header())
            addAll(source.eventsBlocks.mapIndexed { index, event -> event.group(index == 0) })
        }

    private fun EventsBlock.group(expand: Boolean): ListItem =
        RecordsGroupItem(
            id = tag?.id ?: "tag",
            icon = tag.toIcon(),
            name = tag.toName(resources),
            items = events.map { it.record() },
            isExpanded = expand
        )

    private fun Event.record(): ListItem =
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

    private fun Event.formatValue(): String? =
        when (type) {
            EventType.INSULIN -> resources.getString(R.string.event_type_insulin_pattern, value.format() ?: "")
            EventType.BREAD -> resources.getString(R.string.event_type_bread_pattern, value.format() ?: "")
            EventType.WEIGHT -> resources.getString(R.string.event_type_weight_pattern, value.format() ?: "")
            EventType.GLUCOSE -> resources.getString(R.string.event_type_glucose_pattern, value.format() ?: "")
            EventType.ACTIVITY -> this.formatDuration(resources)
            else -> null
        }

    private fun Event.formatDuration(resources: ResourceProvider): String =
        checkNotNull(duration).asTimeString(resources)

    // TODO: rework this solution using Android310
    private fun Event.formatDate(): String {
        return try {
            val date = SimpleDateFormat(PATTERN, Locale.getDefault()).parse(additionTimeString)
            val tokens = SimpleDateFormat("HH:mm XXX", Locale.getDefault()).format(date).split(" ")
            "в ${tokens[0]} (UTC ${tokens[1]})"
        } catch (e: Exception) {
            ""
        }
    }

    private fun Event.toTitle(): String =
        when (type) {
            EventType.INSULIN -> resources.getString(checkNotNull(insulinType).toName())
            EventType.ACTIVITY -> activityType?.let { resources.getString(it.toName()) }
                ?: resources.getString(R.string.event_type_activity_no_name)
            EventType.BREAD -> kind?.let { it }
                ?: resources.getString(R.string.event_type_bread_no_name)
            EventType.MEDICAMENTS -> checkNotNull(name)
            EventType.WEIGHT -> resources.getString(R.string.weight_name)
            EventType.GLUCOSE -> resources.getString(R.string.event_type_glucose_no_name)
            else -> ""
        }

    private fun HomeModel.header(): ListItem =
        RecordsHeaderItem(
            background = glucoseLevel.toBackground(),
            glucoseLevel = lastGlucoseEvent?.value.format(),
            glucoseLevelIndex = glucoseLevelDifference.format(),
            glucoseLevelIndexIcon = this.glucoseLevelDirection?.icon(),
            breadLevel = lastBreadEvent?.value.format(),
            insulinLevel = lastInsulinEvent?.value.format()
        )

    private fun GlucoseLevelDirection.icon(): Int? =
        when (this) {
            GlucoseLevelDirection.UP -> R.drawable.ic_change_index_up
            GlucoseLevelDirection.DOWN -> R.drawable.ic_change_index_down
            else -> null
        }

    private fun GlucoseLevel?.toBackground(): Drawable? =
        when {
            this == null -> resources.getDrawable(R.drawable.bg_gradient_green)
            this == GlucoseLevel.HIGH -> resources.getDrawable(R.drawable.bg_gradient_red)
            this == GlucoseLevel.LOW -> resources.getDrawable(R.drawable.bg_gradient_blue)
            else -> resources.getDrawable(R.drawable.bg_gradient_green)
        }

    private fun Long.asTimeString(resources: ResourceProvider): String {

        val days = TimeUnit.SECONDS.toDays(this)
        val hours = TimeUnit.SECONDS.toHours(this) - days * HOURS_IN_DAY
        val minutes = TimeUnit.SECONDS.toMinutes(this) - TimeUnit.SECONDS.toHours(this) * MINUTES_IN_HOUR
        val seconds = TimeUnit.SECONDS.toSeconds(this) - TimeUnit.SECONDS.toMinutes(this) * SECONDS_IN_MINUTE

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

    private fun Double?.format(): String? =
        when {
            this == null -> null
            else -> numberFormat.format(this)
        }

    private companion object {
        const val HOURS_IN_DAY = 24
        const val MINUTES_IN_HOUR = 60
        const val SECONDS_IN_MINUTE = 60
        const val ZERO = 0
        const val PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSSXXX"
    }
}