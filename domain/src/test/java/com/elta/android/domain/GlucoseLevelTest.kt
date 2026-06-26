package com.elta.android.domain

import com.elta.android.domain.factory.EventTestFactory
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.GlucoseInputType
import com.elta.android.domain.features.diary.home.interactor.glucoseLevel
import com.elta.android.domain.features.diary.home.model.GlucoseLevel
import com.elta.android.domain.features.diary.home.model.GlucoseLevelSettings
import org.junit.Test

class GlucoseLevelTest {

    @Test
    fun glucoseLevel_0_DefaultSettings_Low() {
        val event = EventTestFactory.create(type = EventType.Glucose(GlucoseInputType.MANUAL), value = 0.0)
        val level = event.glucoseLevel(GlucoseLevelSettings())
        assert(level == GlucoseLevel.LOW)
    }

    @Test
    fun glucoseLevel_LowEnd_DefaultSettings_Low() {
        val event =
            EventTestFactory.create(type = EventType.Glucose(GlucoseInputType.MANUAL), value = GlucoseLevelSettings.LOW_END)
        val level = event.glucoseLevel(GlucoseLevelSettings())
        assert(level == GlucoseLevel.LOW)
    }

    @Test
    fun glucoseLevel_NormalStart_DefaultSettings_Normal() {
        val event = EventTestFactory.create(
            type = EventType.Glucose(GlucoseInputType.MANUAL),
            value = GlucoseLevelSettings.NORMAL_START
        )
        val level = event.glucoseLevel(GlucoseLevelSettings())
        assert(level == GlucoseLevel.NORMAL)
    }

    @Test
    fun glucoseLevel_NormalEnd_DefaultSettings_Normal() {
        val event = EventTestFactory.create(
            type = EventType.Glucose(GlucoseInputType.MANUAL),
            value = GlucoseLevelSettings.NORMAL_END
        )
        val level = event.glucoseLevel(GlucoseLevelSettings())
        assert(level == GlucoseLevel.NORMAL)
    }

    @Test
    fun glucoseLevel_HighStart_DefaultSettings_High() {
        val event = EventTestFactory.create(
            type = EventType.Glucose(GlucoseInputType.MANUAL),
            value = GlucoseLevelSettings.HIGH_START
        )
        val level = event.glucoseLevel(GlucoseLevelSettings())
        assert(level == GlucoseLevel.HIGH)
    }
}
