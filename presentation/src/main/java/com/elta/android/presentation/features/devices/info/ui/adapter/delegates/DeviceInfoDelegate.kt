package com.elta.android.presentation.features.devices.info.ui.adapter.delegates

import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.R
import com.elta.android.presentation.features.devices.info.ui.adapter.items.DeviceInfoItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate
import com.nullgr.core.adapter.ktx.ViewHolder
import kotlinx.android.synthetic.main.item_device_info.*

class DeviceInfoDelegate : AdapterDelegate() {

    override val itemType = DeviceInfoItem::class
    override val layoutResource = R.layout.item_device_info

    override fun onBindViewHolder(
        items: List<ListItem>,
        position: Int,
        holder: RecyclerView.ViewHolder
    ) {
        val item = items[position] as DeviceInfoItem

        with(holder as ViewHolder) {
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

        with(holder as ViewHolder) {
            when (payload) {
                DeviceInfoItem.Payload.DESCRIPTION_CHANGED ->
                    descriptionFieldView.text =
                        item.description
            }
        }
    }
}
