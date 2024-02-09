package com.elta.android.data.di

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
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
import javax.inject.Singleton

@Module(includes = [GlucometerModule.Declarations::class])
class GlucometerModule {

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
    fun provideBluetoothAdapter(bluetoothManager: BluetoothManager): BluetoothAdapter =
        bluetoothManager.adapter

    @Provides
    @Singleton
    fun provideBluetoothManager(context: Context): BluetoothManager =
        context.getSystemService(BluetoothManager::class.java)

    //TODO: обсудить 1 экземпляр или на каждое соединение - свое. Документация говорит, что на каждое - своё.
    //Тут на каждое соединение с разными глюкометрами разный менеджер, подумать на чем-то типа мультитона
    @Provides
    @Singleton
    fun provideBleManager(
        context: Context,
        crashlyticsReport: CrashlyticsReport
    ): GlucometerBleManager = GlucometerBleManager(crashlyticsReport, context)

    @Provides
    @Singleton
    fun provideFirmwareManger(context: Context, crashlyticsReport: CrashlyticsReport): FirmwareManager = FirmwareManagerImpl(context, crashlyticsReport)
}
