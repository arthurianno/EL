package com.elta.android.presentation.features.devices.all.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.core.ui.adapter.BaseListAdapter
import com.elta.android.presentation.databinding.ItemActiveDeviceBinding
import com.elta.android.presentation.databinding.ItemDevicesHeaderBinding
import com.elta.android.presentation.features.devices.all.ui.adapter.holder.ActiveDeviceViewHolder
import com.elta.android.presentation.features.devices.all.ui.adapter.holder.DevicesHeaderViewHolder
import com.elta.android.presentation.features.devices.all.ui.adapter.items.ActiveDeviceItem
import com.elta.android.presentation.features.devices.all.ui.adapter.items.DevicesHeaderItem
import com.nullgr.core.rx.RxBus
import javax.inject.Inject

class DevicesAdapter @Inject constructor(
    private val bus: RxBus
) : BaseListAdapter() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            DevicesHeaderItem::class.java.hashCode() -> DevicesHeaderViewHolder(
                ItemDevicesHeaderBinding.inflate(inflater, parent, false)
            )
            ActiveDeviceItem::class.java.hashCode() -> ActiveDeviceViewHolder(
                binding = ItemActiveDeviceBinding.inflate(inflater, parent, false),
                bus = bus
            )
            else -> throw IllegalArgumentException("No delegate defined for ${this::class.simpleName}")
        }
    }
}
