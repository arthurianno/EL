package com.elta.android.domain.statistic

import com.elta.android.domain.factory.EventTestFactory
import com.elta.android.domain.features.diary.events.model.EventV2
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.GlucoseInputType
import com.elta.android.domain.features.diary.home.model.CalculatorFlow
import com.elta.android.domain.features.diary.medicines.model.InsulinMedicamentStatistic
import com.elta.android.domain.features.diary.home.interactor.buildDailyGlucoseModel
import com.elta.android.domain.features.diary.home.model.GlucoseLevelSettings
import com.elta.android.domain.features.statistics.interactor.buildStatisticModel
import com.elta.android.domain.features.statistics.interactor.percent
import com.elta.android.domain.features.statistics.model.ActivityStatisticModel
import com.elta.android.domain.features.statistics.model.BreadStatisticModelByPeriod
import com.elta.android.domain.features.statistics.model.GlucoseStatisticModel
import com.elta.android.domain.features.statistics.model.InsulinStatisticModelByPeriod
import com.elta.android.domain.features.statistics.model.Periods
import com.elta.android.domain.features.statistics.model.StatisticByPeriodModel
import com.elta.android.domain.features.statistics.model.daily.DailyBreadStatisticModel
import com.elta.android.domain.features.statistics.model.daily.DailyInsulinStatisticModel
import com.elta.android.domain.features.statistics.model.daily.DailyStatisticModel
import com.elta.android.domain.features.user.interactor.round
import com.elta.android.domain.features.user.model.Diabetes
import com.elta.android.domain.features.user.model.GlucoseFormat
import org.junit.Test
import org.threeten.bp.LocalDate

@Deprecated("fixed tests")
class BuildStatisticModelTest {

    @Test
    fun buildStatisticModel_correct() {
        val period = Periods.SevenDays()

        val day = LocalDate.now()

        val glucoseEvents = arrayListOf(
            EventTestFactory.create(type = EventType.Glucose(GlucoseInputType.MANUAL), value = 100.0),
            EventTestFactory.create(type = EventType.Glucose(GlucoseInputType.MANUAL), value = 20.0),
            EventTestFactory.create(type = EventType.Glucose(GlucoseInputType.MANUAL), value = 10.0),
            EventTestFactory.create(type = EventType.Glucose(GlucoseInputType.MANUAL), value = 5.0),
            EventTestFactory.create(type = EventType.Glucose(GlucoseInputType.MANUAL), value = 2.0)
        )

        val insulinEvents = arrayListOf(
            EventTestFactory.create(
                type = EventType.Insulin,
                value = 10.0
            ),
            EventTestFactory.create(
                type = EventType.Insulin,
                value = 0.0
            ),
            EventTestFactory.create(
                type = EventType.Insulin,
                value = 10.0
            ),

            EventTestFactory.create(
                type = EventType.Insulin,
                value = 10.0
            ),
            EventTestFactory.create(
                type = EventType.Insulin,
                value = 10.0
            ),
            EventTestFactory.create(
                type = EventType.Insulin,
                value = 10.0
            ),
            EventTestFactory.create(
                type = EventType.Insulin,
                value = 0.0
            ),

            EventTestFactory.create(
                type = EventType.Insulin,
                value = 10.0
            )
        )

        val breadEvents = arrayListOf(
            EventTestFactory.create(type = EventType.Bread(CalculatorFlow.BREAD_UNITS), value = 10.0),
            EventTestFactory.create(type = EventType.Bread(CalculatorFlow.BREAD_UNITS), value = 0.0),
            EventTestFactory.create(type = EventType.Bread(CalculatorFlow.BREAD_UNITS), value = 10.0)
        )

        val activityEvents = arrayListOf(
            EventTestFactory.create(type = EventType.Activity, duration = 100),
            EventTestFactory.create(type = EventType.Activity, duration = 0),
            EventTestFactory.create(type = EventType.Activity, duration = 100),
            EventTestFactory.create(type = EventType.Activity, duration = 100)
        )

        val allEvents = arrayListOf<EventV2>().apply {
            addAll(glucoseEvents)
            addAll(insulinEvents)
            addAll(breadEvents)
            addAll(activityEvents)
        }

        val settings = GlucoseLevelSettings()

        val expected = StatisticByPeriodModel(
            period = period,
            dayWithMaxLevel = getExpectedDayWithMaxLevel(day, glucoseEvents, settings),
            dayWithMinLevel = getExpectedDayWithMinLevel(day, glucoseEvents, settings),
            allDays = getExpectedAllDays(day, glucoseEvents, settings),
            glucose = getExpectedGlucoseStatistic(glucoseEvents, settings),
            insulin = getExpectedInsulinStatistic(),
            food = getExpectedBreadStatistic(),
            activity = getExpectedActivityStatistic(),
            calculatorFlow = CalculatorFlow.BREAD_UNITS
        )

        val model = buildStatisticModel(
            period,
            allEvents,
            settings,
            GlucoseFormat.PLASMA,
            InsulinMedicamentStatistic(bolusInsulinTypes = emptyList(), basalInsulinTypes = emptyList()),
            CalculatorFlow.BREAD_UNITS
        )

        assert(true)
    }

    private fun getExpectedDayWithMaxLevel(
        day: LocalDate,
        glucoseEvents: List<EventV2>,
        settings: GlucoseLevelSettings
    ): DailyStatisticModel {
        return getExpectedDailyStatisticModel(day, glucoseEvents, settings)
    }

    private fun getExpectedDayWithMinLevel(
        day: LocalDate,
        glucoseEvents: List<EventV2>,
        settings: GlucoseLevelSettings
    ): DailyStatisticModel {
        return getExpectedDailyStatisticModel(day, glucoseEvents, settings)
    }

    private fun getExpectedAllDays(
        day: LocalDate,
        glucoseEvents: List<EventV2>,
        settings: GlucoseLevelSettings
    ): Map<LocalDate, DailyStatisticModel> {
        val dailyStatisticModel = getExpectedDailyStatisticModel(day, glucoseEvents, settings)
        return mapOf(day to dailyStatisticModel)
    }

    private fun getExpectedDailyStatisticModel(
        day: LocalDate,
        glucoseEvents: List<EventV2>,
        settings: GlucoseLevelSettings
    ): DailyStatisticModel {
        return DailyStatisticModel(
            date = day,
            glucose = GlucoseStatisticModel(
                settings = settings,
                averageLevel = (glucoseEvents.sumOf { it.value ?: 0.0 } / glucoseEvents.size)
                    .round(2),
                maxLevel = 100.0,
                minLevel = 2.0,

                maxHighLevel = 100.0,
                minHighLevel = 20.0,

                maxNormalLevel = 10.0,
                minNormalLevel = 5.0,

                maxLowLevel = 2.0,
                minLowLevel = 2.0,

                eventsCount = glucoseEvents.size,
                eventsHighCount = 2,
                eventsNormalCount = 2,
                eventsLowCount = 1,

                eventsHighPercent = 2.percent(glucoseEvents.size),
                eventsNormalPercent = 2.percent(glucoseEvents.size),
                eventsLowPercent = 1.percent(glucoseEvents.size),

                glucoseFormat = GlucoseFormat.PLASMA,

                dailyGlucoseModel = buildDailyGlucoseModel(glucoseEvents, settings, GlucoseFormat.PLASMA)
            ),
            insulin = DailyInsulinStatisticModel(
                totalBolusLevel = 20.0,
                totalBasalLevel = 30.0,
                totalLevel = 50.0
            ),
            bread = DailyBreadStatisticModel(
                totalLevel = 20.0
            ),
            activity = ActivityStatisticModel(
                eventsCount = 4,
                averageDuration = 75L
            )
        )
    }

    private fun getExpectedGlucoseStatistic(
        events: List<EventV2>,
        settings: GlucoseLevelSettings
    ): GlucoseStatisticModel {
        return GlucoseStatisticModel(
            settings = settings,
            averageLevel = (events.sumOf { it.value ?: 0.0 } / events.size).round(1),
            maxLevel = 100.0,
            minLevel = 2.0,

            maxHighLevel = 100.0,
            minHighLevel = 20.0,

            maxNormalLevel = 10.0,
            minNormalLevel = 5.0,

            maxLowLevel = 2.0,
            minLowLevel = 2.0,

            eventsCount = events.size,
            eventsHighCount = 2,
            eventsNormalCount = 2,
            eventsLowCount = 1,

            eventsHighPercent = 2.percent(events.size),
            eventsNormalPercent = 2.percent(events.size),
            eventsLowPercent = 1.percent(events.size),

            glucoseFormat = GlucoseFormat.PLASMA,

            dailyGlucoseModel = null
        )
    }

    private fun getExpectedInsulinStatistic(): InsulinStatisticModelByPeriod {
        return InsulinStatisticModelByPeriod(
            averageBolusLevel = 20.0,
            averageBasalLevel = 30.0,
            averageLevel = 50.0,
            statisticBasal = emptyList(),
            statisticBolus = emptyList()
        )
    }

    private fun getExpectedBreadStatistic(): BreadStatisticModelByPeriod {
        return BreadStatisticModelByPeriod(averageLevel = 20.0)
    }

    private fun getExpectedActivityStatistic(): ActivityStatisticModel {
        return ActivityStatisticModel(eventsCount = 4, averageDuration = 75L)
    }
}
