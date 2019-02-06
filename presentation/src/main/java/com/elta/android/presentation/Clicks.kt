package com.elta.android.presentation

import com.elta.android.presentation.core.bus.Click
import com.elta.android.presentation.features.shops.map.ui.adapter.items.ShopItem

sealed class Clicks : Click {

    data class ShopMakeRoute(val item: ShopItem) : Clicks()
    data class ShopMakeCall(val item: ShopItem) : Clicks()
}