package com.elta.android.presentation.features.statistic.period.ui.adapter.delegates

import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.R
import com.elta.android.presentation.databinding.ItemGlucoseDailyChartBinding
import com.elta.android.presentation.features.statistic.period.ui.adapter.items.GlucoseDailyChartItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate

class GlucoseDailyChartDelegate :
    AdapterDelegate<ItemGlucoseDailyChartBinding>(ItemGlucoseDailyChartBinding::inflate) {

    override val layoutResource: Int = R.layout.item_glucose_daily_chart
    override val itemType: Any = GlucoseDailyChartItem::class

    override fun onBindViewHolder(
        items: List<ListItem>,
        position: Int,
        holder: RecyclerView.ViewHolder
    ) {
        val item = items[position] as GlucoseDailyChartItem
        with(binding) {
            dailyGlucoseSubTitleView.text = item.dateTitle
            glucoseDailyView.setChartDataModel(item.chartDataModel)
        }
    }

    override fun onBindViewHolder(
        items: List<ListItem>,
        position: Int,
        holder: RecyclerView.ViewHolder,
        payload: Any
    ) {
        super.onBindViewHolder(items, position, holder, payload)
        val item = items[position] as GlucoseDailyChartItem
        with(binding) {
            when (payload) {
                GlucoseDailyChartItem.Payload.LAST_EVENT_CHANGED ->
                    dailyGlucoseSubTitleView.text = item.dateTitle
                GlucoseDailyChartItem.Payload.CHART_DATA_CHANGED ->
                    glucoseDailyView.setChartDataModel(item.chartDataModel)
                else -> {}
            }
        }
    }
}
