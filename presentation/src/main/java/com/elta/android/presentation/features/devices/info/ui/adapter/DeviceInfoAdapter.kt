package com.elta.android.presentation.features.devices.info.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.core.ui.adapter.BaseListAdapter
import com.elta.android.presentation.databinding.ItemDeviceInfoBinding
import com.elta.android.presentation.databinding.ItemDevicePrimaryInfoBinding
import com.elta.android.presentation.features.devices.info.ui.adapter.holder.DeviceInfoViewHolder
import com.elta.android.presentation.features.devices.info.ui.adapter.holder.DevicePrimaryInfoViewHolder
import com.elta.android.presentation.features.devices.info.ui.adapter.items.DeviceInfoItem
import com.elta.android.presentation.features.devices.info.ui.adapter.items.DevicePrimaryInfoItem
import com.nullgr.core.rx.RxBus
import javax.inject.Inject

class DeviceInfoAdapter @Inject constructor(
    private val bus: RxBus
) : BaseListAdapter() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from((parent.context))
        return when (viewType) {
            DeviceInfoItem::class.java.hashCode() -> DeviceInfoViewHolder(
                ItemDeviceInfoBinding.inflate(inflater, parent, false)
            )
            DevicePrimaryInfoItem::class.java.hashCode() -> DevicePrimaryInfoViewHolder(
                ItemDevicePrimaryInfoBinding.inflate(inflater, parent, false),
                bus
            )
            else -> {
                throw IllegalArgumentException("No delegate defined for ${this::class.simpleName}")
            }
        }
    }
}
