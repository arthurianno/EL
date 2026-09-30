package com.elta.android.domain.statistic

import com.elta.android.domain.factory.EventTestFactory
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.medicines.model.InsulinMedicament
import com.elta.android.domain.features.diary.medicines.model.InsulinMedicamentStatistic
import com.elta.android.domain.features.diary.medicines.model.MedicamentInsulinType
import com.elta.android.domain.features.diary.medicines.model.SHORT
import com.elta.android.domain.features.statistics.interactor.buildInsulinStatisticModelByPeriod
import com.elta.android.domain.features.statistics.interactor.toEventsContainer
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime

class PeriodStatisticTimeZoneTest {

    private val userZone = ZoneId.of("Europe/Moscow")
    private val bolusType = MedicamentInsulinType(
        code = SHORT,
        id = 1,
        name = "Short"
    )
    private val bolusMedicament = InsulinMedicament(
        id = 1,
        name = "Humalog",
        insulinType = bolusType,
        deleted = false
    )
    private val insulinStatistic = InsulinMedicamentStatistic(
        bolusInsulinTypes = listOf(bolusType),
        basalInsulinTypes = emptyList()
    )

    @Test
    fun `insulin events crossing UTC midnight stay in one user day`() {
        val events = listOf(
            insulinEvent(value = 7.0, utcHour = 8, utcMinute = 26, utcDay = 25),
            insulinEvent(value = 4.0, utcHour = 7, utcMinute = 14, utcDay = 25),
            insulinEvent(value = 5.0, utcHour = 23, utcMinute = 59, utcDay = 24)
        )

        val statistic = buildInsulinStatisticModelByPeriod(
            insulinEventsPerPeriod = events,
            insulinMedicamentStatistic = insulinStatistic,
            zoneId = userZone
        )

        assertEquals(16.0, statistic.averageBolusLevel, 0.0)
        assertEquals(16.0, statistic.averageLevel, 0.0)
        assertEquals(
            setOf(LocalDate.of(2026, 9, 25)),
            events.toEventsContainer(userZone).byTypePerDay.keys
        )
    }

    private fun insulinEvent(
        value: Double,
        utcHour: Int,
        utcMinute: Int,
        utcDay: Int
    ) = EventTestFactory.create(
        type = EventType.Insulin,
        value = value,
        insulinMedicament = bolusMedicament,
        date = ZonedDateTime.of(
            2026,
            9,
            utcDay,
            utcHour,
            utcMinute,
            0,
            0,
            ZoneOffset.UTC
        )
    )
}
