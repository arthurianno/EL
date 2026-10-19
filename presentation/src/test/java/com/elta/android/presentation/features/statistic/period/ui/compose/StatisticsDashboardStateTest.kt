package com.elta.android.presentation.features.statistic.period.ui.compose

import com.elta.android.domain.features.calculator.model.Dish
import com.elta.android.domain.features.calculator.model.DishType
import com.elta.android.domain.features.calculator.model.Serving
import com.elta.android.domain.features.diary.events.model.*
import com.elta.android.domain.features.diary.home.model.CalculatorFlow
import com.elta.android.domain.features.diary.home.model.GlucoseLevelSettings
import com.elta.android.domain.features.diary.medicines.model.InsulinMedicamentStatistic
import com.elta.android.domain.features.statistics.interactor.buildStatisticModel
import com.elta.android.domain.features.statistics.model.StatisticPeriod
import com.elta.android.domain.features.user.model.GlucoseFormat
import com.elta.android.presentation.features.statistic.period.ui.Period
import java.util.Locale
import org.junit.Assert.*
import org.junit.Test
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset

class StatisticsDashboardStateTest {
    private val period = object : StatisticPeriod {
        override val start = LocalDateTime.of(2026, 7, 15, 0, 0)
        override val end = start.plusDays(6).withHour(23).withMinute(59)
    }

    @Test fun `empty period shows no sentinel extrema or calculated GMI`() {
        val current = model(emptyList())
        val previous = model(listOf(event(EventType.Glucose(GlucoseInputType.MANUAL), value = 6.0)))
        val state = current.toStatisticsDashboardUiState(Period.SEVEN, previous, Locale.forLanguageTag("ru"))
        assertEquals("—", state.average)
        assertEquals("—", state.minLabel)
        assertEquals("—", state.maxLabel)
        assertEquals("—", state.standardDeviation)
        assertEquals("—", state.gmi)
        assertNull(state.comparison)
        assertNull(state.activity.averageMinutes)
        assertNull(state.nutrition.calories)
    }

    @Test fun `activity averages cover the selected days and use seconds as input`() {
        val state = model(listOf(event(EventType.Activity, duration = 210L * 60)))
            .toStatisticsDashboardUiState(Period.SEVEN)
        assertEquals(7, state.activity.days.size)
        assertEquals(210.0, state.activity.days.first().minutes, 0.001)
        assertEquals(30.0, state.activity.averageMinutes!!, 0.001)
    }

    @Test fun `nutrition uses consumed portion once and GMI includes percentage`() {
        val serving = Serving.empty().copy(calories = 120.0, fats = 3.0, proteins = 6.0, carbohydrate = 18.0, numberOfUnits = 2.0)
        val dish = Dish("dish", "local", "Порция", DishType.Custom, "", listOf(serving), serving, 2.0, 1.5, false)
        val food = event(EventType.Bread(CalculatorFlow.BREAD_UNITS), value = 1.5).copy(dishes = listOf(dish))
        val state = model(listOf(food, event(EventType.Glucose(GlucoseInputType.MANUAL), value = 6.0)))
            .toStatisticsDashboardUiState(Period.SEVEN)
        assertEquals(120.0, state.nutrition.calories!!, 0.001)
        assertEquals(18.0, state.nutrition.carbohydrates!!, 0.001)
        assertEquals(1.5 / 7, state.nutrition.averageBreadUnits!!, 0.001)
        assertTrue(state.gmi.endsWith("%"))
    }

    private fun model(events: List<EventV2>) = buildStatisticModel(
        period, events, GlucoseLevelSettings(), GlucoseFormat.PLASMA,
        InsulinMedicamentStatistic(emptyList(), emptyList()), CalculatorFlow.BREAD_UNITS, ZoneOffset.UTC
    )

    private fun event(type: EventType, value: Double? = null, duration: Long? = null) = EventV2(
        id = type.toString(), additionTime = period.start.withHour(12).atZone(ZoneOffset.UTC),
        tagId = null, tag = null, note = null, modificationTime = null, value = value,
        name = null, kind = null, temperature = null, duration = duration, activityType = null,
        mealTag = null, glucoseInputType = null, insulinMedicament = null, medicament = null,
        tabletsNumber = null, type = type, state = State.CREATED, glucometerSerialNumber = null, dishes = emptyList()
    )
}
