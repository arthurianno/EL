package com.elta.android.presentation.core.ui.adapter

import com.nullgr.core.adapter.items.ListItem

interface ParentItem : ListItem {
    val items: List<ListItem>
}