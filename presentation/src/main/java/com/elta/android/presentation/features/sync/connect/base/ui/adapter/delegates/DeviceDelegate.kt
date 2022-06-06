package com.elta.android.presentation.features.sync.connect.base.ui.adapter.delegates

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.R
import com.elta.android.presentation.core.bus.click
import com.elta.android.presentation.core.ui.adapter.withAdapterPosition
import com.elta.android.presentation.features.sync.connect.base.ui.adapter.items.DeviceItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate
import com.nullgr.core.adapter.ktx.ViewHolder
import com.nullgr.core.rx.RxBus
import com.nullgr.core.ui.extensions.toggleView
import kotlinx.android.synthetic.main.item_device.*

class DeviceDelegate(
    private val bus: RxBus
) : AdapterDelegate() {

    override val layoutResource: Int = R.layout.item_device
    override val itemType: Any = DeviceItem::class

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return super.onCreateViewHolder(parent).apply {
            with(this as ViewHolder) {
                itemView.setOnClickListener {
                    withAdapterPosition<DeviceItem> { _, item, _ ->
                        bus.click(Clicks.DeviceClicked(item))
                    }
                }
            }
        }
    }

    override fun onBindViewHolder(
        items: List<ListItem>,
        position: Int,
        holder: RecyclerView.ViewHolder
    ) {
        val item = items[position] as DeviceItem

        with(holder as ViewHolder) {
            deviceNameView.text = item.name
            deviceAddressView.text = item.address
            deviceChooserView.toggleView(item.isSelected)
            dividerView.toggleView(!item.isTheLast)
        }
    }

    override fun onBindViewHolder(
        items: List<ListItem>,
        position: Int,
        holder: RecyclerView.ViewHolder,
        payload: Any
    ) {
        super.onBindViewHolder(items, position, holder, payload)
        val item = items[position] as DeviceItem

        with(holder as ViewHolder) {
            when (payload) {
                DeviceItem.Payload.SELECTION_CHANGED -> deviceChooserView.toggleView(item.isSelected)
                DeviceItem.Payload.POSITION_CHANGED -> dividerView.toggleView(!item.isTheLast)
            }
        }
    }
}
