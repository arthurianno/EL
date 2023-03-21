package com.elta.android.presentation.features.devices.search.model

import com.elta.android.presentation.core.compose.common.Action

sealed class GlucometerSearchAction : Action {
    object StartConnection : GlucometerSearchAction()
    object StopSearch : GlucometerSearchAction()
}
