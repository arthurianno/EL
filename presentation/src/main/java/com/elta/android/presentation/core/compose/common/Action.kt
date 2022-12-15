package com.elta.android.presentation.core.compose.common

interface Action

sealed class AppAction : Action {
    object BackPressure : AppAction()
    object FreeScreenTap : AppAction()
}
