package com.elta.android.presentation.core.compose.common

interface Event

data class ShowToast(val text: String) : Event {
    override fun equals(other: Any?): Boolean = false
    override fun hashCode(): Int = text.hashCode()

}

sealed class PermissionEvent : Event {
    override fun equals(other: Any?): Boolean = false

    override fun hashCode(): Int {
        return System.identityHashCode(this)
    }

    class Storage : PermissionEvent()
    class RecordAudio : PermissionEvent()
}
