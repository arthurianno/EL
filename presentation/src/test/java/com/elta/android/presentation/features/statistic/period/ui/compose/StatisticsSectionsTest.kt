package com.elta.android.presentation.features.statistic.period.ui.compose

import com.elta.android.presentation.features.statistic.period.model.DEFAULT_VISIBLE_STATISTICS_BLOCKS
import com.elta.android.presentation.features.statistic.period.model.StatisticsBlock
import org.junit.Assert.assertEquals
import org.junit.Test

class StatisticsSectionsTest {
    @Test fun `default order matches reference with heatmap between period charts`() {
        assertEquals(listOf(StatisticsSection.DISTRIBUTION, StatisticsSection.DAILY, StatisticsSection.EPISODES,
            StatisticsSection.KEY_METRICS, StatisticsSection.COMPARISON), statisticsSections(DEFAULT_VISIBLE_STATISTICS_BLOCKS))
    }

    @Test fun `hiding heatmap retains both period charts`() {
        assertEquals(listOf(StatisticsSection.DISTRIBUTION, StatisticsSection.EPISODES),
            statisticsSections(listOf(StatisticsBlock.PERIOD)))
    }

    @Test fun `custom order and hidden period are respected`() {
        assertEquals(listOf(StatisticsSection.FOOD, StatisticsSection.DAILY, StatisticsSection.DISTRIBUTION, StatisticsSection.EPISODES),
            statisticsSections(listOf(StatisticsBlock.FOOD, StatisticsBlock.DAILY, StatisticsBlock.PERIOD)))
        assertEquals(listOf(StatisticsSection.DAILY, StatisticsSection.COMPARISON),
            statisticsSections(listOf(StatisticsBlock.DAILY, StatisticsBlock.COMPARISON)))
    }
}
