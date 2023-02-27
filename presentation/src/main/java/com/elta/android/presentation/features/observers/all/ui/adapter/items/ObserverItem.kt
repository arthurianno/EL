package com.elta.android.presentation.features.observers.all.ui.adapter.items

import androidx.annotation.DrawableRes
import com.elta.android.domain.features.observers.model.ObserverStatus
import com.nullgr.core.adapter.items.ListItem

data class ObserverItem(
    val id: String,
    @DrawableRes val type: Int,
    val title: String,
    val description: String,
    @DrawableRes val action: Int,
    val status: ObserverStatus
) : ListItem
