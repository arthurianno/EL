package com.elta.android.presentation.features.main.events.selector.model

import androidx.compose.runtime.Immutable

@Immutable
data class EventSelectorUi(
    val id: Long,
    val name: String,
    val hasHint: Boolean,
    val isSelected: Boolean,
    val isVisible: Boolean,
    val meta: Any
)
