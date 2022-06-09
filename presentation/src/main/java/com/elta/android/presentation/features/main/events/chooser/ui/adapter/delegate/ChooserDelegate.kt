package com.elta.android.presentation.features.main.events.chooser.ui.adapter.delegate

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.R
import com.elta.android.presentation.core.bus.click
import com.elta.android.presentation.core.ui.adapter.withAdapterPosition
import com.elta.android.presentation.databinding.ItemChooserBinding
import com.elta.android.presentation.features.main.events.chooser.ui.adapter.items.ChooserItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate
import com.nullgr.core.adapter.ktx.ViewHolder
import com.nullgr.core.rx.RxBus
import com.nullgr.core.ui.extensions.toggleView

class ChooserDelegate(private val bus: RxBus) :
    AdapterDelegate<ItemChooserBinding>(ItemChooserBinding::inflate) {

    override val layoutResource: Int = R.layout.item_chooser
    override val itemType: Any = ChooserItem::class

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return super.onCreateViewHolder(parent).apply {
            with(this as ViewHolder) {
                itemView.setOnClickListener {
                    withAdapterPosition<ChooserItem> { _, item, _ ->
                        bus.click(Clicks.ChooserOptionClicked(item.id))
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
        val item = items[position] as ChooserItem
        with(binding) {
            chooserIconView.toggleView(item.iconId != null)
            item.iconId?.let { chooserIconView.setImageResource(it) }
            chooserTitleView.text = item.title
            chooserSelectedIconView.toggleView(item.isSelected)
        }
    }

    override fun onBindViewHolder(
        items: List<ListItem>,
        position: Int,
        holder: RecyclerView.ViewHolder,
        payload: Any
    ) {
        super.onBindViewHolder(items, position, holder, payload)
        val item = items[position] as ChooserItem
        when (payload) {
            ChooserItem.Payload.SELECTION_CHANGED -> binding.chooserSelectedIconView.toggleView(item.isSelected)
        }
    }
}
