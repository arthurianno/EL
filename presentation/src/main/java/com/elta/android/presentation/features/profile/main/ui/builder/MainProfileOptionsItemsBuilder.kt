package com.elta.android.presentation.features.profile.main.ui.builder

import com.elta.android.presentation.R
import com.elta.android.presentation.features.profile.main.ui.adapter.items.MainProfileAdditionalItem
import com.elta.android.presentation.features.profile.main.ui.adapter.items.MainProfileHeaderAdditionalItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.resources.ResourceProvider
import javax.inject.Inject

class MainProfileOptionsItemsBuilder @Inject constructor(
    private val resourceProvider: ResourceProvider
) {

    fun buildItems(): MutableList<ListItem> {
        return arrayListOf<ListItem>().apply {
            add(MainProfileHeaderAdditionalItem(resourceProvider.getString(R.string.profile_additional_functions)))
            addAll(createMainProfileAdditionalItems())
        }
    }

    private fun createMainProfileAdditionalItems(): List<ListItem> {
        val items = mutableListOf<ListItem>()
        items.add(MainProfileAdditionalItem(R.string.profile_my_watchers,
            R.string.profile_management_and_settings,
            R.drawable.ic_doctor)
        )
        items.add(MainProfileAdditionalItem(R.string.profile_where_purchase_products,
            R.string.profile_map_of_stores,
            R.drawable.ic_map_pin_card)
        )
        return items
    }
}