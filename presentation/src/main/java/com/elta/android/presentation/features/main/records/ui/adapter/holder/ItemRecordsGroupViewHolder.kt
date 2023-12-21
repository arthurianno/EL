package com.elta.android.presentation.features.main.records.ui.adapter.holder

import com.elta.android.presentation.Clicks
import com.elta.android.presentation.core.bus.click
import com.elta.android.presentation.core.ui.adapter.BaseListItemViewHolder
import com.elta.android.presentation.databinding.ItemRecordsGroupBinding
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordsGroupItem
import com.nullgr.core.rx.RxBus

class ItemRecordsGroupViewHolder(
    private val binding: ItemRecordsGroupBinding,
    private val bus: RxBus? = null
) : BaseListItemViewHolder<RecordsGroupItem>(binding.root) {


    override fun bind(item: RecordsGroupItem) {
        with(binding) {
            groupIconView.setImageResource(item.icon)
            groupNameView.text = item.name
            groupStateView.isSelected = item.isExpanded
            groupStateView.setOnClickListener {
                bus?.click(Clicks.ExpandCollapse(item))
            }
        }
    }
}
