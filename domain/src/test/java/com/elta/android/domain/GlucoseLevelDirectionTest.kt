package com.elta.android.domain

import com.elta.android.domain.factory.EventTestFactory
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.home.interactor.glucoseLevelDirection
import com.elta.android.domain.features.diary.home.model.GlucoseLevelDirection
import org.junit.Test

class GlucoseLevelDirectionTest {

    @Test
    fun glucoseLevelDirection_Last_4_PreLast_5_Down() {
        val last = EventTestFactory.create(type = EventType.GLUCOSE, value = 4.0)
        val preLast = EventTestFactory.create(type = EventType.GLUCOSE, value = 5.0)
        val direction = last.glucoseLevelDirection(preLast)
        assert(direction == GlucoseLevelDirection.DOWN)
    }

    @Test
    fun glucoseLevelDirection_Last_4_PreLast_4_Stable() {
        val last = EventTestFactory.create(type = EventType.GLUCOSE, value = 4.0)
        val preLast = EventTestFactory.create(type = EventType.GLUCOSE, value = 4.0)
        val direction = last.glucoseLevelDirection(preLast)
        assert(direction == GlucoseLevelDirection.STABLE)
    }

    @Test
    fun glucoseLevelDirection_Last_5_PreLast_Null_Null() {
        val last = EventTestFactory.create(type = EventType.GLUCOSE, value = 5.0)
        val direction = last.glucoseLevelDirection(null)
        assert(direction == null)
    }

    @Test
    fun glucoseLevelDirection_Last_5_PreLast_4_Up() {
        val last = EventTestFactory.create(type = EventType.GLUCOSE, value = 5.0)
        val preLast = EventTestFactory.create(type = EventType.GLUCOSE, value = 4.0)
        val direction = last.glucoseLevelDirection(preLast)
        assert(direction == GlucoseLevelDirection.UP)
    }
}
