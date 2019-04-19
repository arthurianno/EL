package com.elta.android.domain.statistic

import com.elta.android.domain.factory.EventTestFactory
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.home.model.atTimeOfDay
import com.elta.android.domain.features.statistics.interactor.toEventsContainer
import com.nullgr.core.date.minusDay
import com.nullgr.core.date.plusAny
import com.nullgr.core.date.withoutTime
import org.junit.Test
import java.util.Date
import java.util.concurrent.TimeUnit

class ToEventsContainerTest {

    @Test
    fun splitByDate_2_days_2_eventsPerDay_correct() {
        val firstDay = Date().atTimeOfDay(12, 0, 0)
        val secondDay = firstDay.minusDay(2)
        val events = arrayListOf(
            EventTestFactory.create(type = EventType.ACTIVITY, date = firstDay.plusAny(TimeUnit.MINUTES, 2)),
            EventTestFactory.create(type = EventType.INSULIN, date = firstDay.plusAny(TimeUnit.MINUTES, 4)),
            EventTestFactory.create(type = EventType.BREAD, date = secondDay.plusAny(TimeUnit.MINUTES, 2)),
            EventTestFactory.create(type = EventType.GLUCOSE, date = secondDay.plusAny(TimeUnit.MINUTES, 4))
        )

        val container = events.toEventsContainer()
        val byDate = container.byDate

        val first = byDate[firstDay.withoutTime()]
        assert(first != null)
        assert(first?.size == 2)
        assert(first?.find { it.type == EventType.ACTIVITY } != null)
        assert(first?.find { it.type == EventType.INSULIN } != null)

        val second = byDate[secondDay.withoutTime()]
        assert(second != null)
        assert(second?.size == 2)
        assert(second?.find { it.type == EventType.BREAD } != null)
        assert(second?.find { it.type == EventType.GLUCOSE } != null)
    }

    @Test
    fun splitByType_2_types_2_eventsPerType_correct() {
        val firstType = EventType.ACTIVITY
        val secondType = EventType.INSULIN
        val events = arrayListOf(
            EventTestFactory.create(type = firstType),
            EventTestFactory.create(type = secondType),
            EventTestFactory.create(type = firstType),
            EventTestFactory.create(type = secondType)
        )

        val container = events.toEventsContainer()
        val byType = container.byType

        val first = byType[firstType]
        assert(first != null)
        assert(first?.size == 2)
        assert(first?.first()?.type == firstType)

        val second = byType[secondType]
        assert(second != null)
        assert(second?.size == 2)
        assert(second?.first()?.type == secondType)
    }

    @Test
    fun splitByTypePerDate_correct() {
        val firstDay = Date().atTimeOfDay(12, 0, 0)
        val secondDay = firstDay.minusDay(2)

        val firstType = EventType.ACTIVITY
        val secondType = EventType.BREAD

        val events = arrayListOf(
            EventTestFactory.create(type = firstType, date = firstDay.plusAny(TimeUnit.MINUTES, 2)),
            EventTestFactory.create(type = secondType, date = firstDay.plusAny(TimeUnit.MINUTES, 4)),
            EventTestFactory.create(type = firstType, date = secondDay.plusAny(TimeUnit.MINUTES, 2)),
            EventTestFactory.create(type = secondType, date = secondDay.plusAny(TimeUnit.MINUTES, 4))
        )

        val container = events.toEventsContainer()
        val byTypePerDay = container.byTypePerDay

        val first = byTypePerDay[firstDay.withoutTime()]
        assert(first != null)

        if (first != null) {
            val firstByFirstType = first[firstType]
            assert(firstByFirstType != null)
            assert(firstByFirstType?.size == 1)
            assert(firstByFirstType?.first()?.type == firstType)

            val firstBySecondType = first[secondType]
            assert(firstBySecondType != null)
            assert(firstBySecondType?.size == 1)
            assert(firstBySecondType?.first()?.type == secondType)
        }

        val second = byTypePerDay[secondDay.withoutTime()]
        assert(second != null)

        if (second != null) {
            val secondByFirstType = second[firstType]
            assert(secondByFirstType != null)
            assert(secondByFirstType?.size == 1)
            assert(secondByFirstType?.first()?.type == firstType)

            val secondBySecondType = second[secondType]
            assert(secondBySecondType != null)
            assert(secondBySecondType?.size == 1)
            assert(secondBySecondType?.first()?.type == secondType)
        }
    }
}