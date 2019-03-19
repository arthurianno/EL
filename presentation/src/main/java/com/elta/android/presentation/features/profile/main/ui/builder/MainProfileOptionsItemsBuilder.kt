package com.elta.android.presentation.features.profile.main.ui.builder

import com.elta.android.presentation.R
import com.elta.android.presentation.features.profile.main.ui.adapter.items.MainProfileAdditionalItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.resources.ResourceProvider
import javax.inject.Inject

class MainProfileOptionsItemsBuilder @Inject constructor(
    private val resourceProvider: ResourceProvider
) {

    fun buildItems(): MutableList<ListItem> {
        return arrayListOf<ListItem>().apply {
            add(MainProfileAdditionalItem(resourceProvider.getString(R.string.profile_additional_functions)))
        }
    }
}