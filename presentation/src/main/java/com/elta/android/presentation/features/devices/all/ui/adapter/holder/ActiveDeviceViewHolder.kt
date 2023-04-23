package com.elta.android.presentation.features.devices.all.ui.adapter.holder

import com.elta.android.presentation.Clicks
import com.elta.android.presentation.core.bus.click
import com.elta.android.presentation.core.ui.adapter.BaseListItemViewHolder
import com.elta.android.presentation.databinding.ItemActiveDeviceBinding
import com.elta.android.presentation.features.devices.all.ui.adapter.items.ActiveDeviceItem
import com.nullgr.core.rx.RxBus

class ActiveDeviceViewHolder(
    private val binding: ItemActiveDeviceBinding,
    private val bus: RxBus
) : BaseListItemViewHolder<ActiveDeviceItem>(binding.root) {

    override fun bind(item: ActiveDeviceItem) {
        with(binding) {
            deviceIconView.setImageResource(item.icon)
            deviceNameView.text = item.name
            deviceAddressView.text = item.serial
            root.setOnClickListener {
                bus.click(Clicks.ActiveDeviceItemClicked(item))
            }
        }
    }
}
