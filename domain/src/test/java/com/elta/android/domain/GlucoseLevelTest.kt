package com.elta.android.domain

import com.elta.android.domain.factory.EventTestFactory
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.home.interactor.glucoseLevel
import com.elta.android.domain.features.diary.home.model.GlucoseLevel
import com.elta.android.domain.features.diary.home.model.GlucoseLevelSettings
import org.junit.Test

class GlucoseLevelTest {

    @Test
    fun glucoseLevel_0_DefaultSettings_Low() {
        val event = EventTestFactory.create(type = EventType.GLUCOSE, value = 0.0)
        val level = event.glucoseLevel(GlucoseLevelSettings())
        assert(level == GlucoseLevel.LOW)
    }

    @Test
    fun glucoseLevel_3_99_DefaultSettings_Low() {
        val event = EventTestFactory.create(type = EventType.GLUCOSE, value = 3.99)
        val level = event.glucoseLevel(GlucoseLevelSettings())
        assert(level == GlucoseLevel.LOW)
    }

    @Test
    fun glucoseLevel_4_DefaultSettings_Normal() {
        val event = EventTestFactory.create(type = EventType.GLUCOSE, value = 4.0)
        val level = event.glucoseLevel(GlucoseLevelSettings())
        assert(level == GlucoseLevel.NORMAL)
    }

    @Test
    fun glucoseLevel_10_DefaultSettings_Normal() {
        val event = EventTestFactory.create(type = EventType.GLUCOSE, value = 10.0)
        val level = event.glucoseLevel(GlucoseLevelSettings())
        assert(level == GlucoseLevel.NORMAL)
    }

    @Test
    fun glucoseLevel_10_1_DefaultSettings_High() {
        val event = EventTestFactory.create(type = EventType.GLUCOSE, value = 10.1)
        val level = event.glucoseLevel(GlucoseLevelSettings())
        assert(level == GlucoseLevel.HIGH)
    }
}