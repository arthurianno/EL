package com.elta.android.presentation.core.ui.adapter

import com.nullgr.core.adapter.items.ListItem

interface HideableItem : ListItem {
    val isVisible: Boolean
}
