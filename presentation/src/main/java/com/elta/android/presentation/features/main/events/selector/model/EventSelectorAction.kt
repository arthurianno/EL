package com.elta.android.presentation.features.main.events.selector.model

import com.elta.android.presentation.core.compose.common.Action

internal sealed class EventSelectorAction : Action {
    data class SelectionClicked(val id: Long) : EventSelectorAction()
    data class RecentQueryClicked(val data: Pair<Long, String>) : EventSelectorAction()
    data class LastSearchersClicked(val word: String) : EventSelectorAction()
    object AddOther : EventSelectorAction()
    object ClearEvents: EventSelectorAction()
}
