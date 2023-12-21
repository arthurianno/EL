package com.elta.android.domain

import com.elta.android.common.utils.isSortedBy
import com.elta.android.domain.factory.EventTestFactory
import com.elta.android.domain.features.diary.events.model.EventV2
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.home.interactor.buildDailyGlucoseModel
import com.elta.android.domain.features.diary.home.model.CalculatorFlow
import com.elta.android.domain.features.diary.home.model.GlucoseLevelSettings
import com.elta.android.domain.features.user.model.GlucoseFormat
import org.junit.Test

class BuildDailyGlucoseModelText {

    @Test
    fun buildDailyGlucoseModel_OneNormalEvent_hasEvent() {
        val event = EventTestFactory.create(type = EventType.Glucose, value = 4.4)

        val model = buildDailyGlucoseModel(
            listOf(event),
            GlucoseLevelSettings(),
            GlucoseFormat.CAPILLARY
        )

        assert(model.hasEvents)
        assert(model.lastEvent != null)
    }

    @Test
    fun buildDailyGlucoseModel_fewNotValidEvents_empty() {
        val event = EventTestFactory.create(type = EventType.Glucose, value = null)
        val event2 = EventTestFactory.create(type = EventType.Bread(CalculatorFlow.BREAD_UNITS), value = 3.1)

        val model = buildDailyGlucoseModel(
            arrayListOf(event, event2),
            GlucoseLevelSettings(),
            GlucoseFormat.PLASMA
        )

        assert(!model.hasEvents)
        assert(model.lastEvent == null)
    }

    @Test
    fun buildDailyGlucoseModel_differentEvents_hasOnlyGlucose() {
        val events = arrayListOf<EventV2>()
        events.add(EventTestFactory.create(type = EventType.Glucose, value = 4.4))
        events.add(EventTestFactory.create(type = EventType.Bread(CalculatorFlow.BREAD_UNITS), value = 4.4))
        events.add(EventTestFactory.create(type = EventType.Insulin, value = 4.4))
        events.add(EventTestFactory.create(type = EventType.Glucose, value = 3.9))

        val model = buildDailyGlucoseModel(events, GlucoseLevelSettings(), GlucoseFormat.CAPILLARY)

        assert(model.hasEvents)
        assert(model.glucoseEvents.all { it.type == EventType.Glucose })
        assert(model.lastEvent != null)
    }

    @Test
    fun buildDailyGlucoseModel_noGlucoseEvents_isEmpty() {
        val model =
            buildDailyGlucoseModel(arrayListOf(), GlucoseLevelSettings(), GlucoseFormat.CAPILLARY)
        assert(!model.hasEvents)
        assert(model.lastEvent == null)
    }

    @Test
    fun buildDailyGlucoseModel_fewNormalEvents_noMinAndMax() {
        val events = arrayListOf<EventV2>()
        for (a in 4..9) {
            events.add(EventTestFactory.create(EventType.Glucose, value = a.toDouble()))
        }
        events.add(EventTestFactory.create(EventType.Glucose, value = 3.9))

        val model = buildDailyGlucoseModel(events, GlucoseLevelSettings(), GlucoseFormat.CAPILLARY)

        assert(model.hasEvents)
        assert(model.lastEvent != null)
        assert(model.maxEvent == null)
        assert(model.minEvent == null)
    }

    @Test
    fun buildDailyGlucoseModel_fewEventsInDifferentRanges_hasMinAndMax() {
        val events = arrayListOf<EventV2>()
        for (a in 4..9) {
            events.add(EventTestFactory.create(EventType.Glucose, value = a.toDouble()))
        }
        events.add(EventTestFactory.create(EventType.Glucose, value = 3.9))
        events.add(EventTestFactory.create(EventType.Glucose, value = 1.8))
        events.add(EventTestFactory.create(EventType.Glucose, value = 1.1))
        events.add(EventTestFactory.create(EventType.Glucose, value = 12.2))
        events.add(EventTestFactory.create(EventType.Glucose, value = 12.7))

        val model = buildDailyGlucoseModel(events, GlucoseLevelSettings(), GlucoseFormat.CAPILLARY)

        assert(model.hasEvents)
        assert(model.lastEvent != null)
        assert(model.maxEvent != null)
        assert(model.minEvent != null)
        assert(model.maxEvent?.value == 12.7)
        assert(model.minEvent?.value == 1.1)
    }

    @Test
    fun buildDailyGlucoseModel_fewEventsInDifferentRanges_correctOrder() {
        val events = arrayListOf<EventV2>()
        for (a in 4..9) {
            events.add(EventTestFactory.create(EventType.Glucose, value = a.toDouble()))
        }
        events.add(EventTestFactory.create(EventType.Glucose, value = 3.9))
        events.add(EventTestFactory.create(EventType.Glucose, value = 1.8))
        events.add(EventTestFactory.create(EventType.Glucose, value = 1.1))
        events.add(EventTestFactory.create(EventType.Glucose, value = 12.2))
        events.add(EventTestFactory.create(EventType.Glucose, value = 12.7))

        val model = buildDailyGlucoseModel(
            events.shuffled(),
            GlucoseLevelSettings(),
            GlucoseFormat.CAPILLARY
        )

        assert(model.glucoseEvents.isSortedBy { it.additionTime })
    }
}
