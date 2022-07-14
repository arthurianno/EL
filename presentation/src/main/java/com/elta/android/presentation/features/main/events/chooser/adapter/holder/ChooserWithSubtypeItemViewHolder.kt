package com.elta.android.presentation.features.main.events.chooser.adapter.holder

import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.adapter.BaseListItemViewHolder
import com.elta.android.presentation.databinding.ItemChooserBinding
import com.elta.android.presentation.features.main.events.chooser.adapter.items.ChooserWithSubtypeItem
import com.nullgr.core.rx.RxBus
import com.nullgr.core.ui.extensions.toggleView

class ChooserWithSubtypeItemViewHolder(
    private val binding: ItemChooserBinding,
    private val bus: RxBus
) : BaseListItemViewHolder<ChooserWithSubtypeItem>(binding.root) {
    override fun bind(item: ChooserWithSubtypeItem) {
        with(binding) {
            chooserIconView.toggleView(item.iconId != null)
            item.iconId?.let { chooserIconView.setImageResource(it) }
            chooserTitleView.text = item.title
            chooserRightIconView.toggleView(true)
            chooserRightIconView.setImageResource(R.drawable.ic_arrow_left)
            root.setOnClickListener {
                // click
            }
        }
    }
}
