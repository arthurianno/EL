package com.elta.android.presentation.features.main.events.selector.model

data class EventSelectorViewState(
    val isLoading: Boolean,
    val isError: Boolean,
    val searchInFocus: Boolean,
    val lastSearchers: List<EventSelectorUi>,
    val recentlySelection: List<EventSelectorUi>,
    val selection: List<EventSelectorUi>,
    val previousSelection: EventSelectorUi?
)