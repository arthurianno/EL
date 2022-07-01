package com.elta.android.presentation.features.shops.map.ui.adapter

import com.elta.android.presentation.features.shops.map.ui.adapter.delegates.SearchHeaderDelegate
import com.elta.android.presentation.features.shops.map.ui.adapter.delegates.SearchResultDelegate
import com.elta.android.presentation.features.shops.map.ui.adapter.delegates.ShopDelegate
import com.elta.android.presentation.features.shops.map.ui.adapter.items.SearchHeaderItem
import com.elta.android.presentation.features.shops.map.ui.adapter.items.SearchResultItem
import com.elta.android.presentation.features.shops.map.ui.adapter.items.ShopItem
import com.nullgr.core.adapter.AdapterDelegate
import com.nullgr.core.adapter.AdapterDelegatesFactory
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.rx.RxBus
import javax.inject.Inject

class ShopDelegatesFactory @Inject constructor(
    private val bus: RxBus
) : AdapterDelegatesFactory {

    override fun createDelegate(clazz: Class<ListItem>): AdapterDelegate =
        when (clazz) {
            ShopItem::class.java -> ShopDelegate(bus)
            SearchHeaderItem::class.java -> SearchHeaderDelegate()
            SearchResultItem::class.java -> SearchResultDelegate(bus)
            else -> throw IllegalArgumentException("No delegate defined for ${clazz.simpleName}")
        }
}
