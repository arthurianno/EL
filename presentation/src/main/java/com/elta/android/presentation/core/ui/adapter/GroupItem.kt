package com.elta.android.presentation.core.ui.adapter

import com.nullgr.core.adapter.items.ListItem

interface GroupItem : ListItem {
    var isExpanded: Boolean
    val items: List<ListItem>
}