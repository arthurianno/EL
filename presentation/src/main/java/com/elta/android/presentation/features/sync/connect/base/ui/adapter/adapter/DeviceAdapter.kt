package com.elta.android.presentation.features.sync.connect.base.ui.adapter.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.core.ui.adapter.BaseListAdapter
import com.elta.android.presentation.databinding.ItemDeviceBinding
import com.elta.android.presentation.features.sync.connect.base.ui.adapter.adapter.holder.DeviceItemViewHolder
import com.nullgr.core.rx.RxBus
import javax.inject.Inject

class DeviceAdapter @Inject constructor(
    private val bus: RxBus
) : BaseListAdapter() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return DeviceItemViewHolder(
            ItemDeviceBinding.inflate(inflater, parent, false),
            bus
        )
    }
}
