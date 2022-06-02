package com.elta.android.presentation.features.devices.info.ui.adapter.delegates

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.R
import com.elta.android.presentation.core.bus.click
import com.elta.android.presentation.core.ui.adapter.withAdapterPosition
import com.elta.android.presentation.features.devices.info.ui.adapter.items.DevicePrimaryInfoItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate
import com.nullgr.core.adapter.ktx.ViewHolder
import com.nullgr.core.rx.RxBus
import kotlinx.android.synthetic.main.item_device_primary_info.*

class DevicePrimaryInfoDelegate(
    private val bus: RxBus
) : AdapterDelegate() {

    override val itemType = DevicePrimaryInfoItem::class
    override val layoutResource = R.layout.item_device_primary_info

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder =
        super.onCreateViewHolder(parent).apply {
            with(this as ViewHolder) {
                itemView.setOnClickListener {
                    withAdapterPosition<DevicePrimaryInfoItem> { _, _, _ ->
                        bus.click(Clicks.PrimaryDeviceItemClicked)
                    }
                }
            }
        }

    override fun onBindViewHolder(
        items: List<ListItem>,
        position: Int,
        holder: RecyclerView.ViewHolder
    ) {
        val item = items[position] as DevicePrimaryInfoItem

        with(holder as ViewHolder) {
            titleFieldView.text = item.title
            setPrimaryState(item)
        }
    }

    override fun onBindViewHolder(
        items: List<ListItem>,
        position: Int,
        holder: RecyclerView.ViewHolder,
        payload: Any
    ) {
        val item = items[position] as DevicePrimaryInfoItem
        with(holder as ViewHolder) {
            when (payload) {
                DevicePrimaryInfoItem.Payload.IS_PRIMARY_CHANGED -> setPrimaryState(item)
            }
        }
    }

    private fun ViewHolder.setPrimaryState(item: DevicePrimaryInfoItem) {
        switchView.isChecked = item.isPrimary
        itemView.isClickable = !item.isPrimary
    }
}
