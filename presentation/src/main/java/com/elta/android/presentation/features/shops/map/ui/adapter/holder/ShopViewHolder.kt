package com.elta.android.presentation.features.shops.map.ui.adapter.holder

import android.view.View
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.R
import com.elta.android.presentation.core.bus.click
import com.elta.android.presentation.core.ui.adapter.BaseListItemViewHolder
import com.elta.android.presentation.databinding.ItemShopBinding
import com.elta.android.presentation.features.shops.map.ui.adapter.items.ShopItem
import com.nullgr.core.rx.RxBus
import com.nullgr.core.ui.extensions.toggleView

class ShopViewHolder(
    private val binding: ItemShopBinding,
    private val bus: RxBus
) : BaseListItemViewHolder<ShopItem>(binding.root) {
    override fun bind(item: ShopItem) {
        with(binding) {
            shopNameView.text = item.name
            shopAddressView.text = item.address
            shopDistanceView.toggleView(!item.distance.isNullOrEmpty())
            shopDistanceView.text = item.distance
            val listener = View.OnClickListener { view ->
                val click = when (view.id) {
                    R.id.shopRouteView -> Clicks.ShopMakeRoute(item)
                    R.id.shopCallView -> Clicks.ShopMakeCall(item)
                    else -> throw IllegalArgumentException("Unknown view id")
                }
                bus.click(click)
            }
            shopRouteView.setOnClickListener(listener)
            shopCallView.setOnClickListener(listener)
        }
    }
}
