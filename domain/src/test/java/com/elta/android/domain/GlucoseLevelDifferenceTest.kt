package com.elta.android.domain

import com.elta.android.domain.factory.EventTestFactory
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.home.interactor.glucoseLevelDifference
import org.junit.Test

class GlucoseLevelDifferenceTest {

    @Test
    fun glucoseLevelDifference() {
        val last = EventTestFactory.create(EventType.GLUCOSE, value = 10.0)
        val preLast = EventTestFactory.create(EventType.GLUCOSE, value = 5.0)

        val difference = last.glucoseLevelDifference(preLast)

        assert(difference == 5.0)
    }
}