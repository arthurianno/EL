package com.elta.android.domain.statistic

import com.elta.android.domain.factory.EventTestFactory
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.InsulinType
import com.elta.android.domain.features.statistics.interactor.average
import com.elta.android.domain.features.statistics.interactor.checkMax
import com.elta.android.domain.features.statistics.interactor.checkMin
import com.elta.android.domain.features.statistics.interactor.isBasalInsulin
import com.elta.android.domain.features.statistics.interactor.isBolusInsulin
import com.elta.android.domain.features.statistics.interactor.isNotMixedInsulin
import com.elta.android.domain.features.statistics.interactor.percent
import org.junit.Test

class StatisticExtensionFunctionsTest {

    @Test
    fun doubleAverage_simpleDouble_correct() {
        val value = 4.0
        val count = 4
        val expected = 1.0
        val average = value.average(count)
        assert(average == expected)
    }

    @Test
    fun doubleAverage_doubleWithTail_correct() {
        val value = 4.00000000000000001
        val count = 4
        val expected = 1.0
        val average = value.average(count)
        assert(average == expected)
    }

    @Test
    fun longAverage_simpleLong_correct() {
        val value = 4L
        val count = 4
        val expected = 1L
        val average = value.average(count)
        assert(average == expected)
    }

    @Test
    fun intPercent_correct() {
        val value = 10
        val total = 100
        val expected = 10.0
        val percent = value.percent(total)
        assert(percent == expected)
    }

    @Test
    fun doubleCheckMax_receiverIsMax_receiver() {
        val value = 10.0
        val max = 1.0
        assert(value.checkMax(max) == value)
    }

    @Test
    fun doubleCheckMax_argumentIsMax_argument() {
        val value = 1.0
        val max = 10.0
        assert(value.checkMax(max) == max)
    }

    @Test
    fun doubleCheckMin_receiverIsMin_receiver() {
        val value = 1.0
        val min = 10.0
        assert(value.checkMin(min) == value)
    }

    @Test
    fun doubleCheckMin_argumentIsMin_argument() {
        val value = 10.0
        val min = 1.0
        assert(value.checkMin(min) == min)
    }

    @Test
    fun eventIsBolusInsulin_ultraShort_true() {
        val event = EventTestFactory.create(type = EventType.INSULIN, insulinType = InsulinType.ULTRASHORT)
        assert(event.isBolusInsulin())
    }

    @Test
    fun eventIsBolusInsulin_short_true() {
        val event = EventTestFactory.create(type = EventType.INSULIN, insulinType = InsulinType.SHORT)
        assert(event.isBolusInsulin())
    }

    @Test
    fun eventIsBolusInsulin_notInsulinEvent_false() {
        val event = EventTestFactory.create(type = EventType.ACTIVITY)
        assert(!event.isBolusInsulin())
    }

    @Test
    fun eventIsBasalInsulin_intermidiate_true() {
        val event = EventTestFactory.create(type = EventType.INSULIN, insulinType = InsulinType.INTERMIDIATE)
        assert(event.isBasalInsulin())
    }

    @Test
    fun eventIsBasalInsulin_ultraLong_true() {
        val event = EventTestFactory.create(type = EventType.INSULIN, insulinType = InsulinType.ULTRALONG)
        assert(event.isBasalInsulin())
    }

    @Test
    fun eventIsBasalInsulin_long_true() {
        val event = EventTestFactory.create(type = EventType.INSULIN, insulinType = InsulinType.LONG)
        assert(event.isBasalInsulin())
    }

    @Test
    fun eventIsBasalInsulin_notInsulinEvent_false() {
        val event = EventTestFactory.create(type = EventType.ACTIVITY)
        assert(!event.isBasalInsulin())
    }

    @Test
    fun eventIsNotMixedInsulin_mixed_false() {
        val event = EventTestFactory.create(type = EventType.INSULIN, insulinType = InsulinType.MIXED)
        assert(!event.isNotMixedInsulin())
    }

    @Test
    fun eventIsNotMixedInsulin_notMixed_true() {
        val event = EventTestFactory.create(type = EventType.INSULIN, insulinType = InsulinType.ULTRALONG)
        assert(event.isNotMixedInsulin())
    }

    @Test
    fun eventIsNotMixedInsulin_notInsulinEvent_true() {
        val event = EventTestFactory.create(type = EventType.ACTIVITY)
        assert(event.isNotMixedInsulin())
    }
}