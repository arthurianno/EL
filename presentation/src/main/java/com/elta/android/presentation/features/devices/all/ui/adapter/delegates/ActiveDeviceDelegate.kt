package com.elta.android.presentation.features.devices.all.ui.adapter.delegates

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.R
import com.elta.android.presentation.core.bus.click
import com.elta.android.presentation.core.ui.adapter.withAdapterPosition
import com.elta.android.presentation.databinding.ItemActiveDeviceBinding
import com.elta.android.presentation.features.devices.all.ui.adapter.items.ActiveDeviceItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate
import com.nullgr.core.adapter.ktx.ViewHolder
import com.nullgr.core.rx.RxBus

class ActiveDeviceDelegate(
    private val bus: RxBus
) : AdapterDelegate<ItemActiveDeviceBinding>(ItemActiveDeviceBinding::inflate) {

    override val itemType = ActiveDeviceDelegate::class.java
    override val layoutResource = R.layout.item_active_device

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder =
        super.onCreateViewHolder(parent).apply {
            with(this as ViewHolder) {
                itemView.setOnClickListener {
                    withAdapterPosition<ActiveDeviceItem> { _, item, _ ->
                        bus.click(Clicks.ActiveDeviceItemClicked(item))
                    }
                }
            }
        }

    override fun onBindViewHolder(
        items: List<ListItem>,
        position: Int,
        holder: RecyclerView.ViewHolder
    ) {
        val item = items[position] as ActiveDeviceItem
        with(binding) {
            deviceIconView.setImageResource(item.icon)
            deviceNameView.text = item.name
            deviceAddressView.text = item.address
        }
    }

    override fun onBindViewHolder(
        items: List<ListItem>,
        position: Int,
        holder: RecyclerView.ViewHolder,
        payload: Any
    ) {
        val item = items[position] as ActiveDeviceItem
        with(binding) {
            when (payload) {
                ActiveDeviceItem.Payload.NAME_CHANGED -> deviceNameView.text = item.name
                ActiveDeviceItem.Payload.ADDRESS_CHANGED ->
                    deviceAddressView.text =
                        item.address
                ActiveDeviceItem.Payload.IS_PRIMARY_CHANGED -> deviceIconView.setImageResource(
                    item.icon
                )
            }
        }
    }
}
