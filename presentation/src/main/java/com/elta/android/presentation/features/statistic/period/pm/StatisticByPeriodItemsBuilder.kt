@file:Suppress("TooManyFunctions")

package com.elta.android.presentation.features.statistic.period.pm

import android.graphics.drawable.Drawable
import com.elta.android.common.utils.toStringWithFormat
import com.elta.android.domain.features.diary.events.model.toGlucoseFormat
import com.elta.android.domain.features.diary.home.model.CalculatorFlow
import com.elta.android.domain.features.diary.home.model.DailyGlucoseModel
import com.elta.android.domain.features.diary.home.model.DoubleRange
import com.elta.android.domain.features.statistics.model.GlucoseStatisticModel
import com.elta.android.domain.features.statistics.model.StatisticByPeriodModel
import com.elta.android.domain.features.statistics.model.daily.DailyStatisticModel
import com.elta.android.presentation.R
import com.elta.android.presentation.features.main.records.mapper.ChartItemsBuilder
import com.elta.android.presentation.features.statistic.period.ui.adapter.items.GeneralIndexItem
import com.elta.android.presentation.features.statistic.period.ui.adapter.items.GlucoseDailyChartItem
import com.elta.android.presentation.features.statistic.period.ui.adapter.items.GlucoseIndexItem
import com.elta.android.presentation.features.statistic.period.ui.adapter.items.GlucoseIndexesItem
import com.elta.android.presentation.utils.NumberFormatter
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.resources.ResourceProvider
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import org.threeten.bp.LocalDate

private const val ZERO = 0L
private const val DAILY_CHART_DATE_FORMAT = "dd MMM. EEEE"

class StatisticByPeriodItemsBuilder @Inject constructor(
    private val resources: ResourceProvider
) {

    fun build(model: StatisticByPeriodModel, date: LocalDate? = null): List<ListItem> {
        val items = mutableListOf<ListItem>()

        val glucoseIndexItems = mutableListOf<ListItem>()
        val glucoseStatisticModel = when (date == null) {
            true -> model.glucose
            else -> model.allDays[date]?.glucose
        }

        GlucoseIndexItem.Type.values().forEach { type ->
            glucoseIndexItems.add(
                GlucoseIndexItem(
                    type = type,
                    bg = type.getBg(glucoseStatisticModel),
                    value = type.geValue(glucoseStatisticModel),
                    unit = type.geUnit(glucoseStatisticModel, isDay = date != null),
                    description = type.getDescription()
                )
            )
        }

        items.add(GlucoseIndexesItem(glucoseIndexItems))
        glucoseStatisticModel?.dailyGlucoseModel?.dailyChart(date)?.let { items.add(it) }

        val types = if (model.calculatorFlow == CalculatorFlow.PRODUCT_ONLY) {
            GeneralIndexItem.Type.values()
                .filterNot { it == GeneralIndexItem.Type.BREAD }
                .toTypedArray()
        } else GeneralIndexItem.Type.values()

        types.forEachIndexed { index, type ->
            val value = type.getValueByDate(model, date)
            items.add(
                GeneralIndexItem(
                    icon = type.getIcon(),
                    title = type.getTitle(),
                    description = type.getDescriptionByDate(model, value, date),
                    value = value,
                    isTheLast = index == types.size - 1
                )
            )
        }

        return items
    }

    private fun DailyGlucoseModel.dailyChart(date: LocalDate?): GlucoseDailyChartItem? {
        if (date == null) return null
        val dateTitle = date.toStringWithFormat(DAILY_CHART_DATE_FORMAT)
        return GlucoseDailyChartItem(ChartItemsBuilder.build(this), dateTitle)
    }

    private fun GlucoseIndexItem.Type.getBg(glucose: GlucoseStatisticModel?): Drawable? =
        when (this) {
            GlucoseIndexItem.Type.AVERAGE -> glucose.getAverageBg()
            GlucoseIndexItem.Type.TOTAL -> resources.getDrawable(R.drawable.bg_glucose_index_total)
            GlucoseIndexItem.Type.HIGH -> resources.getDrawable(R.drawable.bg_glucose_index_high)
            GlucoseIndexItem.Type.NORMAL -> resources.getDrawable(R.drawable.bg_glucose_index_normal)
            GlucoseIndexItem.Type.LOW -> resources.getDrawable(R.drawable.bg_glucose_index_low)
        }

    private fun GlucoseStatisticModel?.getAverageBg(): Drawable? =
        when {
            this.isAverageIn(this?.settings?.high) -> resources.getDrawable(R.drawable.bg_glucose_index_high)
            this.isAverageIn(this?.settings?.normal) -> resources.getDrawable(R.drawable.bg_glucose_index_normal)
            this.isAverageIn(this?.settings?.low) -> resources.getDrawable(R.drawable.bg_glucose_index_low)
            else -> resources.getDrawable(R.drawable.bg_glucose_index_total)
        }

    private fun GlucoseIndexItem.Type.geValue(glucose: GlucoseStatisticModel?): String =
        when (this) {
            GlucoseIndexItem.Type.AVERAGE -> NumberFormatter.format(
                glucose?.averageLevel?.toGlucoseFormat(glucose.glucoseFormat)
                    ?: ZERO.toDouble()
            )

            GlucoseIndexItem.Type.TOTAL -> glucose?.eventsCount.toString()
            GlucoseIndexItem.Type.HIGH -> glucose?.eventsHighCount.toString()
            GlucoseIndexItem.Type.NORMAL -> glucose?.eventsNormalCount.toString()
            GlucoseIndexItem.Type.LOW -> glucose?.eventsLowCount.toString()
        }

    private fun GlucoseIndexItem.Type.geUnit(
        glucose: GlucoseStatisticModel?,
        isDay: Boolean
    ): String =
        when (this) {
            GlucoseIndexItem.Type.AVERAGE -> resources.getString(R.string.statistic_glucose_index_average_unit)
            GlucoseIndexItem.Type.TOTAL -> if (isDay) {
                resources.getString(R.string.statistic_glucose_index_day_unit)
            } else {
                resources.getString(R.string.statistic_glucose_index_total_unit)
            }

            GlucoseIndexItem.Type.HIGH -> resources.getString(
                R.string.statistic_glucose_index_level_unit,
                glucose?.eventsHighPercent.toString()
            )

            GlucoseIndexItem.Type.NORMAL -> resources.getString(
                R.string.statistic_glucose_index_level_unit,
                glucose?.eventsNormalPercent.toString()
            )

            GlucoseIndexItem.Type.LOW -> resources.getString(
                R.string.statistic_glucose_index_level_unit,
                glucose?.eventsLowPercent.toString()
            )
        }

    private fun GlucoseIndexItem.Type.getDescription(): String =
        when (this) {
            GlucoseIndexItem.Type.AVERAGE -> resources.getString(R.string.statistic_glucose_index_description_average)
            GlucoseIndexItem.Type.TOTAL -> resources.getString(R.string.statistic_glucose_index_description_total)
            GlucoseIndexItem.Type.HIGH -> resources.getString(R.string.statistic_glucose_index_description_high)
            GlucoseIndexItem.Type.NORMAL -> resources.getString(R.string.statistic_glucose_index_description_normal)
            GlucoseIndexItem.Type.LOW -> resources.getString(R.string.statistic_glucose_index_description_low)
        }

    private fun GeneralIndexItem.Type.getIcon(): Int =
        when (this) {
            GeneralIndexItem.Type.BREAD -> R.drawable.ic_event_bread_with_bg
            GeneralIndexItem.Type.TOTAL -> R.drawable.ic_event_insulin_with_bg
            GeneralIndexItem.Type.BOLUS -> R.drawable.ic_event_insulin_with_bg
            GeneralIndexItem.Type.BASAL -> R.drawable.ic_event_insulin_with_bg
            GeneralIndexItem.Type.ACTIVITY -> R.drawable.ic_event_activity_with_bg
        }

    private fun GeneralIndexItem.Type.getTitle(): String =
        when (this) {
            GeneralIndexItem.Type.BREAD -> resources.getString(R.string.statistic_general_index_title_bread)
            GeneralIndexItem.Type.TOTAL -> resources.getString(R.string.statistic_general_index_title_insulin)
            GeneralIndexItem.Type.BOLUS -> resources.getString(R.string.statistic_general_index_title_bolus_insulin)
            GeneralIndexItem.Type.BASAL -> resources.getString(R.string.statistic_general_index_title_basal_insulin)
            GeneralIndexItem.Type.ACTIVITY -> resources.getString(R.string.statistic_general_index_title_activity)
        }

    private fun GeneralIndexItem.Type.getDescriptionByDate(
        stat: StatisticByPeriodModel,
        value: String,
        date: LocalDate?
    ): String {

        return if (date != null) {
            getDescriptionByDay(
                eventsCount = stat.allDays[date]?.activity?.eventsCount,
                value = value,
                stat.insulin.statisticBasal,
                stat.insulin.statisticBolus
            )
        } else {
            getDescriptionByPeriod(
                eventsCount = stat.activity.eventsCount,
                value = value,
                stat.insulin.statisticBasal,
                stat.insulin.statisticBolus
            )
        }
    }

    private fun GeneralIndexItem.Type.getDescriptionByDay(
        eventsCount: Int?,
        value: String,
        statisticBasal: List<String>,
        statisticBolus: List<String>
    ): String =
        when (this) {
            GeneralIndexItem.Type.BREAD -> resources.getString(
                R.string.statistic_general_index_description_by_day_bread,
                value
            )

            GeneralIndexItem.Type.TOTAL -> resources.getString(
                R.string.statistic_general_index_description_by_day_insulin,
                (statisticBasal + statisticBolus).statisticString(),
                value
            )

            GeneralIndexItem.Type.BOLUS -> resources.getString(
                R.string.statistic_general_index_description_by_day_bolus_insulin,
                statisticBolus.statisticString(),
                value
            )

            GeneralIndexItem.Type.BASAL -> resources.getString(
                R.string.statistic_general_index_description_by_day_basal_insulin,
                statisticBasal.statisticString(),
                value,
            )

            GeneralIndexItem.Type.ACTIVITY -> resources.getString(
                R.string.statistic_general_index_description_by_day_activity,
                eventsCount.toString(),
                value
            )
        }

    private fun GeneralIndexItem.Type.getDescriptionByPeriod(
        eventsCount: Int?,
        value: String,
        statisticBasal: List<String>,
        statisticBolus: List<String>
    ): String =
        when (this) {
            GeneralIndexItem.Type.BREAD -> resources.getString(
                R.string.statistic_general_index_description_by_period_bread,
                value
            )

            GeneralIndexItem.Type.TOTAL -> resources.getString(
                R.string.statistic_general_index_description_by_period_insulin,
                (statisticBasal + statisticBolus).statisticString(),
                value
            )

            GeneralIndexItem.Type.BOLUS -> resources.getString(
                R.string.statistic_general_index_description_by_period_bolus_insulin,
                statisticBolus.statisticString(),
                value,
            )

            GeneralIndexItem.Type.BASAL -> resources.getString(
                R.string.statistic_general_index_description_by_period_basal_insulin,
                statisticBasal.statisticString(),
                value,
            )

            GeneralIndexItem.Type.ACTIVITY -> resources.getString(
                R.string.statistic_general_index_description_by_period_activity,
                eventsCount.toString(),
                value
            )
        }

    private fun List<String>.statisticString() =
        joinToString(separator = " + ", prefix = "(", postfix = ")")

    private fun GeneralIndexItem.Type.getValueByDate(
        model: StatisticByPeriodModel,
        date: LocalDate?
    ): String =
        when (date == null) {
            true -> getValue(model)
            else -> getValue(model.allDays[date])
        }

    private fun GeneralIndexItem.Type.getValue(stat: StatisticByPeriodModel?): String =
        when (this) {
            GeneralIndexItem.Type.BREAD -> resources.getString(
                R.string.statistic_general_index_description_value_by_period_bread,
                stat?.food?.averageLevel.format()
            )

            GeneralIndexItem.Type.TOTAL -> resources.getString(
                R.string.statistic_general_index_description_value_by_period_insulin,
                stat?.insulin?.averageLevel.format()
            )

            GeneralIndexItem.Type.BOLUS -> resources.getString(
                R.string.statistic_general_index_description_value_by_period_insulin,
                stat?.insulin?.averageBolusLevel.format()
            )

            GeneralIndexItem.Type.BASAL -> resources.getString(
                R.string.statistic_general_index_description_value_by_period_insulin,
                stat?.insulin?.averageBasalLevel.format()
            )

            GeneralIndexItem.Type.ACTIVITY -> stat?.activity?.averageDuration.asTimeString(resources)
        }

    private fun GeneralIndexItem.Type.getValue(stat: DailyStatisticModel?): String =
        when (this) {
            GeneralIndexItem.Type.BREAD -> resources.getString(
                R.string.statistic_general_index_description_value_by_period_bread,
                stat?.bread?.totalLevel.format()
            )

            GeneralIndexItem.Type.TOTAL -> resources.getString(
                R.string.statistic_general_index_description_value_by_period_insulin,
                stat?.insulin?.totalLevel.format()
            )

            GeneralIndexItem.Type.BOLUS -> resources.getString(
                R.string.statistic_general_index_description_value_by_period_insulin,
                stat?.insulin?.totalBolusLevel.format()
            )

            GeneralIndexItem.Type.BASAL -> resources.getString(
                R.string.statistic_general_index_description_value_by_period_insulin,
                stat?.insulin?.totalBasalLevel.format()
            )

            GeneralIndexItem.Type.ACTIVITY -> stat?.activity?.averageDuration.asTimeString(resources)
        }

    private fun Long?.asTimeString(resources: ResourceProvider): String {
        val duration = this ?: ZERO
        val minutes = TimeUnit.SECONDS.toMinutes(duration)

        val time = StringBuilder().apply {
            if (isNotEmpty()) append("\u00A0") // non breaking space
            append(resources.getString(R.string.activity_duration_min, minutes.toInt()))
        }

        return time.toString()
    }

    private fun Int?.toString() = (this ?: ZERO).toString()

    private fun Double?.format() = NumberFormatter.format(this ?: 0.0)

    private fun GlucoseStatisticModel?.isAverageIn(range: DoubleRange?) =
        this != null && this.eventsCount > 0 && range?.contains(this.averageLevel) ?: false
}
