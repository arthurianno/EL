package com.elta.android.presentation.features.home.ui.adapter.items

import com.elta.android.domain.features.diary.events.model.EventType
import com.nullgr.core.adapter.items.ListItem

data class UserEventItem(
    val iconRes: Int,
    val titleRes: Int,
    val event: EventType
) : ListItem