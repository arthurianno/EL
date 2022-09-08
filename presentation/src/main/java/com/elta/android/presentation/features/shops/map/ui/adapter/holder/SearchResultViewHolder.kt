package com.elta.android.presentation.features.shops.map.ui.adapter.holder

import com.elta.android.presentation.Clicks
import com.elta.android.presentation.core.bus.click
import com.elta.android.presentation.core.ui.adapter.BaseListItemViewHolder
import com.elta.android.presentation.databinding.ItemSearchResultBinding
import com.elta.android.presentation.features.shops.map.ui.adapter.items.SearchResultItem
import com.nullgr.core.rx.RxBus

class SearchResultViewHolder(
    private val binding: ItemSearchResultBinding,
    private val bus: RxBus
) : BaseListItemViewHolder<SearchResultItem>(binding.root) {
    override fun bind(item: SearchResultItem) {
        with(binding) {
            nameView.text = item.name
            addressView.text = item.address
            root.setOnClickListener {
                bus.click(Clicks.SearchResult(item))
            }
        }
    }
}
