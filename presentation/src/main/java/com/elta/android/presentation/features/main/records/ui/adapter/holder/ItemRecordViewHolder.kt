package com.elta.android.presentation.features.main.records.ui.adapter.holder

import android.view.View
import androidx.core.view.isVisible
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.core.bus.click
import com.elta.android.presentation.core.ui.adapter.BaseListItemViewHolder
import com.elta.android.presentation.databinding.ItemRecordBinding
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordItem
import com.nullgr.core.rx.RxBus

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
            
            if (item.isInvalid) {
                recordDateView.setTextColor(androidx.core.content.ContextCompat.getColor(root.context, com.elta.android.presentation.R.color.red))
            } else {
                recordDateView.setTextColor(androidx.core.content.ContextCompat.getColor(root.context, com.elta.android.presentation.R.color.shade_black2))
            }

            if (item.isTemperatureInvalid) {
                recordInvalidBadgeView.isVisible = true
                recordInvalidBadgeView.setImageResource(com.elta.android.presentation.R.drawable.ic_thermometer_alert)
            } else {
                recordInvalidBadgeView.isVisible = false
            }

            if (item.labelIcon != null) {
                recordLabelView.setImageResource(item.labelIcon)
                recordLabelView.visibility = View.VISIBLE
            } else {
                recordLabelView.visibility = View.GONE
            }
            root.setOnClickListener {
                bus.click(Clicks.RecordClicked(item))
            }
        }
    }
}
