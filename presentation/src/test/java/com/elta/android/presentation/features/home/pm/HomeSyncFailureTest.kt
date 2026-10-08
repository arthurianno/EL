package com.elta.android.presentation.features.home.pm

import com.elta.android.common.errors.BluetoothNotEnabledError
import com.elta.android.common.errors.BluetoothPermissionNotGrantedError
import com.elta.android.common.errors.GlucometerDeviceHardwareError
import com.elta.android.common.errors.GlucometerOfflineError
import com.elta.android.common.errors.GlucometerSyncError
import com.elta.android.common.errors.PrimaryGlucometerNotFoundError
import java.util.concurrent.TimeoutException
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeSyncFailureTest {
    @Test
    fun `manual failures distinguish retry, settings, device and hardware paths`() {
        assertEquals(ManualSyncFailure.CONNECTION_DISABLED, classifyManualSyncFailure(BluetoothNotEnabledError))
        assertEquals(ManualSyncFailure.PERMISSION_DENIED, classifyManualSyncFailure(BluetoothPermissionNotGrantedError))
        assertEquals(ManualSyncFailure.DEVICE_MISSING, classifyManualSyncFailure(PrimaryGlucometerNotFoundError))
        assertEquals(ManualSyncFailure.DEVICE_OFFLINE, classifyManualSyncFailure(GlucometerOfflineError))
        assertEquals(ManualSyncFailure.HARDWARE, classifyManualSyncFailure(GlucometerSyncError(GlucometerDeviceHardwareError)))
        assertEquals(ManualSyncFailure.DEVICE_TIMEOUT, classifyManualSyncFailure(GlucometerSyncError(TimeoutException())))
        assertEquals(ManualSyncFailure.DEVICE_ERROR, classifyManualSyncFailure(GlucometerSyncError(IllegalStateException())))
    }

    @Test
    fun `automatic failures distinguish silent and visible errors`() {
        assertEquals(AutoSyncFailure.SILENT, classifyAutoSyncFailure(BluetoothNotEnabledError))
        assertEquals(AutoSyncFailure.MESSAGE, classifyAutoSyncFailure(GlucometerSyncError(GlucometerOfflineError)))
        assertEquals(AutoSyncFailure.DEVICE_MISSING, classifyAutoSyncFailure(PrimaryGlucometerNotFoundError))
        assertEquals(AutoSyncFailure.UNEXPECTED, classifyAutoSyncFailure(IllegalStateException()))
    }
}
