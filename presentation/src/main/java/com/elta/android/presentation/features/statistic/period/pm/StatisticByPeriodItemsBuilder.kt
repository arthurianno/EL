package com.elta.android.presentation.features.statistic.period.pm

import android.graphics.drawable.Drawable
import com.elta.android.domain.features.statistics.model.GlucoseStatisticModel
import com.elta.android.domain.features.statistics.model.StatisticByPeriodModel
import com.elta.android.presentation.R
import com.elta.android.presentation.features.statistic.period.ui.adapter.items.GeneralIndexItem
import com.elta.android.presentation.features.statistic.period.ui.adapter.items.GlucoseIndexItem
import com.elta.android.presentation.features.statistic.period.ui.adapter.items.GlucoseIndexesItem
import com.elta.android.presentation.utils.NumberFormatter
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.resources.ResourceProvider
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class StatisticByPeriodItemsBuilder @Inject constructor(
    private val resources: ResourceProvider
) {

    fun build(model: StatisticByPeriodModel): List<ListItem> {
        val items = arrayListOf<ListItem>()

        val glucoseIndexItems = arrayListOf<ListItem>()

        GlucoseIndexItem.Type.values().forEach { type ->
            glucoseIndexItems.add(
                GlucoseIndexItem(
                    type = type,
                    bg = type.getBg(model.glucose),
                    value = type.geValue(model.glucose),
                    unit = type.geUnit(model.glucose),
                    description = type.getDescription()
                )
            )
        }

        items.add(GlucoseIndexesItem(glucoseIndexItems))

        val types = GeneralIndexItem.Type.values()
        types.forEachIndexed { index, type ->
            val value = type.getValue(model)
            items.add(
                GeneralIndexItem(
                    icon = type.getIcon(),
                    title = type.getTitle(),
                    description = type.getDescription(model, value),
                    value = value,
                    isTheLast = index == types.size - 1
                )
            )
        }

        return items
    }

    private inline fun GlucoseIndexItem.Type.getBg(glucose: GlucoseStatisticModel): Drawable? =
        when (this) {
            GlucoseIndexItem.Type.AVERAGE -> glucose.getAverageBg()
            GlucoseIndexItem.Type.TOTAL -> resources.getDrawable(R.drawable.bg_glucose_index_total)
            GlucoseIndexItem.Type.HIGH -> resources.getDrawable(R.drawable.bg_glucose_index_high)
            GlucoseIndexItem.Type.NORMAL -> resources.getDrawable(R.drawable.bg_glucose_index_normal)
            GlucoseIndexItem.Type.LOW -> resources.getDrawable(R.drawable.bg_glucose_index_low)
        }

    private inline fun GlucoseStatisticModel.getAverageBg(): Drawable? =
        when (averageLevel) {
            in settings.high -> resources.getDrawable(R.drawable.bg_glucose_index_high)
            in settings.normal -> resources.getDrawable(R.drawable.bg_glucose_index_normal)
            in settings.low -> resources.getDrawable(R.drawable.bg_glucose_index_low)
            else -> resources.getDrawable(R.drawable.bg_glucose_index_total)
        }

    private inline fun GlucoseIndexItem.Type.geValue(glucose: GlucoseStatisticModel): String =
        when (this) {
            GlucoseIndexItem.Type.AVERAGE -> NumberFormatter.format(glucose.averageLevel)
            GlucoseIndexItem.Type.TOTAL -> glucose.eventsCount.toString()
            GlucoseIndexItem.Type.HIGH -> glucose.eventsHighCount.toString()
            GlucoseIndexItem.Type.NORMAL -> glucose.eventsNormalCount.toString()
            GlucoseIndexItem.Type.LOW -> glucose.eventsLowCount.toString()
        }

    private inline fun GlucoseIndexItem.Type.geUnit(glucose: GlucoseStatisticModel): String =
        when (this) {
            GlucoseIndexItem.Type.AVERAGE -> resources.getString(R.string.statistic_glucose_index_average_unit)
            GlucoseIndexItem.Type.TOTAL -> resources.getString(R.string.statistic_glucose_index_total_unit)
            GlucoseIndexItem.Type.HIGH -> resources.getString(
                R.string.statistic_glucose_index_level_unit,
                glucose.eventsHighPercent.toString()
            )
            GlucoseIndexItem.Type.NORMAL -> resources.getString(
                R.string.statistic_glucose_index_level_unit,
                glucose.eventsNormalPercent.toString()
            )
            GlucoseIndexItem.Type.LOW -> resources.getString(
                R.string.statistic_glucose_index_level_unit,
                glucose.eventsLowPercent.toString()
            )
        }

    private inline fun GlucoseIndexItem.Type.getDescription(): String =
        when (this) {
            GlucoseIndexItem.Type.AVERAGE -> resources.getString(R.string.statistic_glucose_index_description_average)
            GlucoseIndexItem.Type.TOTAL -> resources.getString(R.string.statistic_glucose_index_description_total)
            GlucoseIndexItem.Type.HIGH -> resources.getString(R.string.statistic_glucose_index_description_high)
            GlucoseIndexItem.Type.NORMAL -> resources.getString(R.string.statistic_glucose_index_description_normal)
            GlucoseIndexItem.Type.LOW -> resources.getString(R.string.statistic_glucose_index_description_low)
        }

    private inline fun GeneralIndexItem.Type.getIcon(): Int =
        when (this) {
            GeneralIndexItem.Type.BREAD -> R.drawable.ic_event_bread_with_bg
            GeneralIndexItem.Type.TOTAL -> R.drawable.ic_event_insulin_with_bg
            GeneralIndexItem.Type.BOLUS -> R.drawable.ic_event_insulin_with_bg
            GeneralIndexItem.Type.BASAL -> R.drawable.ic_event_insulin_with_bg
            GeneralIndexItem.Type.ACTIVITY -> R.drawable.ic_event_activity_with_bg
        }

    private inline fun GeneralIndexItem.Type.getTitle(): String =
        when (this) {
            GeneralIndexItem.Type.BREAD -> resources.getString(R.string.statistic_general_index_title_bread)
            GeneralIndexItem.Type.TOTAL -> resources.getString(R.string.statistic_general_index_title_insulin)
            GeneralIndexItem.Type.BOLUS -> resources.getString(R.string.statistic_general_index_title_bolus_insulin)
            GeneralIndexItem.Type.BASAL -> resources.getString(R.string.statistic_general_index_title_basal_insulin)
            GeneralIndexItem.Type.ACTIVITY -> resources.getString(R.string.statistic_general_index_title_activity)
        }

    private inline fun GeneralIndexItem.Type.getDescription(stat: StatisticByPeriodModel, value: String): String =
        when (this) {
            GeneralIndexItem.Type.BREAD -> resources.getString(
                R.string.statistic_general_index_description_by_period_bread,
                value
            )
            GeneralIndexItem.Type.TOTAL -> resources.getString(
                R.string.statistic_general_index_description_by_period_insulin,
                value
            )
            GeneralIndexItem.Type.BOLUS -> resources.getString(
                R.string.statistic_general_index_description_by_period_bolus_insulin,
                value
            )
            GeneralIndexItem.Type.BASAL -> resources.getString(
                R.string.statistic_general_index_description_by_period_basal_insulin,
                value
            )
            GeneralIndexItem.Type.ACTIVITY -> resources.getString(
                R.string.statistic_general_index_description_by_period_activity,
                stat.activity.eventsCount.toString(),
                value
            )
        }

    private inline fun GeneralIndexItem.Type.getValue(stat: StatisticByPeriodModel): String =
        when (this) {
            GeneralIndexItem.Type.BREAD -> resources.getString(
                R.string.statistic_general_index_description_value_by_period_bread,
                NumberFormatter.format(stat.bread.averageLevel)
            )
            GeneralIndexItem.Type.TOTAL -> resources.getString(
                R.string.statistic_general_index_description_value_by_period_insulin,
                NumberFormatter.format(stat.insulin.averageLevel)
            )
            GeneralIndexItem.Type.BOLUS -> resources.getString(
                R.string.statistic_general_index_description_value_by_period_insulin,
                NumberFormatter.format(stat.insulin.averageBolusLevel)
            )
            GeneralIndexItem.Type.BASAL -> resources.getString(
                R.string.statistic_general_index_description_value_by_period_insulin,
                NumberFormatter.format(stat.insulin.averageBasalLevel)
            )
            GeneralIndexItem.Type.ACTIVITY -> stat.activity.averageDuration.asTimeString(resources)
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
            if (days == ZERO && hours == ZERO && minutes == ZERO && seconds == ZERO) {
                append(resources.getString(R.string.activity_duration_min, minutes.toInt()))
            }
            append(".")
        }

        return time.toString()
    }

    companion object {
        const val HOURS_IN_DAY = 24
        const val MINUTES_IN_HOUR = 60
        const val SECONDS_IN_MINUTE = 60
        const val ZERO = 0L
    }
}