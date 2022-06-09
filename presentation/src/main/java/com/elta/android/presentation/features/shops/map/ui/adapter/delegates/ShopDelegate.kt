package com.elta.android.presentation.features.shops.map.ui.adapter.delegates

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.R
import com.elta.android.presentation.core.bus.click
import com.elta.android.presentation.core.ui.adapter.withAdapterPosition
import com.elta.android.presentation.databinding.ItemShopBinding
import com.elta.android.presentation.features.shops.map.ui.adapter.items.ShopItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate
import com.nullgr.core.adapter.ktx.ViewHolder
import com.nullgr.core.rx.RxBus
import com.nullgr.core.ui.extensions.toggleView

class ShopDelegate(
    private val bus: RxBus
) : AdapterDelegate<ItemShopBinding>(ItemShopBinding::inflate) {

    override val layoutResource: Int = R.layout.item_shop
    override val itemType: Any = ShopItem::class

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return super.onCreateViewHolder(parent).apply {
            with(this as ViewHolder) {
                binding.run {
                    val listener = View.OnClickListener { view ->
                        withAdapterPosition<ShopItem> { _, item, _ ->
                            val click = when (view.id) {
                                R.id.shopRouteView -> Clicks.ShopMakeRoute(item)
                                R.id.shopCallView -> Clicks.ShopMakeCall(item)
                                else -> throw IllegalArgumentException("Unknown view id")
                            }
                            bus.click(click)
                        }
                    }
                    shopRouteView.setOnClickListener(listener)
                    shopCallView.setOnClickListener(listener)
                }
            }
        }
    }

    override fun onBindViewHolder(
        items: List<ListItem>,
        position: Int,
        holder: RecyclerView.ViewHolder
    ) {
        val item = items[position] as ShopItem
        with(binding) {
            shopNameView.text = item.name
            shopAddressView.text = item.address
            shopDistanceView.toggleView(!item.distance.isNullOrEmpty())
            shopDistanceView.text = item.distance
        }
    }

    override fun onBindViewHolder(
        items: List<ListItem>,
        position: Int,
        holder: RecyclerView.ViewHolder,
        payload: Any
    ) {
        val item = items[position] as ShopItem
        with(binding) {
            when (payload) {
                ShopItem.Payload.DISTANCE_CHANGED -> {
                    shopDistanceView.toggleView(!item.distance.isNullOrEmpty())
                    shopDistanceView.text = item.distance
                }
            }
        }
    }
}
