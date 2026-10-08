package com.elta.android.presentation.features.main.records.pm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.threeten.bp.YearMonth

class DashboardMonthCacheTest {
    private val month = YearMonth.of(2026, 10)

    @Test
    fun `failed month can be requested again`() {
        val cache = DashboardMonthCache()
        val generation = cache.start(month)!!

        assertNull(cache.start(month))
        cache.fail(month, generation)
        assertNotNull(cache.start(month))
    }

    @Test
    fun `successful empty month is cached`() {
        val cache = DashboardMonthCache()
        val generation = cache.start(month)!!

        assertEquals(emptyList<Any>(), cache.complete(month, generation, emptyList())?.get(month))
        assertNull(cache.start(month))
    }

    @Test
    fun `stale response after refresh is ignored`() {
        val cache = DashboardMonthCache()
        val oldGeneration = cache.start(month)!!
        cache.clear()

        assertNull(cache.complete(month, oldGeneration, emptyList()))
        assertNotNull(cache.start(month))
    }
}
