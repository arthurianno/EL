package com.elta.android.presentation.core.compose.common

interface Event

class ShowToast(val text: String) : Event

sealed class PermissionEvent : Event {
    override fun equals(other: Any?) = false
    override fun hashCode() = System.identityHashCode(this)

    class Storage : PermissionEvent()
    object Camera : PermissionEvent()
    class RecordAudio : PermissionEvent()
    object OpenSettings : PermissionEvent()
    object RequestPermissions : PermissionEvent()
    object RequestEnableLocation : PermissionEvent()

    sealed class Bluetooth : PermissionEvent() {
        object RequestEnable : Bluetooth()
        object OnAllow : Bluetooth()
    }
}

object ClearEvent : Event
