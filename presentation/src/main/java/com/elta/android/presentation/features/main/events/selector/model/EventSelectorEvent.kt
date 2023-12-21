package com.elta.android.presentation.features.main.events.selector.model

import com.elta.android.presentation.core.compose.common.Event

sealed class EventSelectorEvent: Event {
    data class ScrollToPosition(val id: Long): EventSelectorEvent()
}
