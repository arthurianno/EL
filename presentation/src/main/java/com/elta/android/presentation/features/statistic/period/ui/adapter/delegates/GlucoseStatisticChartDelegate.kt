package com.elta.android.presentation.features.statistic.period.ui.adapter.delegates

import android.support.v7.widget.RecyclerView
import com.elta.android.presentation.R
import com.elta.android.presentation.features.statistic.period.ui.adapter.items.GlucoseStatisticChartItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate
import com.nullgr.core.adapter.ktx.ViewHolder
import com.nullgr.core.rx.RxBus
import kotlinx.android.synthetic.main.item_glucose_statistic_chart.*

class GlucoseStatisticChartDelegate(val bus: RxBus) : AdapterDelegate() {

    override val layoutResource: Int = R.layout.item_glucose_statistic_chart
    override val itemType: Any = GlucoseStatisticChartItem::class

    override fun onBindViewHolder(items: List<ListItem>, position: Int, holder: RecyclerView.ViewHolder) {
        val item = items[position] as GlucoseStatisticChartItem
        with(holder as ViewHolder) {
            periodDatesTitleView.text = item.datesTitle
            statisticsChartView.setChartModel(item.chartModel)
        }
    }
}