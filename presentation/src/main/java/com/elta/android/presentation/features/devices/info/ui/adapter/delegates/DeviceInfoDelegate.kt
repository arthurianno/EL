package com.elta.android.presentation.features.devices.info.ui.adapter.delegates

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.*
import com.elta.android.presentation.R
import com.elta.android.presentation.features.devices.info.ui.adapter.items.DeviceInfoItem
import com.elta.android.presentation.features.sync.connect.ui.adapter.items.DeviceItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate
import com.nullgr.core.ui.extensions.toggleView
import kotlinx.android.synthetic.main.item_device_info.*

class DeviceInfoDelegate : AdapterDelegate() {

    override val itemType = DeviceInfoItem::class
    override val layoutResource = R.layout.item_device_info

    override fun onBindViewHolder(items: List<ListItem>, position: Int, holder: ViewHolder) {
        val item = items[position] as DeviceInfoItem

        with(holder as com.nullgr.core.adapter.ktx.ViewHolder) {
            titleFieldView.text = item.title
            descriptionFieldView.text = item.description
            dividerView.toggleView(!item.isTheLast)
        }
    }

    override fun onBindViewHolder(items: List<ListItem>, position: Int, holder: RecyclerView.ViewHolder, payload: Any) {
        super.onBindViewHolder(items, position, holder, payload)
        val item = items[position] as DeviceItem

        with(holder as com.nullgr.core.adapter.ktx.ViewHolder) {
            when (payload) {
                DeviceInfoItem.Payload.POSITION_CHANGED -> dividerView.toggleView(!item.isTheLast)
            }
        }
    }
}