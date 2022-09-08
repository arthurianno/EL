package com.elta.android.presentation.features.shops.map.ui.adapter.holder

import com.elta.android.presentation.core.ui.adapter.BaseListItemViewHolder
import com.elta.android.presentation.core.ui.adapter.bindCardCorners
import com.elta.android.presentation.databinding.ItemSearchHeaderBinding
import com.elta.android.presentation.features.shops.map.ui.adapter.items.SearchHeaderItem
import com.nullgr.core.adapter.items

class SearchHeaderViewHolder(
    binding: ItemSearchHeaderBinding
) : BaseListItemViewHolder<SearchHeaderItem>(binding.root) {
    override fun bind(item: SearchHeaderItem) {
        bindCardCorners(items().orEmpty(), adapterPosition, this)
    }
}
