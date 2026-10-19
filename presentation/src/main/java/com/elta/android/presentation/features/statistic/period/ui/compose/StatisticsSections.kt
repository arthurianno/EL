package com.elta.android.presentation.features.statistic.period.ui.compose

import com.elta.android.presentation.features.statistic.period.model.StatisticsBlock

internal enum class StatisticsSection {
    DISTRIBUTION, DAILY, EPISODES, KEY_METRICS, COMPARISON, ACTIVITY, FOOD
}

/** The two period charts surround the daily heatmap in the reference layout. */
internal fun statisticsSections(blocks: List<StatisticsBlock>): List<StatisticsSection> = buildList {
    var deferredEpisodes = false
    blocks.distinct().forEachIndexed { index, block ->
        when (block) {
            StatisticsBlock.PERIOD -> {
                add(StatisticsSection.DISTRIBUTION)
                if (blocks.distinct().getOrNull(index + 1) == StatisticsBlock.DAILY) deferredEpisodes = true
                else add(StatisticsSection.EPISODES)
            }
            StatisticsBlock.DAILY -> {
                add(StatisticsSection.DAILY)
                if (deferredEpisodes) {
                    add(StatisticsSection.EPISODES)
                    deferredEpisodes = false
                }
            }
            StatisticsBlock.KEY_METRICS -> add(StatisticsSection.KEY_METRICS)
            StatisticsBlock.COMPARISON -> add(StatisticsSection.COMPARISON)
            StatisticsBlock.ACTIVITY -> add(StatisticsSection.ACTIVITY)
            StatisticsBlock.FOOD -> add(StatisticsSection.FOOD)
        }
    }
}
