package com.elta.android.presentation.features.devices.info.ui.adapter.holder

import com.elta.android.presentation.Clicks
import com.elta.android.presentation.core.bus.click
import com.elta.android.presentation.core.ui.adapter.BaseListItemViewHolder
import com.elta.android.presentation.databinding.ItemDevicePrimaryInfoBinding
import com.elta.android.presentation.features.devices.info.ui.adapter.items.DevicePrimaryInfoItem
import com.nullgr.core.rx.RxBus

class DevicePrimaryInfoViewHolder(
    private val binding: ItemDevicePrimaryInfoBinding,
    private val bus: RxBus
) : BaseListItemViewHolder<DevicePrimaryInfoItem>(binding.root) {
    init {
        binding.root.setOnClickListener {
            bus.click(Clicks.PrimaryDeviceItemClicked)
        }
    }

    override fun bind(item: DevicePrimaryInfoItem) {
        binding.titleFieldView.text = item.title
        binding.switchView.isChecked = item.isPrimary
        itemView.isClickable = !item.isPrimary
    }
}
