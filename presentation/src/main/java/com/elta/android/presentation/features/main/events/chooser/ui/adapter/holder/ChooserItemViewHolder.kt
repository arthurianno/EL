package com.elta.android.presentation.features.main.events.chooser.ui.adapter.holder

import com.elta.android.presentation.Clicks
import com.elta.android.presentation.core.bus.click
import com.elta.android.presentation.core.ui.adapter.BaseListItemViewHolder
import com.elta.android.presentation.databinding.ItemChooserBinding
import com.elta.android.presentation.features.main.events.chooser.ui.adapter.items.ChooserItem
import com.nullgr.core.rx.RxBus
import com.nullgr.core.ui.extensions.toggleView

class ChooserItemViewHolder(
    private val binding: ItemChooserBinding,
    private val bus: RxBus
) : BaseListItemViewHolder<ChooserItem>(binding.root) {
    override fun bind(item: ChooserItem) {
        with(binding) {
            chooserIconView.toggleView(item.iconId != null)
            item.iconId?.let { chooserIconView.setImageResource(it) }
            chooserTitleView.text = item.title
            chooserRightIconView.toggleView(item.isSelected)
            root.setOnClickListener {
                bus.click(Clicks.ChooserOptionClicked(item.id))
            }
        }
    }
}
