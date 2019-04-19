package com.elta.android.domain.statistic

import com.elta.android.domain.factory.EventTestFactory
import com.elta.android.domain.features.diary.events.model.Event
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.InsulinType
import com.elta.android.domain.features.diary.home.model.GlucoseLevelSettings
import com.elta.android.domain.features.statistics.interactor.buildStatisticModel
import com.elta.android.domain.features.statistics.model.ActivityStatisticModel
import com.elta.android.domain.features.statistics.model.BreadStatisticModelByPeriod
import com.elta.android.domain.features.statistics.model.GlucoseStatisticModel
import com.elta.android.domain.features.statistics.model.InsulinStatisticModelByPeriod
import com.elta.android.domain.features.statistics.model.Periods
import com.elta.android.domain.features.statistics.model.StatisticByPeriodModel
import com.elta.android.domain.features.statistics.model.daily.DailyStatisticModel
import java.util.Date

class BuildStatisticModelTest {

    fun buildStatisticModel_correct() {
        val period = Periods.SevenDays()

        val day = Date()

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
            dayWithMaxLevel = getExpectedDayWithMaxLevel(),
            dayWithMinLevel = getExpectedDayWithMinLevel(),
            allDays = getExpectedAllDays(),
            glucose = getExpectedGlucoseStatistic(),
            insulin = getExpectedInsulinStatistic(),
            bread = getExpectedBreadStatistic(),
            activity = getExpectedActivityStatistic()
        )

        val model = buildStatisticModel(period, allEvents, settings)

        assert(model == expected)
    }

    private fun getExpectedDayWithMaxLevel() : DailyStatisticModel {

    }

    private fun getExpectedDayWithMinLevel() : DailyStatisticModel {

    }

    private fun getExpectedAllDays(): Map<Date, DailyStatisticModel> {

    }

    private fun getExpectedGlucoseStatistic(): GlucoseStatisticModel {

    }

    private fun getExpectedInsulinStatistic(): InsulinStatisticModelByPeriod {

    }

    private fun getExpectedBreadStatistic(): BreadStatisticModelByPeriod {

    }

    private fun getExpectedActivityStatistic(): ActivityStatisticModel {

    }
}