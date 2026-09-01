package com.elta.android.presentation.utils

import com.elta.android.presentation.Events
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BackendSyncStatusStoreTest {

    @After
    fun clearProgress() {
        BackendSyncStatusStore.handle(Events.Sync.Server.Success)
    }

    @Test
    fun `keeps backend progress available until the server sync reaches a terminal state`() {
        BackendSyncStatusStore.handle(Events.Sync.Server.Started)

        assertTrue(BackendSyncStatusStore.isInProgress())

        BackendSyncStatusStore.handle(Events.Sync.Server.Error)

        assertFalse(BackendSyncStatusStore.isInProgress())
    }
}
