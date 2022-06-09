package com.elta.android.presentation.features.main.records.ui.adapter.delegates

import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.R
import com.elta.android.presentation.databinding.ItemRecordsDailyGlucoseBinding
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordsDailyGlucoseItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate

class RecordsDailyGlucoseDelegate :
    AdapterDelegate<ItemRecordsDailyGlucoseBinding>(ItemRecordsDailyGlucoseBinding::inflate) {

    override val layoutResource: Int = R.layout.item_records_daily_glucose
    override val itemType: Any = RecordsDailyGlucoseItem::class

    override fun onBindViewHolder(
        items: List<ListItem>,
        position: Int,
        holder: RecyclerView.ViewHolder
    ) {
        val item = items[position] as RecordsDailyGlucoseItem
        with(binding) {
            dailyRecordsSubTitleView.text = item.lastEventTimeTitle
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
        val item = items[position] as RecordsDailyGlucoseItem
        with(binding) {
            when (payload) {
                RecordsDailyGlucoseItem.Payload.LAST_EVENT_CHANGED ->
                    dailyRecordsSubTitleView.text = item.lastEventTimeTitle
                RecordsDailyGlucoseItem.Payload.CHART_DATA_CHANGED ->
                    glucoseDailyView.setChartDataModel(item.chartDataModel)
                else -> {}
            }
        }
    }
}
