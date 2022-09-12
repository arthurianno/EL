package com.elta.android.presentation.features.main.records.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.core.ui.adapter.BaseListAdapter
import com.elta.android.presentation.databinding.ItemRecordBinding
import com.elta.android.presentation.databinding.ItemRecordsDailyGlucoseBinding
import com.elta.android.presentation.databinding.ItemRecordsGroupBinding
import com.elta.android.presentation.databinding.ItemRecordsHeaderBinding
import com.elta.android.presentation.features.main.records.ui.adapter.holder.ItemRecordViewHolder
import com.elta.android.presentation.features.main.records.ui.adapter.holder.ItemRecordsDailyGlucoseVieHolder
import com.elta.android.presentation.features.main.records.ui.adapter.holder.ItemRecordsGroupViewHolder
import com.elta.android.presentation.features.main.records.ui.adapter.holder.ItemRecordsHeaderViewHolder
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordItem
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordsDailyGlucoseItem
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordsGroupItem
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordsHeaderItem
import com.nullgr.core.rx.RxBus
import javax.inject.Inject

class MainRecordsAdapter @Inject constructor(
    private val viewPool: RecyclerView.RecycledViewPool,
    private val bus: RxBus
) : BaseListAdapter() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            RecordItem::class.hashCode() -> ItemRecordViewHolder(
                ItemRecordBinding.inflate(inflater, parent, false),
                bus
            )
            RecordsGroupItem::class.hashCode() -> ItemRecordsGroupViewHolder(
                ItemRecordsGroupBinding.inflate(inflater, parent, false),
                viewPool,
                RecordItemGroupAdapter(bus)
            )

            RecordsHeaderItem::class.hashCode() -> ItemRecordsHeaderViewHolder(
                ItemRecordsHeaderBinding.inflate(inflater, parent, false)
            )
            RecordsDailyGlucoseItem::class.hashCode() -> ItemRecordsDailyGlucoseVieHolder(
                ItemRecordsDailyGlucoseBinding.inflate(inflater, parent, false)
            )

            else -> {
                throw IllegalArgumentException("No delegate defined for ${viewType::class.simpleName}")
            }
        }
    }
}
