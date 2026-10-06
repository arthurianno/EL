package com.elta.android.presentation.features.main.records.mapper

import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.EventV2
import com.elta.android.domain.features.diary.events.model.GlucoseInputType
import com.elta.android.domain.features.diary.events.model.State
import com.elta.android.domain.features.diary.home.interactor.buildDailyGlucoseModel
import com.elta.android.domain.features.diary.home.model.CalculatorFlow
import com.elta.android.domain.features.diary.home.model.GlucoseLevelSettings
import com.elta.android.domain.features.user.model.GlucoseFormat
import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime

class DetailedChartEventsTest {
    @Test
    fun `food insulin and activity remain visible without glucose measurements`() {
        val events = listOf(
            event("food", EventType.Bread(CalculatorFlow.BREAD_UNITS), value = 2.5),
            event("insulin", EventType.Insulin, value = 4.0),
            event("activity", EventType.Activity, duration = 30L * 60L)
        )

        assertEquals(1, DetailedChartItemsBuilder.buildFoodEntries(emptyList(), events).size)
        assertEquals(1, DetailedChartItemsBuilder.buildInsulinEntries(emptyList(), events).size)
        val activity = DetailedChartItemsBuilder.buildActivityEntries(events).single()
        assertEquals(30L, activity.durationMins)
        assertEquals("15:30", activity.endTimeLabel)
    }

    @Test
    fun `activity duration is interpreted as seconds across midnight`() {
        val activity = event(
            "activity", EventType.Activity, duration = 30L * 60L,
            time = ZonedDateTime.of(2026, 10, 6, 23, 45, 0, 0, ZoneOffset.ofHours(3))
        )

        val entry = DetailedChartItemsBuilder.buildActivityEntries(listOf(activity)).single()

        assertEquals(LocalDate.of(2026, 10, 7), entry.endDate)
        assertEquals("00:15", entry.endTimeLabel)
        assertEquals(30L, entry.durationMins)
    }

    @Test
    fun `short activity stays visible and invalid duration is ignored`() {
        val short = event("short", EventType.Activity, duration = 30L)
        val invalid = event("invalid", EventType.Activity, duration = 13L * 60L * 60L)

        val entries = DetailedChartItemsBuilder.buildActivityEntries(listOf(short, invalid))

        assertEquals(1, entries.size)
        assertEquals(1L, entries.single().durationMins)
        assertEquals("15:00", entries.single().endTimeLabel)
    }

    @Test
    fun `activity duration near a glucose measurement uses minutes in point details`() {
        val activity = event("activity", EventType.Activity, duration = 30L * 60L)
        val glucose = event(
            "glucose", EventType.Glucose(GlucoseInputType.MANUAL), value = 7.0,
            time = ZonedDateTime.of(2026, 10, 6, 16, 0, 0, 0, ZoneOffset.ofHours(3))
        )
        val events = listOf(activity, glucose)
        val model = buildDailyGlucoseModel(events, GlucoseLevelSettings(), GlucoseFormat.PLASMA)

        val point = DetailedChartItemsBuilder.buildPoints(model, events).single()

        assertEquals("30 мин.", point.activityDuration)
    }

    private fun event(
        id: String,
        type: EventType,
        value: Double? = null,
        duration: Long? = null,
        time: ZonedDateTime = ZonedDateTime.of(2026, 10, 6, 15, 0, 0, 0, ZoneOffset.ofHours(3))
    ) = EventV2(
        id = id,
        additionTime = time,
        tagId = null,
        tag = null,
        note = null,
        modificationTime = null,
        value = value,
        name = null,
        kind = null,
        temperature = null,
        duration = duration,
        activityType = null,
        mealTag = null,
        glucoseInputType = null,
        insulinMedicament = null,
        medicament = null,
        tabletsNumber = null,
        type = type,
        state = State.CREATED,
        glucometerSerialNumber = null,
        dishes = emptyList()
    )
}
