package com.elta.android.presentation.features.devices.info.ui.adapter.delegates

import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.R
import com.elta.android.presentation.databinding.ItemDeviceInfoBinding
import com.elta.android.presentation.features.devices.info.ui.adapter.items.DeviceInfoItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate

class DeviceInfoDelegate : AdapterDelegate<ItemDeviceInfoBinding>(ItemDeviceInfoBinding::inflate) {

    override val itemType = DeviceInfoItem::class
    override val layoutResource = R.layout.item_device_info

    override fun onBindViewHolder(
        items: List<ListItem>,
        position: Int,
        holder: RecyclerView.ViewHolder
    ) {
        val item = items[position] as DeviceInfoItem
        with(binding) {
            titleFieldView.text = item.title
            descriptionFieldView.text = item.description
        }
    }

    override fun onBindViewHolder(
        items: List<ListItem>,
        position: Int,
        holder: RecyclerView.ViewHolder,
        payload: Any
    ) {
        val item = items[position] as DeviceInfoItem
        when (payload) {
            DeviceInfoItem.Payload.DESCRIPTION_CHANGED ->
                binding.descriptionFieldView.text = item.description
        }
    }
}
