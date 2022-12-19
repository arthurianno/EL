package com.elta.android.presentation.features.sync.connect.base.ui.adapter.holder

import com.elta.android.presentation.Clicks
import com.elta.android.presentation.core.bus.click
import com.elta.android.presentation.core.ui.adapter.BaseListItemViewHolder
import com.elta.android.presentation.databinding.ItemDeviceBinding
import com.elta.android.presentation.features.sync.connect.base.ui.adapter.items.DeviceItem
import com.nullgr.core.rx.RxBus
import com.nullgr.core.ui.extensions.toggleView

class DeviceItemViewHolder(
    val binding: ItemDeviceBinding,
    private val bus: RxBus
) : BaseListItemViewHolder<DeviceItem>(binding.root) {
    override fun bind(item: DeviceItem) {
        with(binding) {
            deviceNameView.text = item.name
            deviceAddressView.text = item.address
            deviceChooserView.toggleView(item.isSelected)
            dividerView.toggleView(!item.isLast)

            root.setOnClickListener {
                bus.click(Clicks.DeviceClicked(item))
            }
        }
    }
}
