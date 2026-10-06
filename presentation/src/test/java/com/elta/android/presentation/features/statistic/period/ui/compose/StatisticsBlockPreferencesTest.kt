package com.elta.android.presentation.features.statistic.period.ui.compose

import org.junit.Assert.assertEquals
import org.junit.Test

class StatisticsBlockPreferencesTest {
    @Test
    fun `new installation uses default visible blocks`() {
        assertEquals(DEFAULT_VISIBLE_STATISTICS_BLOCKS, StatisticsBlockPreferences.decode(null))
    }

    @Test
    fun `saved order and visibility survive decoding`() {
        assertEquals(
            listOf(StatisticsBlock.DAILY, StatisticsBlock.PERIOD),
            StatisticsBlockPreferences.decode("DAILY,PERIOD")
        )
        assertEquals(emptyList<StatisticsBlock>(), StatisticsBlockPreferences.decode(""))
    }

    @Test
    fun `unknown and duplicate names are ignored`() {
        assertEquals(
            listOf(StatisticsBlock.FOOD, StatisticsBlock.DAILY),
            StatisticsBlockPreferences.decode("FOOD,UNKNOWN,FOOD,DAILY")
        )
        assertEquals(DEFAULT_VISIBLE_STATISTICS_BLOCKS, StatisticsBlockPreferences.decode("UNKNOWN"))
    }
}
