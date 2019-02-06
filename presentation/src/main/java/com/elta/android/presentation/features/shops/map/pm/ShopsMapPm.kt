package com.elta.android.presentation.features.shops.map.pm

import com.elta.android.presentation.R
import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import com.elta.android.presentation.features.shops.map.ui.adapter.items.ShopItem
import com.nullgr.core.adapter.items.ListItem
import javax.inject.Inject

class ShopsMapPm @Inject constructor(
    services: ServiceFacade
) : BasePm(services) {

    val items = State<List<ListItem>>()

    override fun onCreate() {
        super.onCreate()

        items.consumer.accept(
            mutableListOf<ListItem>().apply {
                repeat((0..10).count()) {
                    add(
                        ShopItem(
                            id = it,
                            name = "Test Name #$it",
                            address = "Test Address #$it",
                            distance = resources.getString(R.string.shops_map_distance_km_pattern, it)
                        )
                    )
                }
            }
        )
    }
}