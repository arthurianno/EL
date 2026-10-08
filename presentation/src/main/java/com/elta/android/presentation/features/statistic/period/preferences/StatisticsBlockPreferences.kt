package com.elta.android.presentation.features.statistic.period.preferences

import android.content.Context
import com.elta.android.presentation.features.statistic.period.model.StatisticsBlock
import com.elta.android.presentation.features.statistic.period.model.DEFAULT_VISIBLE_STATISTICS_BLOCKS
import javax.inject.Inject

/** Stores the order of visible dashboard sections in app-private preferences. */
class StatisticsBlockPreferences @Inject constructor(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        "statistics_dashboard_blocks",
        Context.MODE_PRIVATE
    )

    fun read(): List<StatisticsBlock> = decode(preferences.getString(VISIBLE_BLOCKS_KEY, null))

    fun save(blocks: List<StatisticsBlock>) {
        preferences.edit().putString(VISIBLE_BLOCKS_KEY, blocks.joinToString(",") { it.name }).apply()
    }

    companion object {
        private const val VISIBLE_BLOCKS_KEY = "visible_blocks_order_v1"

        internal fun decode(value: String?): List<StatisticsBlock> {
            if (value == null) return DEFAULT_VISIBLE_STATISTICS_BLOCKS
            val decoded = value.split(',')
                .mapNotNull { name -> StatisticsBlock.entries.firstOrNull { it.name == name } }
                .distinct()
            if (decoded.isEmpty() && value.isNotEmpty()) return DEFAULT_VISIBLE_STATISTICS_BLOCKS
            return decoded
        }
    }
}
