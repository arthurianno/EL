package com.elta.android.domain

import com.elta.android.domain.factory.EventTestFactory
import com.elta.android.domain.features.diary.events.model.Event
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.home.interactor.buildDailyGlucoseModel
import com.elta.android.domain.features.diary.home.model.GlucoseLevelSettings
import org.junit.Test

class BuildDailyGlucoseModelText {

    @Test
    fun buildDailyGlucoseModel_OneNormalEvent_hasEvent() {
        val event = EventTestFactory.create(type = EventType.GLUCOSE, value = 4.4)

        val model = buildDailyGlucoseModel(arrayListOf(event), GlucoseLevelSettings())

        assert(model.hasEvents)
        assert(model.lastEvent != null)
    }

    @Test
    fun buildDailyGlucoseModel_fewNotValidEvents_empty() {
        val event = EventTestFactory.create(type = EventType.GLUCOSE, value = null)
        val event2 = EventTestFactory.create(type = EventType.BREAD, value = 3.1)

        val model = buildDailyGlucoseModel(arrayListOf(event, event2), GlucoseLevelSettings())

        assert(!model.hasEvents)
        assert(model.lastEvent == null)
    }

    @Test
    fun buildDailyGlucoseModel_differentEvents_hasOnlyGlucose() {
        val events = arrayListOf<Event>()
        events.add(EventTestFactory.create(type = EventType.GLUCOSE, value = 4.4))
        events.add(EventTestFactory.create(type = EventType.BREAD, value = 4.4))
        events.add(EventTestFactory.create(type = EventType.INSULIN, value = 4.4))
        events.add(EventTestFactory.create(type = EventType.GLUCOSE, value = 3.9))

        val model = buildDailyGlucoseModel(events, GlucoseLevelSettings())

        assert(model.hasEvents)
        assert(model.glucoseEvents.all { it.type == EventType.GLUCOSE })
        assert(model.lastEvent != null)
    }

    @Test
    fun buildDailyGlucoseModel_noGlucoseEvents_isEmpty() {
        val model = buildDailyGlucoseModel(arrayListOf(), GlucoseLevelSettings())
        assert(!model.hasEvents)
        assert(model.lastEvent == null)
    }

    @Test
    fun buildDailyGlucoseModel_fewNormalEvents_noMinAndMax() {
        val events = arrayListOf<Event>()
        for (a in 4..9) {
            events.add(EventTestFactory.create(EventType.GLUCOSE, value = a.toDouble()))
        }
        events.add(EventTestFactory.create(EventType.GLUCOSE, value = 3.9))

        val model = buildDailyGlucoseModel(events, GlucoseLevelSettings())

        assert(model.hasEvents)
        assert(model.lastEvent != null)
        assert(model.maxEvent == null)
        assert(model.minEvent == null)
    }

    @Test
    fun buildDailyGlucoseModel_fewEventsInDifferentRanges_hasMinAndMax() {
        val events = arrayListOf<Event>()
        for (a in 4..9) {
            events.add(EventTestFactory.create(EventType.GLUCOSE, value = a.toDouble()))
        }
        events.add(EventTestFactory.create(EventType.GLUCOSE, value = 3.9))
        events.add(EventTestFactory.create(EventType.GLUCOSE, value = 1.8))
        events.add(EventTestFactory.create(EventType.GLUCOSE, value = 1.1))
        events.add(EventTestFactory.create(EventType.GLUCOSE, value = 12.2))
        events.add(EventTestFactory.create(EventType.GLUCOSE, value = 12.7))

        val model = buildDailyGlucoseModel(events, GlucoseLevelSettings())

        assert(model.hasEvents)
        assert(model.lastEvent != null)
        assert(model.maxEvent != null)
        assert(model.minEvent != null)
        assert(model.maxEvent?.value == 12.7)
        assert(model.minEvent?.value == 1.1)
    }

    @Test
    fun buildDailyGlucoseModel_fewEventsInDifferentRanges_correctOrder() {
        val events = arrayListOf<Event>()
        for (a in 4..9) {
            events.add(EventTestFactory.create(EventType.GLUCOSE, value = a.toDouble()))
        }
        events.add(EventTestFactory.create(EventType.GLUCOSE, value = 3.9))
        events.add(EventTestFactory.create(EventType.GLUCOSE, value = 1.8))
        events.add(EventTestFactory.create(EventType.GLUCOSE, value = 1.1))
        events.add(EventTestFactory.create(EventType.GLUCOSE, value = 12.2))
        events.add(EventTestFactory.create(EventType.GLUCOSE, value = 12.7))

        val model = buildDailyGlucoseModel(events.shuffled(), GlucoseLevelSettings())

        assert(model.glucoseEvents.isSortedBy { it.additionTime })
    }

    private inline fun <T, R : Comparable<R>> Iterable<T>.isSortedBy(crossinline selector: (T) -> R): Boolean {
        val iter = iterator()
        if (!iter.hasNext()) {
            return true
        }
        var t = iter.next()
        while (iter.hasNext()) {
            val t2 = iter.next()
            if (selector(t) > selector(t2)) {
                return false
            }
            t = t2
        }
        return true
    }
}