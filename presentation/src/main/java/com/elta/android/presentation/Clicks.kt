package com.elta.android.presentation

import com.elta.android.domain.features.events.model.UserEvent
import com.elta.android.presentation.core.bus.Click
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordItem
import com.elta.android.presentation.features.shops.map.ui.adapter.items.SearchResultItem
import com.elta.android.presentation.features.shops.map.ui.adapter.items.ShopItem

sealed class Clicks : Click {

    data class ShopMakeRoute(val item: ShopItem) : Clicks()
    data class ShopMakeCall(val item: ShopItem) : Clicks()
    data class SearchResult(val item: SearchResultItem) : Clicks()
    data class AddUserEvent(val userEvent: UserEvent) : Clicks()
    data class RecordClicked(val item: RecordItem): Clicks()
}