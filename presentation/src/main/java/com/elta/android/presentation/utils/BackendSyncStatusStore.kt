package com.elta.android.presentation.utils

import com.elta.android.presentation.Events

/**
 * Holds only the active backend-sync phase. Unlike the event bus this value can be read by a
 * screen that is created after the sync has already started.
 */
object BackendSyncStatusStore {
    @Volatile
    private var backendSyncInProgress = false

    fun handle(event: Events.Sync) {
        when (event) {
            is Events.Sync.Server.Started -> backendSyncInProgress = true
            is Events.Sync.Server.Success,
            is Events.Sync.Server.Error,
            is Events.Sync.Server.ErrorWithMessage -> backendSyncInProgress = false
            else -> Unit
        }
    }

    fun isInProgress(): Boolean = backendSyncInProgress
}
