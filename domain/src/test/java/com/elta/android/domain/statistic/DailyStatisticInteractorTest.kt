package com.elta.android.domain.statistic

import com.elta.android.domain.factory.EventTestFactory
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.Insulin
import com.elta.android.domain.features.diary.events.model.InsulinType
import com.elta.android.domain.features.diary.home.interactor.buildDailyGlucoseModel
import com.elta.android.domain.features.diary.home.model.GlucoseLevelSettings
import com.elta.android.domain.features.statistics.interactor.buildDailyBreadStatisticModel
import com.elta.android.domain.features.statistics.interactor.buildDailyInsulinStatisticModel
import com.elta.android.domain.features.statistics.interactor.buildDailyStatisticModel
import com.elta.android.domain.features.statistics.interactor.percent
import com.elta.android.domain.features.statistics.model.ActivityStatisticModel
import com.elta.android.domain.features.statistics.model.GlucoseStatisticModel
import com.elta.android.domain.features.statistics.model.daily.DailyBreadStatisticModel
import com.elta.android.domain.features.statistics.model.daily.DailyInsulinStatisticModel
import com.elta.android.domain.features.statistics.model.daily.DailyStatisticModel
import com.elta.android.domain.features.user.interactor.round
import org.junit.Test
import org.threeten.bp.LocalDate

class DailyStatisticInteractorTest {

    @Test
    fun buildDailyBreadStatisticModel_correct() {
        val events = arrayListOf(
            EventTestFactory.create(type = EventType.BREAD, value = 10.0),
            EventTestFactory.create(type = EventType.BREAD, value = 0.0),
            EventTestFactory.create(type = EventType.BREAD, value = 10.0)
        )

        val expected = DailyBreadStatisticModel(totalLevel = 20.0)

        val model = buildDailyBreadStatisticModel(events)

        assert(model == expected)
    }

    @Test
    fun buildDailyInsulinStatisticModel_correct() {
        val events = arrayListOf(
            EventTestFactory.create(
                type = EventType.INSULIN,
                insulin = Insulin("", "", InsulinType.ULTRASHORT),
                value = 10.0
            ),
            EventTestFactory.create(
                type = EventType.INSULIN,
                insulin = Insulin("", "", InsulinType.ULTRASHORT),
                value = 0.0
            ),
            EventTestFactory.create(
                type = EventType.INSULIN,
                insulin = Insulin("", "", InsulinType.SHORT),
                value = 10.0
            ),

            EventTestFactory.create(
                type = EventType.INSULIN,
                insulin = Insulin("", "", InsulinType.INTERMIDIATE),
                value = 10.0
            ),
            EventTestFactory.create(
                type = EventType.INSULIN,
                insulin = Insulin("", "", InsulinType.LONG),
                value = 10.0
            ),
            EventTestFactory.create(
                type = EventType.INSULIN,
                insulin = Insulin("", "", InsulinType.ULTRALONG),
                value = 10.0
            ),
            EventTestFactory.create(
                type = EventType.INSULIN,
                insulin = Insulin("", "", InsulinType.ULTRALONG),
                value = 0.0
            ),

            EventTestFactory.create(
                type = EventType.INSULIN,
                insulin = Insulin("", "", InsulinType.MIXED),
                value = 10.0
            )
        )

        val expected = DailyInsulinStatisticModel(
            totalBolusLevel = 20.0,
            totalBasalLevel = 30.0,
            totalLevel = 50.0
        )

        val model = buildDailyInsulinStatisticModel(events)

        assert(model == expected)
    }

    @Test
    fun buildDailyStatisticModel_correct() {
        val day = LocalDate.now()

        val glucoseEvents = arrayListOf(
            EventTestFactory.create(type = EventType.GLUCOSE, value = 100.0),
            EventTestFactory.create(type = EventType.GLUCOSE, value = 20.0),
            EventTestFactory.create(type = EventType.GLUCOSE, value = 10.0),
            EventTestFactory.create(type = EventType.GLUCOSE, value = 5.0),
            EventTestFactory.create(type = EventType.GLUCOSE, value = 2.0)
        )

        val insulinEvents = arrayListOf(
            EventTestFactory.create(
                type = EventType.INSULIN,
                insulin = Insulin("", "", InsulinType.ULTRASHORT),
                value = 10.0
            ),
            EventTestFactory.create(
                type = EventType.INSULIN,
                insulin = Insulin("", "", InsulinType.ULTRASHORT),
                value = 0.0
            ),
            EventTestFactory.create(
                type = EventType.INSULIN,
                insulin = Insulin("", "", InsulinType.SHORT),
                value = 10.0
            ),

            EventTestFactory.create(
                type = EventType.INSULIN,
                insulin = Insulin("", "", InsulinType.INTERMIDIATE),
                value = 10.0
            ),
            EventTestFactory.create(
                type = EventType.INSULIN,
                insulin = Insulin("", "", InsulinType.LONG),
                value = 10.0
            ),
            EventTestFactory.create(
                type = EventType.INSULIN,
                insulin = Insulin("", "", InsulinType.ULTRALONG),
                value = 10.0
            ),
            EventTestFactory.create(
                type = EventType.INSULIN,
                insulin = Insulin("", "", InsulinType.ULTRALONG),
                value = 0.0
            ),

            EventTestFactory.create(
                type = EventType.INSULIN,
                insulin = Insulin("", "", InsulinType.MIXED),
                value = 10.0
            )
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

        val eventsPerDay = mapOf(
            EventType.GLUCOSE to glucoseEvents,
            EventType.INSULIN to insulinEvents,
            EventType.BREAD to breadEvents,
            EventType.ACTIVITY to activityEvents
        )

        val settings = GlucoseLevelSettings()

        val expected = DailyStatisticModel(
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

        val model = buildDailyStatisticModel(day, eventsPerDay, settings)

        assert(model == expected)
    }
}
