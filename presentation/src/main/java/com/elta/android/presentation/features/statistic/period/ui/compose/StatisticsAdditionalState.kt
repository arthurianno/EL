package com.elta.android.presentation.features.statistic.period.ui.compose

import com.elta.android.domain.features.diary.events.model.MealTag
import com.elta.android.domain.features.diary.events.model.glucoseValue
import com.elta.android.domain.features.diary.tags.model.TagImage
import com.elta.android.domain.features.statistics.model.StatisticByPeriodModel
import org.threeten.bp.LocalDate

data class ActivityDay(val date: LocalDate, val minutes: Double, val glucose: Double?)

data class ActivityStatisticsUiState(
    val days: List<ActivityDay> = emptyList(),
    val averageMinutes: Double? = null
)

data class MealGlucoseUiState(val label: String, val before: Double?, val after: Double?)

data class NutritionStatisticsUiState(
    val calories: Double? = null,
    val fats: Double? = null,
    val proteins: Double? = null,
    val carbohydrates: Double? = null,
    val averageBreadUnits: Double? = null,
    val meals: List<MealGlucoseUiState> = emptyList()
)

internal fun StatisticByPeriodModel.toActivityStatistics(dates: List<LocalDate>): ActivityStatisticsUiState {
    if (activity.eventsCount == 0) return ActivityStatisticsUiState()
    val days = dates.map { date ->
        val day = allDays[date]
        ActivityDay(
            date = date,
            minutes = day?.activity?.let { it.averageDuration * it.eventsCount / 60.0 } ?: 0.0,
            glucose = day?.glucose?.takeIf { it.eventsCount > 0 }?.averageLevel
        )
    }
    return ActivityStatisticsUiState(days, days.map { it.minutes }.average())
}

internal fun StatisticByPeriodModel.toNutritionStatistics(): NutritionStatisticsUiState {
    // Selected servings already contain nutrients recalculated for the consumed portion.
    val servings = foodEvents.flatMap { it.dishes }.map { it.servingSelect }
    fun total(selector: (com.elta.android.domain.features.calculator.model.Serving) -> Double?): Double? =
        servings.mapNotNull(selector).takeIf { it.isNotEmpty() }?.sum()
    val events = allDays.values.flatMap { it.glucose.dailyGlucoseModel?.glucoseEvents.orEmpty() }
    val meals = listOf(
        TagImage.BREAKFAST to "Завтрак", TagImage.LUNCH to "Обед",
        TagImage.SNACK to "Перекус", TagImage.DINNER to "Ужин"
    ).map { (tag, label) ->
        val tagged = events.filter { it.tag?.image == tag }
        fun average(mealTag: MealTag) = tagged.filter { it.mealTag == mealTag }
            .map { it.glucoseValue(glucose.glucoseFormat) }.takeIf { it.isNotEmpty() }?.average()
        MealGlucoseUiState(label, average(MealTag.BEFOREMEAL), average(MealTag.AFTERMEAL))
    }
    return NutritionStatisticsUiState(
        calories = total { it.calories }, fats = total { it.fats },
        proteins = total { it.proteins }, carbohydrates = total { it.carbohydrate },
        averageBreadUnits = foodEvents.takeIf { it.isNotEmpty() }?.let { events ->
            val days = org.threeten.bp.temporal.ChronoUnit.DAYS.between(period.start.toLocalDate(), period.end.toLocalDate()) + 1
            events.sumOf { it.value ?: 0.0 } / days.coerceAtLeast(1)
        },
        meals = meals.takeIf { it.any { meal -> meal.before != null || meal.after != null } }.orEmpty()
    )
}
