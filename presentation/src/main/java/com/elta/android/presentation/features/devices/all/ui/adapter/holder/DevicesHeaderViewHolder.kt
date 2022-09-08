package com.elta.android.presentation.features.devices.all.ui.adapter.holder

import com.elta.android.presentation.core.ui.adapter.BaseListItemViewHolder
import com.elta.android.presentation.databinding.ItemDevicesHeaderBinding
import com.elta.android.presentation.features.devices.all.ui.adapter.items.DevicesHeaderItem

class DevicesHeaderViewHolder(
    private val binding: ItemDevicesHeaderBinding
) : BaseListItemViewHolder<DevicesHeaderItem>(binding.root) {
    override fun bind(item: DevicesHeaderItem) {
        binding.root.text = item.title
    }
}
