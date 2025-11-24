package com.elta.android.data.di

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.location.LocationManager
import com.elta.android.common.di.qualifires.Firmware
import com.elta.android.common.di.qualifires.UpdateType
import com.elta.android.common.logger.crashlyrics.CrashlyticsReport
import com.elta.android.data.features.devices.glucometer.builder.DefaultGlucometerEventBuilder
import com.elta.android.data.features.devices.glucometer.builder.DefaultGlucometerInfoBuilder
import com.elta.android.data.features.devices.glucometer.builder.GlucometerEventBuilder
import com.elta.android.data.features.devices.glucometer.builder.GlucometerInfoBuilder
import com.elta.android.data.features.devices.glucometer.client.GlucometerBleManager
import com.elta.android.data.features.devices.glucometer.firmware.FirmwareManager
import com.elta.android.data.features.devices.glucometer.firmware.FirmwareManagerImpl
import com.elta.android.data.features.devices.glucometer.generator.DefaultGlucometerEventIdGenerator
import com.elta.android.data.features.devices.glucometer.generator.GlucometerEventIdGenerator
import com.elta.android.data.features.devices.glucometer.storage.DbGlucometerPinStorage
import com.elta.android.data.features.devices.glucometer.storage.GlucometerPinStorage
import dagger.Binds
import dagger.Module
import dagger.Provides
import no.nordicsemi.android.ble.ConnectionPriorityRequest
import javax.inject.Singleton

@Module(includes = [BleToolsModule.Declarations::class])
class BleToolsModule {

    @Module
    interface Declarations {
        @Binds
        fun bindPinStorage(
            storage: DbGlucometerPinStorage
        ): GlucometerPinStorage

        @Binds
        fun bindInfoBuilder(
            builder: DefaultGlucometerInfoBuilder
        ): GlucometerInfoBuilder

        @Binds
        fun bindEventBuilder(
            builder: DefaultGlucometerEventBuilder
        ): GlucometerEventBuilder

        @Binds
        fun bindEventIdGeneratorBuilder(
            generator: DefaultGlucometerEventIdGenerator
        ): GlucometerEventIdGenerator
    }

    @Provides
    @Singleton
    @Firmware(UpdateType.BootMode)
    fun provideBootModeBleManager(
        crashlyticsReport: CrashlyticsReport,
        context: Context
    ): GlucometerBleManager =
        GlucometerBleManager.Builder()
            .setMtu(247)
            .setConnectionPriority(ConnectionPriorityRequest.CONNECTION_PRIORITY_HIGH)
            .setCrashlyticsReport(crashlyticsReport)
            .setContext(context)
            .build()

    @Provides
    @Singleton
    @Firmware(UpdateType.NordicDfu)
    fun provideNordicDfuBleManager(
        crashlyticsReport: CrashlyticsReport,
        context: Context
    ): GlucometerBleManager =
        GlucometerBleManager.Builder()
            .setCrashlyticsReport(crashlyticsReport)
            .setContext(context)
            .build()

    @Provides
    @Singleton
    fun provideBluetoothAdapter(bluetoothManager: BluetoothManager): BluetoothAdapter =
        bluetoothManager.adapter

    @Provides
    @Singleton
    fun provideBluetoothManager(context: Context): BluetoothManager =
        context.getSystemService(BluetoothManager::class.java)

    @Provides
    @Singleton
    fun provideLocationManager(
        context: Context
    ): LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    @Provides
    @Singleton
    fun provideFirmwareManager(
        context: Context,
        crashlyticsReport: CrashlyticsReport
    ): FirmwareManager = FirmwareManagerImpl(context, crashlyticsReport)
}
