package com.elta.android.presentation.features.statistic.period.ui.adapter.delegates

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.R
import com.elta.android.presentation.core.bus.click
import com.elta.android.presentation.databinding.ItemGlucoseStatisticChartBinding
import com.elta.android.presentation.features.statistic.period.ui.adapter.items.GlucoseStatisticChartItem
import com.elta.android.presentation.widgets.charts.statistics.listeners.OnStatisticsDateChangedListener
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate
import com.nullgr.core.rx.RxBus
import org.threeten.bp.LocalDate

class GlucoseStatisticChartDelegate(val bus: RxBus) :
    AdapterDelegate<ItemGlucoseStatisticChartBinding>(ItemGlucoseStatisticChartBinding::inflate) {

    override val layoutResource: Int = R.layout.item_glucose_statistic_chart
    override val itemType: Any = GlucoseStatisticChartItem::class

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return super.onCreateViewHolder(parent).apply {
            with(binding) {
                statisticsChartView.setOnStatisticsDateChangedListener(
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
        }
    }

    override fun onBindViewHolder(
        items: List<ListItem>,
        position: Int,
        holder: RecyclerView.ViewHolder
    ) {
        val item = items[position] as GlucoseStatisticChartItem
        with(binding) {
            periodDatesTitleView.text = item.datesTitle
            statisticsChartView.setChartModel(item.chartModel)
        }
    }

    override fun onBindViewHolder(
        items: List<ListItem>,
        position: Int,
        holder: RecyclerView.ViewHolder,
        payload: Any
    ) {
        val item = items[position] as GlucoseStatisticChartItem
        with(binding) {
            when (payload) {
                GlucoseStatisticChartItem.Payload.CHART_DATA_CHANGED ->
                    statisticsChartView.setChartModel(item.chartModel)
            }
        }
    }
}
