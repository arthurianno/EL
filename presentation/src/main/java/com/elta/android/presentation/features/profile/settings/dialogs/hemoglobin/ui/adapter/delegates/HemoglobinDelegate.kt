package com.elta.android.presentation.features.profile.settings.dialogs.hemoglobin.ui.adapter.delegates

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.R
import com.elta.android.presentation.core.bus.click
import com.elta.android.presentation.core.ui.adapter.withAdapterPosition
import com.elta.android.presentation.databinding.ItemHemoglobinBinding
import com.elta.android.presentation.features.profile.settings.dialogs.hemoglobin.ui.adapter.items.HemoglobinItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate
import com.nullgr.core.adapter.ktx.ViewHolder
import com.nullgr.core.rx.RxBus

class HemoglobinDelegate(val bus: RxBus) :
    AdapterDelegate<ItemHemoglobinBinding>(ItemHemoglobinBinding::inflate) {

    override val layoutResource: Int = R.layout.item_hemoglobin
    override val itemType: Any = HemoglobinItem::class

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return super.onCreateViewHolder(parent).apply {
            with(this as ViewHolder) {
                binding.deleteButtonView.setOnClickListener {
                    withAdapterPosition<HemoglobinItem> { _, item, _ ->
                        bus.click(Clicks.DeleteHemoglobinEventClicked(item.id))
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
        val item = items[position] as HemoglobinItem
        with(binding) {
            valueTextView.text = item.value
            dateTextView.text = item.date
        }
    }
}
