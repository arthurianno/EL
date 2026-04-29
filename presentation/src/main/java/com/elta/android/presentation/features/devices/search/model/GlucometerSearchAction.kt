package com.elta.android.presentation.features.devices.search.model

import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.common.Event


sealed class GlucometerSearchAction : Action {
    object StartConnection : GlucometerSearchAction()
    object StopSearch : GlucometerSearchAction()

    object Location {
        data object AllowPermission : GlucometerSearchAction()
        data object DeniedPermission : GlucometerSearchAction()
        data object Enable : GlucometerSearchAction()
    }

    object Bluetooth {
        data object Enable : GlucometerSearchAction()
        data object Reject : GlucometerSearchAction()
    }
}


sealed interface GlucometerSearchEvent : Event {
    object Location {
        data object RequestPermission : GlucometerSearchEvent
        data object Enable : GlucometerSearchEvent
    }

    object Bluetooth {
        data object Enable : GlucometerSearchEvent
    }
}
