package com.elta.android.presentation.features.main.records.ui.adapter.holder

import com.elta.android.presentation.core.ui.adapter.BaseListItemViewHolder
import com.elta.android.presentation.databinding.ItemRecordsDailyGlucoseBinding
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordsDailyGlucoseItem

class ItemRecordsDailyGlucoseVieHolder(private val binding: ItemRecordsDailyGlucoseBinding) :
    BaseListItemViewHolder<RecordsDailyGlucoseItem>(binding.root) {

    override fun bind(item: RecordsDailyGlucoseItem) {
        with(binding) {
            dailyRecordsSubTitleView.text = item.lastEventTimeTitle
            glucoseDailyView.setChartDataModel(item.chartDataModel)
        }
    }
}
