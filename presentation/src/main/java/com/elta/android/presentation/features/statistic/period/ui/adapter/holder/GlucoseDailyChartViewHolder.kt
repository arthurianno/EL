package com.elta.android.presentation.features.statistic.period.ui.adapter.holder

import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.adapter.BaseListItemViewHolder
import com.elta.android.presentation.databinding.ItemGlucoseDailyChartBinding
import com.elta.android.presentation.features.statistic.period.ui.adapter.items.GlucoseDailyChartItem

class GlucoseDailyChartViewHolder(
    private val binding: ItemGlucoseDailyChartBinding
) : BaseListItemViewHolder<GlucoseDailyChartItem>(binding.root) {
    override fun bind(item: GlucoseDailyChartItem) {
        with(binding) {
            dailyGlucoseSubTitleView.text = root.context.getString(
                R.string.main_records_daily_glucose_subtitle,
                item.chartDataModel.chartItems.last().formattedTime
            )
            glucoseDailyView.setChartDataModel(item.chartDataModel)
        }
    }
}
