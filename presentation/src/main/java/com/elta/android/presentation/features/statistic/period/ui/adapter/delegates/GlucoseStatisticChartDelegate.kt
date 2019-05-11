package com.elta.android.presentation.features.statistic.period.ui.adapter.delegates

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.R
import com.elta.android.presentation.core.bus.click
import com.elta.android.presentation.features.statistic.period.ui.adapter.items.GlucoseStatisticChartItem
import com.elta.android.presentation.widgets.charts.statistics.listeners.OnStatisticsDateChangedListener
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate
import com.nullgr.core.adapter.ktx.ViewHolder
import com.nullgr.core.rx.RxBus
import kotlinx.android.synthetic.main.item_glucose_statistic_chart.*
import java.util.Date

class GlucoseStatisticChartDelegate(val bus: RxBus) : AdapterDelegate() {

    override val layoutResource: Int = R.layout.item_glucose_statistic_chart
    override val itemType: Any = GlucoseStatisticChartItem::class

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return super.onCreateViewHolder(parent).apply {
            with(this as ViewHolder) {
                statisticsChartView.setOnStatisticsDateChangedListener(
                    object : OnStatisticsDateChangedListener {
                        override fun onUnselectedAll() {
                            bus.click(Clicks.DateInStatisticsClicked(null))
                        }

                        override fun onDateChanged(date: Date) {
                            bus.click(Clicks.DateInStatisticsClicked(date))
                        }
                    }
                )
            }
        }
    }

    override fun onBindViewHolder(items: List<ListItem>, position: Int, holder: RecyclerView.ViewHolder) {
        val item = items[position] as GlucoseStatisticChartItem
        with(holder as ViewHolder) {
            periodDatesTitleView.text = item.datesTitle
            statisticsChartView.setChartModel(item.chartModel)
        }
    }

    override fun onBindViewHolder(items: List<ListItem>, position: Int, holder: RecyclerView.ViewHolder, payload: Any) {
        val item = items[position] as GlucoseStatisticChartItem
        with(holder as ViewHolder) {
            when (payload) {
                GlucoseStatisticChartItem.Payload.CHART_DATA_CHANGED ->
                    statisticsChartView.setChartModel(item.chartModel)
            }
        }
    }
}