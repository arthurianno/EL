package com.elta.android.domain.statistic

import com.elta.android.domain.factory.EventTestFactory
import com.elta.android.domain.features.diary.events.model.Event
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.InsulinType
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
import com.nullgr.core.date.withoutTime
import org.junit.Test
import java.util.Date

class BuildStatisticModelTest {

    @Test
    fun buildStatisticModel_correct() {
        val period = Periods.SevenDays()

        val day = Date().withoutTime()

        val glucoseEvents = arrayListOf(
            EventTestFactory.create(type = EventType.GLUCOSE, value = 100.0),
            EventTestFactory.create(type = EventType.GLUCOSE, value = 20.0),
            EventTestFactory.create(type = EventType.GLUCOSE, value = 10.0),
            EventTestFactory.create(type = EventType.GLUCOSE, value = 5.0),
            EventTestFactory.create(type = EventType.GLUCOSE, value = 2.0)
        )

        val insulinEvents = arrayListOf(
            EventTestFactory.create(type = EventType.INSULIN, insulinType = InsulinType.ULTRASHORT, value = 10.0),
            EventTestFactory.create(type = EventType.INSULIN, insulinType = InsulinType.ULTRASHORT, value = 0.0),
            EventTestFactory.create(type = EventType.INSULIN, insulinType = InsulinType.SHORT, value = 10.0),

            EventTestFactory.create(type = EventType.INSULIN, insulinType = InsulinType.INTERMIDIATE, value = 10.0),
            EventTestFactory.create(type = EventType.INSULIN, insulinType = InsulinType.LONG, value = 10.0),
            EventTestFactory.create(type = EventType.INSULIN, insulinType = InsulinType.ULTRALONG, value = 10.0),
            EventTestFactory.create(type = EventType.INSULIN, insulinType = InsulinType.ULTRALONG, value = 0.0),

            EventTestFactory.create(type = EventType.INSULIN, insulinType = InsulinType.MIXED, value = 10.0)
        )

        val breadEvents = arrayListOf(
            EventTestFactory.create(type = EventType.BREAD, value = 10.0),
            EventTestFactory.create(type = EventType.BREAD, value = 0.0),
            EventTestFactory.create(type = EventType.BREAD, value = 10.0)
        )

        val activityEvents = arrayListOf(
            EventTestFactory.create(type = EventType.ACTIVITY, duration = 100),
            EventTestFactory.create(type = EventType.ACTIVITY, duration = 0),
            EventTestFactory.create(type = EventType.ACTIVITY, duration = 100),
            EventTestFactory.create(type = EventType.ACTIVITY, duration = 100)
        )

        val allEvents = arrayListOf<Event>().apply {
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
            bread = getExpectedBreadStatistic(),
            activity = getExpectedActivityStatistic()
        )

        val model = buildStatisticModel(period, allEvents, settings)

        assert(model == expected)
    }

    private fun getExpectedDayWithMaxLevel(day: Date, glucoseEvents: List<Event>, settings: GlucoseLevelSettings): DailyStatisticModel {
        return getExpectedDailyStatisticModel(day, glucoseEvents, settings)
    }

    private fun getExpectedDayWithMinLevel(day: Date, glucoseEvents: List<Event>, settings: GlucoseLevelSettings): DailyStatisticModel {
        return getExpectedDailyStatisticModel(day, glucoseEvents, settings)
    }

    private fun getExpectedAllDays(day: Date, glucoseEvents: List<Event>, settings: GlucoseLevelSettings): Map<Date, DailyStatisticModel> {
        val dailyStatisticModel = getExpectedDailyStatisticModel(day, glucoseEvents, settings)
        return mapOf(day to dailyStatisticModel)
    }

    private fun getExpectedDailyStatisticModel(day: Date, glucoseEvents: List<Event>, settings: GlucoseLevelSettings): DailyStatisticModel {
        return DailyStatisticModel(
            date = day,
            glucose = GlucoseStatisticModel(
                settings = settings,
                averageLevel = (glucoseEvents.sumByDouble {
                    it.value ?: 0.0
                } / glucoseEvents.size).round(2),
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

                dailyGlucoseModel = buildDailyGlucoseModel(glucoseEvents, settings)
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

    private fun getExpectedGlucoseStatistic(events: List<Event>, settings: GlucoseLevelSettings): GlucoseStatisticModel {
        return GlucoseStatisticModel(
            settings = settings,
            averageLevel = (events.sumByDouble { it.value ?: 0.0 } / events.size).round(1),
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

            dailyGlucoseModel = null
        )
    }

    private fun getExpectedInsulinStatistic(): InsulinStatisticModelByPeriod {
        return InsulinStatisticModelByPeriod(
            averageBolusLevel = 10.0,
            averageBasalLevel = 10.0,
            averageLevel = 10.0
        )
    }

    private fun getExpectedBreadStatistic(): BreadStatisticModelByPeriod {
        return BreadStatisticModelByPeriod(averageLevel = 20.0)
    }

    private fun getExpectedActivityStatistic(): ActivityStatisticModel {
        return ActivityStatisticModel(eventsCount = 4, averageDuration = 75L)
    }
}