package com.elta.android.presentation.features.profile.settings.emias.model

import com.elta.android.presentation.core.compose.common.Action

sealed class EmiasProfileAction : Action {
    object UnbindEmias : EmiasProfileAction()
}
