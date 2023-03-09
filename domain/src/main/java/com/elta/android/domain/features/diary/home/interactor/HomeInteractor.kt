package com.elta.android.domain.features.diary.home.interactor

import com.elta.android.domain.features.diary.events.model.Event
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.home.model.DayPeriod
import com.elta.android.domain.features.diary.home.model.DayPeriodHolder.day
import com.elta.android.domain.features.diary.home.model.DayPeriodHolder.morning
import com.elta.android.domain.features.diary.home.model.EventsBlock
import com.elta.android.domain.features.diary.home.model.GlucoseLevel
import com.elta.android.domain.features.diary.home.model.GlucoseLevelDirection
import com.elta.android.domain.features.diary.home.model.GlucoseLevelSettings
import com.elta.android.domain.features.diary.home.model.HomeModel
import com.elta.android.domain.features.diary.tags.model.Tag
import com.elta.android.domain.features.userinfo.model.UserInfo
import timber.log.Timber
import java.util.Date
import kotlin.math.abs

private const val DOUBLE_ZERO: Double = 0.0

fun buildHomeModel(
    events: List<Event>,
    tags: List<Tag>,
    settings: GlucoseLevelSettings,
    userInfo: UserInfo
): HomeModel {
    val sortedEvents = events.sortAndFilter()
    sortedEvents.forEach { event ->
        Timber.i("<<<<<<< GetHomeModelUseCase >>>>>>  Event: id = ${event.id} , additionTime = ${event.additionTime} , value = ${event.value} , type = ${event.type}, state = ${event.state}")
    }

    var lastBreadEvent: Event? = null
    var lastInsulinEvent: Event? = null
    var lastGlucoseEvent: Event? = null
    var preLastGlucoseEvent: Event? = null

    sortedEvents.forEach { event ->
        if (lastBreadEvent == null && event.type == EventType.BREAD) {
            lastBreadEvent = event
        } else if (lastInsulinEvent == null && event.type == EventType.INSULIN) {
            lastInsulinEvent = event
        } else if (lastGlucoseEvent == null && event.type == EventType.GLUCOSE) {
            lastGlucoseEvent = event
        } else if (preLastGlucoseEvent == null && event.type == EventType.GLUCOSE) {
            preLastGlucoseEvent = event
        }
    }

    return HomeModel(
        isFirstEntrance = userInfo.isFirstHomeEntrance,
        dayPeriod = getDayPeriod(Date().time),
        lastBreadEvent = lastBreadEvent,
        lastInsulinEvent = lastInsulinEvent,
        lastGlucoseEvent = lastGlucoseEvent,
        glucoseLevel = lastGlucoseEvent?.glucoseLevel(settings),
        glucoseLevelDirection = lastGlucoseEvent?.glucoseLevelDirection(preLastGlucoseEvent),
        glucoseLevelDifference = lastGlucoseEvent?.glucoseLevelDifference(preLastGlucoseEvent),
        eventsBlocks = getEventsBlocks(sortedEvents, tags),
        dailyGlucoseModel = buildDailyGlucoseModel(events, settings)
    )
}

fun List<Event>.sortAndFilter(): List<Event> = sortedByDescending { it.additionTime }
    .filter { it.type != EventType.GLYCATEDHEMOGLOBIN }

fun getDayPeriod(now: Long): DayPeriod =
    when (now) {
        in morning -> DayPeriod.MORNING
        in day -> DayPeriod.AFTERNOON
        else -> DayPeriod.EVENING
    }

fun Event.glucoseLevel(settings: GlucoseLevelSettings): GlucoseLevel =
    when (value.orZero()) {
        in settings.low -> GlucoseLevel.LOW
        in settings.normal -> GlucoseLevel.NORMAL
        else -> GlucoseLevel.HIGH
    }

fun Event.glucoseLevelDirection(preLastEvent: Event?): GlucoseLevelDirection? =
    when {
        preLastEvent == null -> null
        (this.value.orZero()) > (preLastEvent.value.orZero()) -> GlucoseLevelDirection.UP
        (this.value.orZero()) < (preLastEvent.value.orZero()) -> GlucoseLevelDirection.DOWN
        else -> GlucoseLevelDirection.STABLE
    }

fun Event.glucoseLevelDifference(preLastEvent: Event?): Double? {
    return preLastEvent?.let {
        abs(this.value?.minus(preLastEvent.value.orZero()).orZero())
    }
}

fun getEventsBlocks(events: List<Event>, tags: List<Tag>): List<EventsBlock> {
    if (events.isEmpty()) {
        return emptyList()
    }

    val nullTagId = "null_tag_id"
    val blocksMap = mutableMapOf<String, MutableList<Event>>()
    events.forEach { event ->
        val tagId = event.tagId ?: nullTagId
        var block = blocksMap[tagId]
        if (block == null) {
            block = mutableListOf()
            blocksMap[tagId] = block
        }
        if ((event.tagId ?: nullTagId) == tagId) {
            block.add(event)
        }
    }

    val blocks = mutableListOf<EventsBlock>()
    blocksMap.forEach { entry ->
        val tagId = entry.key
        val tag = tags.firstOrNull { it.id == tagId }
        val newBlock = EventsBlock(tag, entry.value)
        blocks.add(newBlock)
    }

    return blocks
}

private fun Double?.orZero(): Double = this ?: DOUBLE_ZERO
