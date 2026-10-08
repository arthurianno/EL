package com.elta.android.domain.features.diary.home.interactor

import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.EventV2
import com.elta.android.domain.features.diary.events.model.modifyValues
import com.elta.android.domain.features.diary.home.model.CalculatorFlow
import com.elta.android.domain.features.diary.home.model.DayPeriod
import com.elta.android.domain.features.diary.home.model.DayPeriodHolder.day
import com.elta.android.domain.features.diary.home.model.DayPeriodHolder.morning
import com.elta.android.domain.features.diary.home.model.EventsBlock
import com.elta.android.domain.features.diary.home.model.GlucoseLevel
import com.elta.android.domain.features.diary.home.model.GlucoseLevelDirection
import com.elta.android.domain.features.diary.home.model.GlucoseLevelSettings
import com.elta.android.domain.features.diary.home.model.HomeModel
import com.elta.android.domain.features.diary.tags.model.Tag
import com.elta.android.domain.features.user.model.GlucoseFormat
import com.elta.android.domain.features.userinfo.model.UserInfo
import java.util.Date
import kotlin.math.abs

private const val DOUBLE_ZERO: Double = 0.0

fun buildHomeModel(
    events: List<EventV2>,
    tags: List<Tag>,
    settings: GlucoseLevelSettings,
    userInfo: UserInfo,
    glucoseFormat: GlucoseFormat,
    calculatorFlow: CalculatorFlow
): HomeModel {
    val sortedEvents = events
        .modifyValues(glucoseFormat)
        .sortAndFilter()

    var lastBreadEvent: EventV2? = null
    var lastInsulinEvent: EventV2? = null
    var lastGlucoseEvent: EventV2? = null
    var preLastGlucoseEvent: EventV2? = null

    sortedEvents.forEach { event ->
        if (lastBreadEvent == null && event.type is EventType.Bread) {
            lastBreadEvent = event
        } else if (lastInsulinEvent == null && event.type is EventType.Insulin) {
            lastInsulinEvent = event
        } else if (lastGlucoseEvent == null && event.type is EventType.Glucose) {
            lastGlucoseEvent = event
        } else if (preLastGlucoseEvent == null && event.type is EventType.Glucose) {
            preLastGlucoseEvent = event
        }
    }

    return HomeModel(
        isFirstEntrance = userInfo.isFirstHomeEntrance ?: true,
        dayPeriod = getDayPeriod(Date().time),
        lastFoodEvent = lastBreadEvent,
        lastInsulinEvent = lastInsulinEvent,
        lastGlucoseEvent = lastGlucoseEvent,
        glucoseLevel = lastGlucoseEvent?.glucoseLevel(settings),
        glucoseLevelDirection = lastGlucoseEvent?.glucoseLevelDirection(preLastGlucoseEvent),
        glucoseLevelDifference = lastGlucoseEvent?.glucoseLevelDifference(preLastGlucoseEvent),
        eventsBlocks = getEventsBlocks(sortedEvents, tags, calculatorFlow),
        dailyGlucoseModel = buildDailyGlucoseModel(events, settings, glucoseFormat),
        glucoseFormat = glucoseFormat,
        calculatorFlow = calculatorFlow
    )
}

fun List<EventV2>.sortAndFilter(): List<EventV2> = sortedByDescending { it.additionTime }
    .filter { it.type != EventType.Glycatedhemoglobin }

fun getDayPeriod(now: Long): DayPeriod =
    when (now) {
        in morning -> DayPeriod.MORNING
        in day -> DayPeriod.AFTERNOON
        else -> DayPeriod.EVENING
    }

fun EventV2.glucoseLevel(settings: GlucoseLevelSettings): GlucoseLevel =
    when (value.orZero()) {
        in settings.low -> GlucoseLevel.LOW
        in settings.normal -> GlucoseLevel.NORMAL
        else -> GlucoseLevel.HIGH
    }

fun EventV2.glucoseLevelDirection(preLastEvent: EventV2?): GlucoseLevelDirection? =
    when {
        preLastEvent == null -> null
        (this.value.orZero()) > (preLastEvent.value.orZero()) -> GlucoseLevelDirection.UP
        (this.value.orZero()) < (preLastEvent.value.orZero()) -> GlucoseLevelDirection.DOWN
        else -> GlucoseLevelDirection.STABLE
    }

fun EventV2.glucoseLevelDifference(preLastEvent: EventV2?): Double? {
    return preLastEvent?.let {
        abs(this.value?.minus(preLastEvent.value.orZero()).orZero())
    }
}

fun getEventsBlocks(
    events: List<EventV2>,
    tags: List<Tag>,
    calculatorFlow: CalculatorFlow
): List<EventsBlock> {
    val tagsById = tags.associateBy(Tag::id)
    return events.groupBy(EventV2::tagId).map { (tagId, groupedEvents) ->
        EventsBlock(
            tag = tagId?.let(tagsById::get),
            events = groupedEvents,
            calculatorFlow = calculatorFlow
        )
    }
}

private fun Double?.orZero(): Double = this ?: DOUBLE_ZERO
