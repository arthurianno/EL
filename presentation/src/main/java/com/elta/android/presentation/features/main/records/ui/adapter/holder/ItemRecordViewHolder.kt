package com.elta.android.presentation.features.main.records.ui.adapter.holder

import android.view.View
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.core.bus.click
import com.elta.android.presentation.core.ui.adapter.BaseListItemViewHolder
import com.elta.android.presentation.databinding.ItemRecordBinding
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordItem
import com.nullgr.core.rx.RxBus
import com.nullgr.core.ui.extensions.toggleView

class ItemRecordViewHolder(
    private val binding: ItemRecordBinding,
    private val bus: RxBus
) : BaseListItemViewHolder<RecordItem>(binding.root) {

    override fun bind(item: RecordItem) {
        with(binding) {
            recordIconView.setImageResource(item.icon)
            recordTitleView.text = item.title
            recordTypeView.text = item.type
            recordCountView.text = item.count
            recordDateView.text = item.date
            item.labelIcon?.let {
                recordLabelView.setImageResource(it)
                recordLabelView.visibility = View.VISIBLE
            } ?: recordRedDotView.toggleView(item.showLabel)
            root.setOnClickListener {
                bus.click(Clicks.RecordClicked(item))
            }
        }
    }
}
