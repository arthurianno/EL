package com.elta.android.presentation.features.main.records.pm

import com.elta.android.domain.features.diary.events.model.EventV2
import org.threeten.bp.YearMonth

/** Keeps successful months, including empty ones, and ignores results from an invalidated load. */
internal class DashboardMonthCache {
    private var generation = 0L
    private val pending = mutableSetOf<YearMonth>()
    private var loaded = emptyMap<YearMonth, List<EventV2>>()

    fun start(month: YearMonth): Long? {
        if (month in loaded || !pending.add(month)) return null
        return generation
    }

    fun complete(month: YearMonth, requestGeneration: Long, events: List<EventV2>): Map<YearMonth, List<EventV2>>? {
        if (requestGeneration != generation) return null
        pending.remove(month)
        loaded = loaded + (month to events)
        return loaded
    }

    fun fail(month: YearMonth, requestGeneration: Long): Boolean {
        if (requestGeneration != generation) return false
        pending.remove(month)
        return true
    }

    fun clear(): Map<YearMonth, List<EventV2>> {
        generation++
        pending.clear()
        loaded = emptyMap()
        return loaded
    }
}
