package com.elta.android.presentation.features.observers.all.ui.adapter.items

import com.nullgr.core.adapter.items.ListItem

data class ObserverHeaderItem(val title: String) : ListItem {

    override fun getUniqueProperty() = title
}