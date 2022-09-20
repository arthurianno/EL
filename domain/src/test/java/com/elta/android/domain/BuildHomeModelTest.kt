package com.elta.android.domain

import com.elta.android.domain.factory.EventTestFactory
import com.elta.android.domain.factory.TagTestFactory
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.home.interactor.buildHomeModel
import com.elta.android.domain.features.diary.home.model.GlucoseLevelDirection
import com.elta.android.domain.features.diary.home.model.GlucoseLevelSettings
import com.elta.android.domain.features.userinfo.model.UserInfo
import org.junit.Test

class BuildHomeModelTest {

    @Test
    fun buildHomeModel_OneGlucoseEvent_HasGlucoseEvent() {
        val event = EventTestFactory.create(type = EventType.GLUCOSE)
        val model =
            buildHomeModel(arrayListOf(event), emptyList(), GlucoseLevelSettings(), UserInfo())

        assert(model.isFirstEntrance)
        assert(model.hasEvents)
        assert(model.lastBreadEvent == null)
        assert(model.lastGlucoseEvent != null)
        assert(model.lastGlucoseEvent == event)
        assert(model.lastInsulinEvent == null)
    }

    @Test
    fun buildHomeModel_OneGlucoseEvent_DirectionUp() {
        val event = EventTestFactory.create(type = EventType.GLUCOSE)
        val model =
            buildHomeModel(arrayListOf(event), emptyList(), GlucoseLevelSettings(), UserInfo())

        assert(model.isFirstEntrance)
        assert(model.hasEvents)
        assert(model.lastBreadEvent == null)
        assert(model.lastGlucoseEvent != null)
        assert(model.lastGlucoseEvent == event)
        assert(model.glucoseLevelDirection == GlucoseLevelDirection.UP)
        assert(model.lastInsulinEvent == null)
    }

    @Test
    fun buildHomeModel_OneBreadEvent_HasBreadEvent() {
        val event = EventTestFactory.create(type = EventType.BREAD)
        val model =
            buildHomeModel(arrayListOf(event), emptyList(), GlucoseLevelSettings(), UserInfo())

        assert(model.isFirstEntrance)
        assert(model.hasEvents)
        assert(model.lastBreadEvent != null)
        assert(model.lastBreadEvent == event)
        assert(model.lastGlucoseEvent == null)
        assert(model.lastInsulinEvent == null)
    }

    @Test
    fun buildHomeModel_OneInsulinEvent_HasInsulinEvent() {
        val event = EventTestFactory.create(type = EventType.INSULIN)
        val model =
            buildHomeModel(arrayListOf(event), emptyList(), GlucoseLevelSettings(), UserInfo())

        assert(model.isFirstEntrance)
        assert(model.hasEvents)
        assert(model.lastBreadEvent == null)
        assert(model.lastGlucoseEvent == null)
        assert(model.lastInsulinEvent != null)
        assert(model.lastInsulinEvent == event)
    }

    @Test
    fun buildHomeModel_ThreeEvents_HasAllLastEvents() {
        val event1 = EventTestFactory.create(type = EventType.BREAD)
        val event2 = EventTestFactory.create(type = EventType.GLUCOSE)
        val event3 = EventTestFactory.create(type = EventType.INSULIN)
        val model = buildHomeModel(
            arrayListOf(event1, event2, event3),
            emptyList(),
            GlucoseLevelSettings(),
            UserInfo()
        )

        assert(model.isFirstEntrance)
        assert(model.hasEvents)

        assert(model.lastBreadEvent != null)
        assert(model.lastBreadEvent == event1)

        assert(model.lastGlucoseEvent != null)
        assert(model.lastGlucoseEvent == event2)

        assert(model.lastInsulinEvent != null)
        assert(model.lastInsulinEvent == event3)
    }

    @Test
    fun buildHomeModel_TwoGlucoseEvents_DirectionUp() {
        val event1 = EventTestFactory.create(type = EventType.GLUCOSE, value = 4.0)
        Thread.sleep(50)
        val event2 = EventTestFactory.create(type = EventType.GLUCOSE, value = 5.0)
        val model = buildHomeModel(
            arrayListOf(event1, event2),
            emptyList(),
            GlucoseLevelSettings(),
            UserInfo()
        )

        assert(model.isFirstEntrance)
        assert(model.hasEvents)

        assert(model.lastBreadEvent == null)

        assert(model.lastGlucoseEvent != null)
        assert(model.lastGlucoseEvent == event2)
        assert(model.glucoseLevelDirection == GlucoseLevelDirection.UP)

        assert(model.lastInsulinEvent == null)
    }

    @Test
    fun buildHomeModel_TwoGlucoseEvents_DirectionDown() {
        val event1 = EventTestFactory.create(type = EventType.GLUCOSE, value = 4.0)
        Thread.sleep(50)
        val event2 = EventTestFactory.create(type = EventType.GLUCOSE, value = 3.0)
        val model = buildHomeModel(
            arrayListOf(event1, event2),
            emptyList(),
            GlucoseLevelSettings(),
            UserInfo()
        )

        assert(model.isFirstEntrance)
        assert(model.hasEvents)

        assert(model.lastBreadEvent == null)

        assert(model.lastGlucoseEvent != null)
        assert(model.lastGlucoseEvent == event2)
        assert(model.glucoseLevelDirection == GlucoseLevelDirection.DOWN)

        assert(model.lastInsulinEvent == null)
    }

    @Test
    fun buildHomeModel_TwoGlucoseEvents_DirectionStable() {
        val event1 = EventTestFactory.create(type = EventType.GLUCOSE, value = 4.0)
        Thread.sleep(50)
        val event2 = EventTestFactory.create(type = EventType.GLUCOSE, value = 4.0)
        val model = buildHomeModel(
            arrayListOf(event1, event2),
            emptyList(),
            GlucoseLevelSettings(),
            UserInfo()
        )

        assert(model.isFirstEntrance)
        assert(model.hasEvents)

        assert(model.lastBreadEvent == null)

        assert(model.lastGlucoseEvent != null)
        assert(model.lastGlucoseEvent == event2)
        assert(model.glucoseLevelDirection == GlucoseLevelDirection.STABLE)

        assert(model.lastInsulinEvent == null)
    }

    @Test
    fun buildHomeModel_TwoEvents_OneTag_SortedCorrect() {
        val tagId1 = TagTestFactory.nextId
        val event1 = EventTestFactory.create(type = EventType.GLUCOSE, tagId = tagId1)
        val tag1 = TagTestFactory.create(tagId1)

        Thread.sleep(50)

        val event2 = EventTestFactory.create(type = EventType.GLUCOSE)
        val model = buildHomeModel(
            arrayListOf(event1, event2).shuffled(),
            arrayListOf(tag1),
            GlucoseLevelSettings(),
            UserInfo()
        )

        assert(model.isFirstEntrance)
        assert(model.hasEvents)
        assert(model.eventsBlocks.size == 2)

        assert(model.eventsBlocks[0].tag == null)
        assert(model.eventsBlocks[1].tag != null)
        assert(model.eventsBlocks[1].tag == tag1)

        assert(model.eventsBlocks[0].events[0].additionTime > model.eventsBlocks[1].events[0].additionTime)
    }
}
