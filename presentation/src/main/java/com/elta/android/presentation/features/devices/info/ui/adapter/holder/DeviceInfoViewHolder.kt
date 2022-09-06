package com.elta.android.presentation.features.devices.info.ui.adapter.holder

import com.elta.android.presentation.core.ui.adapter.BaseListItemViewHolder
import com.elta.android.presentation.databinding.ItemDeviceInfoBinding
import com.elta.android.presentation.features.devices.info.ui.adapter.items.DeviceInfoItem

class DeviceInfoViewHolder(
    private val binding: ItemDeviceInfoBinding
) :
    BaseListItemViewHolder<DeviceInfoItem>(binding.root) {

    override fun bind(item: DeviceInfoItem) {
        with(binding) {
            titleFieldView.text = item.title
            descriptionFieldView.text = item.description
        }
        item.onClick?.let { onClick ->
            binding.root.setOnClickListener { onClick() }
        }
    }
}
