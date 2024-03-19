package com.elta.android.presentation.features.version.mandatory.model

import com.elta.android.presentation.core.compose.common.Event

sealed class MandatoryUpdateEvent: Event {
    object OpenAppPageInStore: MandatoryUpdateEvent()
}
