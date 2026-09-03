package com.elta.android.domain

import com.elta.android.domain.factory.EventTestFactory
import com.elta.android.domain.factory.TagTestFactory
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.GlucoseInputType
import com.elta.android.domain.features.diary.home.interactor.buildHomeModel
import com.elta.android.domain.features.diary.home.model.CalculatorFlow
import com.elta.android.domain.features.diary.home.model.GlucoseLevelDirection
import com.elta.android.domain.features.diary.home.model.GlucoseLevelSettings
import com.elta.android.domain.features.calculator.model.Dish
import com.elta.android.domain.features.calculator.model.DishType
import com.elta.android.domain.features.calculator.model.Serving
import com.elta.android.domain.features.user.model.GlucoseFormat
import com.elta.android.domain.features.userinfo.model.UserInfo
import org.junit.Assert.assertEquals
import org.junit.Test

class BuildHomeModelTest {

    @Test
    fun buildHomeModel_calculatesTotalsForAllInsulinAndCatalogFood() {
        val food = EventTestFactory.create(type = EventType.Bread(CalculatorFlow.PRODUCT_ONLY))
            .copy(dishes = listOf(testDish(breadUnits = 2.5)))
        val model = buildHomeModel(
            events = listOf(
                food,
                EventTestFactory.create(type = EventType.Insulin, value = 6.0),
                EventTestFactory.create(type = EventType.Insulin, value = 10.0)
            ),
            tags = emptyList(),
            settings = GlucoseLevelSettings(),
            userInfo = UserInfo(),
            glucoseFormat = GlucoseFormat.CAPILLARY,
            calculatorFlow = CalculatorFlow.PRODUCT_ONLY
        )

        assertEquals(2.5, model.breadUnitsTotal, 0.0)
        assertEquals(16.0, model.insulinTotal, 0.0)
    }

    @Test
    fun buildHomeModel_OneGlucoseEvent_HasGlucoseEvent() {
        val event = EventTestFactory.create(type = EventType.Glucose(GlucoseInputType.MANUAL))
        val model =
            buildHomeModel(
                listOf(event),
                emptyList(),
                GlucoseLevelSettings(),
                UserInfo(),
                GlucoseFormat.CAPILLARY,
                CalculatorFlow.BREAD_UNITS
            )

        assert(model.isFirstEntrance)
        assert(model.hasEvents)
        assert(model.lastFoodEvent == null)
        assert(model.lastGlucoseEvent != null)
        assert(model.lastInsulinEvent == null)
    }

    @Test
    fun buildHomeModel_OneGlucoseEvent_DirectionNull() {
        val event = EventTestFactory.create(type = EventType.Glucose(GlucoseInputType.MANUAL))
        val model =
            buildHomeModel(
                listOf(event),
                emptyList(),
                GlucoseLevelSettings(),
                UserInfo(),
                GlucoseFormat.CAPILLARY,
                CalculatorFlow.BREAD_UNITS
            )

        assert(model.isFirstEntrance)
        assert(model.hasEvents)
        assert(model.lastFoodEvent == null)
        assert(model.lastGlucoseEvent != null)
        assert(model.glucoseLevelDirection == null)
        assert(model.lastInsulinEvent == null)
    }

    @Test
    fun buildHomeModel_OneBreadEvent_HasBreadEvent() {
        val event = EventTestFactory.create(type = EventType.Bread(CalculatorFlow.BREAD_UNITS))
        val model =
            buildHomeModel(
                listOf(event),
                emptyList(),
                GlucoseLevelSettings(),
                UserInfo(),
                GlucoseFormat.CAPILLARY,
                CalculatorFlow.BREAD_UNITS
            )

        assert(model.isFirstEntrance)
        assert(model.hasEvents)
        assert(model.lastFoodEvent != null)
        assert(model.lastFoodEvent == event)
        assert(model.lastGlucoseEvent == null)
        assert(model.lastInsulinEvent == null)
    }

    @Test
    fun buildHomeModel_OneInsulinEvent_HasInsulinEvent() {
        val event = EventTestFactory.create(type = EventType.Insulin)
        val model =
            buildHomeModel(
                listOf(event),
                emptyList(),
                GlucoseLevelSettings(),
                UserInfo(),
                GlucoseFormat.CAPILLARY,
                CalculatorFlow.BREAD_UNITS
            )

        assert(model.isFirstEntrance)
        assert(model.hasEvents)
        assert(model.lastFoodEvent == null)
        assert(model.lastGlucoseEvent == null)
        assert(model.lastInsulinEvent != null)
        assert(model.lastInsulinEvent == event)
    }

    @Test
    fun buildHomeModel_ThreeEvents_HasAllLastEvents() {
        val event1 = EventTestFactory.create(type = EventType.Bread(CalculatorFlow.BREAD_UNITS))
        val event2 = EventTestFactory.create(type = EventType.Glucose(GlucoseInputType.MANUAL))
        val event3 = EventTestFactory.create(type = EventType.Insulin)
        val model = buildHomeModel(
            arrayListOf(event1, event2, event3),
            emptyList(),
            GlucoseLevelSettings(),
            UserInfo(),
            GlucoseFormat.CAPILLARY,
            CalculatorFlow.BREAD_UNITS
        )

        assert(model.isFirstEntrance)
        assert(model.hasEvents)

        assert(model.lastFoodEvent != null)
        assert(model.lastFoodEvent == event1)

        assert(model.lastGlucoseEvent != null)

        assert(model.lastInsulinEvent != null)
        assert(model.lastInsulinEvent == event3)
    }

    @Test
    fun buildHomeModel_TwoGlucoseEvents_DirectionUp() {
        val event1 = EventTestFactory.create(type = EventType.Glucose(GlucoseInputType.MANUAL), value = 4.0)
        Thread.sleep(50)
        val event2 = EventTestFactory.create(type = EventType.Glucose(GlucoseInputType.MANUAL), value = 5.0)
        val model = buildHomeModel(
            arrayListOf(event1, event2),
            emptyList(),
            GlucoseLevelSettings(),
            UserInfo(),
            GlucoseFormat.CAPILLARY,
            CalculatorFlow.BREAD_UNITS
        )

        assert(model.isFirstEntrance)
        assert(model.hasEvents)

        assert(model.lastFoodEvent == null)

        assert(model.lastGlucoseEvent != null)
        assert(model.glucoseLevelDirection == GlucoseLevelDirection.UP)

        assert(model.lastInsulinEvent == null)
    }

    @Test
    fun buildHomeModel_TwoGlucoseEvents_DirectionDown() {
        val event1 = EventTestFactory.create(type = EventType.Glucose(GlucoseInputType.MANUAL), value = 4.0)
        Thread.sleep(50)
        val event2 = EventTestFactory.create(type = EventType.Glucose(GlucoseInputType.MANUAL), value = 3.0)
        val model = buildHomeModel(
            arrayListOf(event1, event2),
            emptyList(),
            GlucoseLevelSettings(),
            UserInfo(),
            GlucoseFormat.CAPILLARY,
            CalculatorFlow.BREAD_UNITS
        )

        assert(model.isFirstEntrance)
        assert(model.hasEvents)

        assert(model.lastFoodEvent == null)

        assert(model.lastGlucoseEvent != null)
        assert(model.lastGlucoseEvent == event2)
        assert(model.glucoseLevelDirection == GlucoseLevelDirection.DOWN)

        assert(model.lastInsulinEvent == null)
    }

    @Test
    fun buildHomeModel_TwoGlucoseEvents_DirectionStable() {
        val event1 = EventTestFactory.create(type = EventType.Glucose(GlucoseInputType.MANUAL), value = 4.0)
        Thread.sleep(50)
        val event2 = EventTestFactory.create(type = EventType.Glucose(GlucoseInputType.MANUAL), value = 4.0)
        val model = buildHomeModel(
            arrayListOf(event1, event2),
            emptyList(),
            GlucoseLevelSettings(),
            UserInfo(),
            GlucoseFormat.CAPILLARY,
            CalculatorFlow.BREAD_UNITS
        )

        assert(model.isFirstEntrance)
        assert(model.hasEvents)

        assert(model.lastFoodEvent == null)

        assert(model.lastGlucoseEvent != null)
        assert(model.lastGlucoseEvent == event2)
        assert(model.glucoseLevelDirection == GlucoseLevelDirection.STABLE)

        assert(model.lastInsulinEvent == null)
    }

    @Test
    fun buildHomeModel_TwoEvents_OneTag_SortedCorrect() {
        val tagId1 = TagTestFactory.nextId
        val event1 = EventTestFactory.create(type = EventType.Glucose(GlucoseInputType.MANUAL), tagId = tagId1)
        val tag1 = TagTestFactory.create(tagId1)

        Thread.sleep(50)

        val event2 = EventTestFactory.create(type = EventType.Glucose(GlucoseInputType.MANUAL))
        val model = buildHomeModel(
            arrayListOf(event1, event2).shuffled(),
            arrayListOf(tag1),
            GlucoseLevelSettings(),
            UserInfo(),
            GlucoseFormat.CAPILLARY,
            CalculatorFlow.BREAD_UNITS
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

private fun testDish(breadUnits: Double) = Dish(
    id = "dish",
    localId = "local-dish",
    name = "Test dish",
    type = DishType.Generic,
    brandName = "",
    servings = emptyList(),
    servingSelect = Serving.empty(),
    servingAmount = 1.0,
    breadUnits = breadUnits,
    isVerified = false
)
