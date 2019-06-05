package com.elta.android.domain.statistic

import com.elta.android.domain.factory.EventTestFactory
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.statistics.interactor.toEventsContainer
import org.junit.Test
import org.threeten.bp.LocalTime
import org.threeten.bp.ZonedDateTime

class ToEventsContainerTest {

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
        val firstDay = ZonedDateTime.now().with(LocalTime.of(12, 0, 0))
        val secondDay = firstDay.minusDays(2)

        val firstType = EventType.ACTIVITY
        val secondType = EventType.BREAD

        val events = arrayListOf(
            EventTestFactory.create(type = firstType, date = firstDay.plusMinutes(2)),
            EventTestFactory.create(type = secondType, date = firstDay.plusMinutes(4)),
            EventTestFactory.create(type = firstType, date = secondDay.plusMinutes(2)),
            EventTestFactory.create(type = secondType, date = secondDay.plusMinutes(4))
        )

        val container = events.toEventsContainer()
        val byTypePerDay = container.byTypePerDay

        val first = byTypePerDay[firstDay.toLocalDate()]
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

        val second = byTypePerDay[secondDay.toLocalDate()]
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