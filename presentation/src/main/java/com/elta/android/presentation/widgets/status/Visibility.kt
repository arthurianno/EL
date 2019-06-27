package com.elta.android.presentation.widgets.status

sealed class Visibility {
    abstract val value: Boolean
    abstract val delay: Boolean

    object Show : Visibility() {
        override val value: Boolean = true
        override val delay: Boolean = false
    }

    object Hide : Visibility() {
        override val value: Boolean = false
        override val delay: Boolean = false
    }

    object HideWithDelay : Visibility() {
        override val value: Boolean = false
        override val delay: Boolean = true
    }
}