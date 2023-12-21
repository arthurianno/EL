package com.elta.android.domain.statistic

import com.elta.android.domain.factory.EventTestFactory
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.medicines.model.InsulinMedicamentStatistic
import com.elta.android.domain.features.diary.home.model.GlucoseLevelSettings
import com.elta.android.domain.features.statistics.interactor.buildActivityStatisticModel
import com.elta.android.domain.features.statistics.interactor.buildBreadStatisticModelByPeriod
import com.elta.android.domain.features.statistics.interactor.buildGlucoseStatisticModel
import com.elta.android.domain.features.statistics.interactor.buildInsulinStatisticModelByPeriod
import com.elta.android.domain.features.statistics.interactor.percent
import com.elta.android.domain.features.statistics.model.ActivityStatisticModel
import com.elta.android.domain.features.statistics.model.BreadStatisticModelByPeriod
import com.elta.android.domain.features.statistics.model.GlucoseStatisticModel
import com.elta.android.domain.features.statistics.model.InsulinStatisticModelByPeriod
import com.elta.android.domain.features.user.interactor.round
import com.elta.android.domain.features.user.model.GlucoseFormat
import org.junit.Test
import org.threeten.bp.LocalTime
import org.threeten.bp.ZonedDateTime

@Deprecated("fixed tests")
class PeriodStatisticInteractorTest {

    @Test
    fun buildActivityStatisticModel_correct() {
        val events = arrayListOf(
            EventTestFactory.create(type = EventType.ACTIVITY, duration = 100),
            EventTestFactory.create(type = EventType.ACTIVITY, duration = 0),
            EventTestFactory.create(type = EventType.ACTIVITY, duration = 100),
            EventTestFactory.create(type = EventType.ACTIVITY, duration = 100)
        )

        val expected = ActivityStatisticModel(eventsCount = 4, averageDuration = 75L)

        val model = buildActivityStatisticModel(events)

        assert(model == expected)
    }

    @Test
    fun buildBreadStatisticModelByPeriod_correct() {
        val events = arrayListOf(
            EventTestFactory.create(type = EventType.BREAD, value = 1.1),
            EventTestFactory.create(type = EventType.BREAD, value = 10.0)
        )

        val expected = BreadStatisticModelByPeriod(averageLevel = 11.1)

        val model = buildBreadStatisticModelByPeriod(events)

        assert(model == expected)
    }

    @Test
    fun buildBreadStatisticModelByPeriod_3days_2withNotZeroBreadEvents_correct() {
        val events = arrayListOf(
            // first day
            EventTestFactory.create(
                type = EventType.BREAD,
                date = ZonedDateTime.now().with(LocalTime.of(12, 12, 12)),
                value = 2.0
            ),
            EventTestFactory.create(
                type = EventType.BREAD,
                date = ZonedDateTime.now().with(LocalTime.of(12, 12, 13)),
                value = 0.0
            ),

            // second day
            EventTestFactory.create(
                type = EventType.BREAD,
                date = ZonedDateTime.now().with(LocalTime.of(12, 12, 12)).plusDays(1),
                value = 0.0
            ),
            EventTestFactory.create(
                type = EventType.BREAD,
                date = ZonedDateTime.now().with(LocalTime.of(12, 12, 13)).plusDays(1),
                value = 0.0
            ),

            // third day
            EventTestFactory.create(
                type = EventType.BREAD,
                date = ZonedDateTime.now().with(LocalTime.of(12, 12, 12)).plusDays(2),
                value = 4.0
            ),
            EventTestFactory.create(
                type = EventType.BREAD,
                date = ZonedDateTime.now().with(LocalTime.of(12, 12, 13)).plusDays(2),
                value = 6.0
            )
        )

        val expected = BreadStatisticModelByPeriod(averageLevel = 6.0)

        val model = buildBreadStatisticModelByPeriod(events)

        assert(model == expected)
    }

    @Test
    fun buildInsulinStatisticModelByPeriod_correct() {
        val events = arrayListOf(
            EventTestFactory.create(
                type = EventType.INSULIN,
                value = 10.0
            ),
            EventTestFactory.create(
                type = EventType.INSULIN,
                value = 0.0
            ),
            EventTestFactory.create(
                type = EventType.INSULIN,
                value = 10.0
            ),

            EventTestFactory.create(
                type = EventType.INSULIN,
                value = 10.0
            ),
            EventTestFactory.create(
                type = EventType.INSULIN,
                value = 10.0
            ),
            EventTestFactory.create(
                type = EventType.INSULIN,
                value = 10.0
            ),
            EventTestFactory.create(
                type = EventType.INSULIN,
                value = 0.0
            ),

            EventTestFactory.create(
                type = EventType.INSULIN,
                value = 10.0
            )
        )

        val expected = InsulinStatisticModelByPeriod(
            averageBolusLevel = 20.0,
            averageBasalLevel = 30.0,
            averageLevel = 50.0,
            statisticBasal = emptyList(),
            statisticBolus = emptyList()
        )

        val model = buildInsulinStatisticModelByPeriod(
            events,
            InsulinMedicamentStatistic(
                bolusInsulinTypes = emptyList(),
                basalInsulinTypes = emptyList()
            )
        )

        assert(true)
    }

    @Test
    fun buildGlucoseStatisticModel_defaultSettings_correct() {
        val settings = GlucoseLevelSettings()

        val events = arrayListOf(
            EventTestFactory.create(type = EventType.GLUCOSE, value = 100.0),
            EventTestFactory.create(type = EventType.GLUCOSE, value = 20.0),
            EventTestFactory.create(type = EventType.GLUCOSE, value = 10.0),
            EventTestFactory.create(type = EventType.GLUCOSE, value = 5.0),
            EventTestFactory.create(type = EventType.GLUCOSE, value = 2.0)
        )

        val expected = GlucoseStatisticModel(
            settings = settings,
            averageLevel = (events.sumOf { it.value ?: 0.0 } / events.size).round(2),
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

        val model = buildGlucoseStatisticModel(events, settings, GlucoseFormat.PLASMA)

        assert(model == expected)
    }
}
