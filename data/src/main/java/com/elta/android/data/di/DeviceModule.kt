package com.elta.android.data.di

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.content.Context
import com.elta.android.data.features.devices.glucometer.storage.DbGlucometerPinStorage
import com.elta.android.data.features.devices.glucometer.builder.DefaultGlucometerEventBuilder
import com.elta.android.data.features.devices.glucometer.generator.DefaultGlucometerEventIdGenerator
import com.elta.android.data.features.devices.glucometer.builder.DefaultGlucometerInfoBuilder
import com.elta.android.data.features.devices.glucometer.builder.GlucometerEventBuilder
import com.elta.android.data.features.devices.glucometer.generator.GlucometerEventIdGenerator
import com.elta.android.data.features.devices.glucometer.builder.GlucometerInfoBuilder
import com.elta.android.data.features.devices.glucometer.refactor.GlucometerBleManager
import com.elta.android.data.features.devices.glucometer.refactor.Manager
import com.elta.android.data.features.devices.glucometer.refactor.ManagerImpl
import com.elta.android.data.features.devices.glucometer.storage.GlucometerPinStorage
import com.polidea.rxandroidble2.LogConstants
import com.polidea.rxandroidble2.LogOptions
import com.polidea.rxandroidble2.RxBleClient
import dagger.Binds
import dagger.Module
import dagger.Provides
import timber.log.Timber
import javax.inject.Singleton

@Module
abstract class DeviceModule {
    //TODO: переместить в новый Module???
    @Binds
    @Singleton
    abstract fun bindManager(source: ManagerImpl): Manager
}
