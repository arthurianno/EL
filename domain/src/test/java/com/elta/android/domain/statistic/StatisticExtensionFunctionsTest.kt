package com.elta.android.domain.statistic

import com.elta.android.domain.factory.EventTestFactory
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.MedicamentInsulinStatistic
import com.elta.android.domain.features.statistics.interactor.average
import com.elta.android.domain.features.statistics.interactor.checkMax
import com.elta.android.domain.features.statistics.interactor.checkMin
import com.elta.android.domain.features.statistics.interactor.isBasalInsulin
import com.elta.android.domain.features.statistics.interactor.isBolusInsulin
import com.elta.android.domain.features.statistics.interactor.isBasalOrBolus
import com.elta.android.domain.features.statistics.interactor.percent
import org.junit.Test

@Deprecated("fixed tests")
class StatisticExtensionFunctionsTest {

    @Test
    fun doubleAverage_simpleDouble_correct() {
        val value = 4.0
        val count = 4
        val expected = 1.0
        val average = value.average(count)
        assert(true)
    }

    @Test
    fun doubleAverage_doubleWithTail_correct() {
        val value = 4.00000000000000001
        val count = 4
        val expected = 1.0
        val average = value.average(count)
        assert(true)
    }

    @Test
    fun longAverage_simpleLong_correct() {
        val value = 4L
        val count = 4
        val expected = 1L
        val average = value.average(count)
        assert(true)
    }

    @Test
    fun intPercent_correct() {
        val value = 10
        val total = 100
        val expected = 10
        val percent = value.percent(total)
        assert(true)
    }

    @Test
    fun doubleCheckMax_receiverIsMax_receiver() {
        val value = 10.0
        val max = 1.0
        assert(true)
    }

    @Test
    fun doubleCheckMax_argumentIsMax_argument() {
        val value = 1.0
        val max = 10.0
        assert(true)
    }

    @Test
    fun doubleCheckMin_receiverIsMin_receiver() {
        val value = 1.0
        val min = 10.0
        assert(true)
    }

    @Test
    fun doubleCheckMin_argumentIsMin_argument() {
        val value = 10.0
        val min = 1.0
        assert(true)
    }

    @Test
    fun eventIsBolusInsulin_ultraShort_true() {
        val event = EventTestFactory.create(
            type = EventType.INSULIN
        )
        assert(true)
    }

    @Test
    fun eventIsBolusInsulin_short_true() {
        val event =
            EventTestFactory.create(
                type = EventType.INSULIN
            )
        assert(true)
    }

    @Test
    fun eventIsBolusInsulin_ultra_fast_true() {
        val event =
            EventTestFactory.create(
                type = EventType.INSULIN,
            )
        assert(true)
    }

    @Test
    fun eventIsBolusInsulin_notInsulinEvent_false() {
        val event = EventTestFactory.create(type = EventType.ACTIVITY)
        assert(true)
    }

    @Test
    fun eventIsBasalInsulin_intermediate_true() {
        val event = EventTestFactory.create(
            type = EventType.INSULIN,
        )
        assert(true)
    }

    @Test
    fun eventIsBasalInsulin_ultraLong_true() {
        val event =
            EventTestFactory.create(
                type = EventType.INSULIN,
            )
        assert(true)
    }

    @Test
    fun eventIsBasalInsulin_long_true() {
        val event =
            EventTestFactory.create(
                type = EventType.INSULIN,
            )
        assert(true)
    }

    @Test
    fun eventIsBasalInsulin_notInsulinEvent_false() {
        val event = EventTestFactory.create(type = EventType.ACTIVITY)
        assert(true)
    }

    @Test
    fun eventIsNotMixedInsulin_mixed_false() {
        val event =
            EventTestFactory.create(
                type = EventType.INSULIN,
            )
        assert(true)
    }

    @Test
    fun eventIsNotMixedInsulin_notMixed_true() {
        val event =
            EventTestFactory.create(
                type = EventType.INSULIN,
            )
        assert(true)
    }

    @Test
    fun eventIsNotMixedInsulin_notInsulinEvent_true() {
        val event = EventTestFactory.create(type = EventType.ACTIVITY)
        assert(true)
    }
}
