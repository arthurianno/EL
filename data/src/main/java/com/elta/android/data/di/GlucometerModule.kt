package com.elta.android.data.di

import android.bluetooth.BluetoothAdapter
import com.elta.android.common.di.qualifires.Firmware
import com.elta.android.common.di.qualifires.UpdateType
import com.elta.android.common.logger.crashlyrics.CrashlyticsReport
import com.elta.android.data.features.devices.glucometer.client.EnvironmentScanner
import com.elta.android.data.features.devices.glucometer.client.GlucometerBleManager
import com.elta.android.data.features.devices.glucometer.client.GlucometerClientImpl
import com.elta.android.data.features.devices.glucometer.firmware.FirmwareManager
import com.elta.android.domain.features.devices.repository.BluetoothStateRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class GlucometerModule {
    @Provides
    @Singleton
    @Firmware(UpdateType.NordicDfu)
    fun provideNordicDfuGlucometerClient(
        @Firmware(UpdateType.NordicDfu) glucometerBleManager: GlucometerBleManager,
        firmwareManager: FirmwareManager,
        environmentScanner: EnvironmentScanner,
        crashlyticsReport: CrashlyticsReport,
        bluetoothStateRepository: BluetoothStateRepository,
        bluetoothAdapter: BluetoothAdapter
    ) = GlucometerClientImpl(
        glucometerBleManager,
        firmwareManager,
        environmentScanner,
        crashlyticsReport,
        bluetoothAdapter,
        bluetoothStateRepository
    )

    @Provides
    @Singleton
    @Firmware(UpdateType.BootMode)
    fun provideBootModeGlucometerClient(
        @Firmware(UpdateType.BootMode) glucometerBleManager: GlucometerBleManager,
        firmwareManager: FirmwareManager,
        environmentScanner: EnvironmentScanner,
        crashlyticsReport: CrashlyticsReport,
        bluetoothStateRepository: BluetoothStateRepository,
        bluetoothAdapter: BluetoothAdapter
    ) = GlucometerClientImpl(
        glucometerBleManager,
        firmwareManager,
        environmentScanner,
        crashlyticsReport,
        bluetoothAdapter,
        bluetoothStateRepository
    )
}
