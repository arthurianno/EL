package com.elta.android.presentation.core.compose.common

interface Event

class ShowToast(val text: String) : Event

sealed class PermissionEvent : Event {
    override fun equals(other: Any?): Boolean = false

    override fun hashCode(): Int {
        return System.identityHashCode(this)
    }

    class Storage : PermissionEvent()
    class RecordAudio : PermissionEvent()
}
