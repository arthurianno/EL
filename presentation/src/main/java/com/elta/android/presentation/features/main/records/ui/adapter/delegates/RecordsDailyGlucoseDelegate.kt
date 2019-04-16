package com.elta.android.presentation.features.main.records.ui.adapter.delegates

import android.support.v7.widget.RecyclerView
import com.elta.android.presentation.R
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordsDailyGlucoseItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate
import com.nullgr.core.adapter.ktx.ViewHolder
import kotlinx.android.synthetic.main.item_records_daily_glucose.*

class RecordsDailyGlucoseDelegate : AdapterDelegate() {

    override val layoutResource: Int = R.layout.item_records_daily_glucose
    override val itemType: Any = RecordsDailyGlucoseItem::class

    override fun onBindViewHolder(items: List<ListItem>, position: Int, holder: RecyclerView.ViewHolder) {
        val item = items[position] as RecordsDailyGlucoseItem
        with(holder as ViewHolder) {
            dailyRecordsSubTitleView.text = item.lastEventTimeTitle
            dailyGlucoseChartView.chartDataModel = item.chartDataModel
        }
    }

    override fun onBindViewHolder(items: List<ListItem>, position: Int, holder: RecyclerView.ViewHolder, payload: Any) {
        super.onBindViewHolder(items, position, holder, payload)
        val item = items[position] as RecordsDailyGlucoseItem
        with(holder as ViewHolder) {
            when (payload) {
                RecordsDailyGlucoseItem.Payload.LAST_EVENT_CHANGED ->
                    dailyRecordsSubTitleView.text = item.lastEventTimeTitle
                RecordsDailyGlucoseItem.Payload.CHART_DATA_CHANGED ->
                    dailyGlucoseChartView.chartDataModel = item.chartDataModel
            }
        }
    }
}