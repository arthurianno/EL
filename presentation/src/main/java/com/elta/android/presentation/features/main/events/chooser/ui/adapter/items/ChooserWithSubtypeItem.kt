package com.elta.android.presentation.features.main.events.chooser.ui.adapter.items

import com.nullgr.core.adapter.items.ListItem

data class ChooserWithSubtypeItem(
    val id: String,
    val title: String,
    val iconId: Int?,
    val meta: Any,
    val isSelectedType: Boolean,
    val drug: String?
) : ListItem {
    override fun getUniqueProperty() = id
}
