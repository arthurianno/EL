package com.elta.android.presentation.features.shops.map.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.core.ui.adapter.BaseListAdapter
import com.elta.android.presentation.databinding.ItemSearchHeaderBinding
import com.elta.android.presentation.databinding.ItemSearchResultBinding
import com.elta.android.presentation.databinding.ItemShopBinding
import com.elta.android.presentation.features.shops.map.ui.adapter.holder.SearchHeaderViewHolder
import com.elta.android.presentation.features.shops.map.ui.adapter.holder.SearchResultViewHolder
import com.elta.android.presentation.features.shops.map.ui.adapter.holder.ShopViewHolder
import com.elta.android.presentation.features.shops.map.ui.adapter.items.SearchHeaderItem
import com.elta.android.presentation.features.shops.map.ui.adapter.items.SearchResultItem
import com.elta.android.presentation.features.shops.map.ui.adapter.items.ShopItem
import com.nullgr.core.rx.RxBus
import javax.inject.Inject

class MapAdapter @Inject constructor(
    private val bus: RxBus
) : BaseListAdapter() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ShopItem::class.java.hashCode() -> ShopViewHolder(
                binding = ItemShopBinding.inflate(inflater, parent, false),
                bus = bus
            )
            SearchHeaderItem::class.java.hashCode() -> SearchHeaderViewHolder(
                binding = ItemSearchHeaderBinding.inflate(inflater, parent, false)
            )
            SearchResultItem::class.java.hashCode() -> SearchResultViewHolder(
                binding = ItemSearchResultBinding.inflate(inflater, parent, false),
                bus = bus
            )
            else -> throw IllegalArgumentException("No delegate defined for ${this::class.simpleName}")
        }
    }
}
