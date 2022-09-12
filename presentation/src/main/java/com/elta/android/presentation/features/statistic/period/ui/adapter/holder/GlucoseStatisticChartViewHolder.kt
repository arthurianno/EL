package com.elta.android.presentation.features.statistic.period.ui.adapter.holder

import com.elta.android.presentation.Clicks
import com.elta.android.presentation.core.bus.click
import com.elta.android.presentation.core.ui.adapter.BaseListItemViewHolder
import com.elta.android.presentation.databinding.ItemGlucoseStatisticChartBinding
import com.elta.android.presentation.features.statistic.period.ui.adapter.items.GlucoseStatisticChartItem
import com.elta.android.presentation.widgets.charts.statistics.listeners.OnStatisticsDateChangedListener
import com.nullgr.core.rx.RxBus
import org.threeten.bp.LocalDate

class GlucoseStatisticChartViewHolder(
    private val binding: ItemGlucoseStatisticChartBinding,
    private val bus: RxBus
) : BaseListItemViewHolder<GlucoseStatisticChartItem>(binding.root) {
    init {
        binding.statisticsChartView.setOnStatisticsDateChangedListener(
            object : OnStatisticsDateChangedListener {
                override fun onUnselectedAll() {
                    bus.click(Clicks.DateInStatisticsClicked(null))
                }

                override fun onDateChanged(date: LocalDate) {
                    bus.click(Clicks.DateInStatisticsClicked(date))
                }
            }
        )
    }

    override fun bind(item: GlucoseStatisticChartItem) {
        with(binding) {
            periodDatesTitleView.text = item.datesTitle
            statisticsChartView.setChartModel(item.chartModel)
        }
    }
}
