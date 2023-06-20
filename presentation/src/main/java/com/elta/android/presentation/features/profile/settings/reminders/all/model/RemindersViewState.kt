package com.elta.android.presentation.features.profile.settings.reminders.all.model

import androidx.compose.runtime.Immutable
import com.nullgr.core.adapter.items.ListItem

@Immutable
data class RemindersViewState(
    val reminders: List<ListItem>
)
