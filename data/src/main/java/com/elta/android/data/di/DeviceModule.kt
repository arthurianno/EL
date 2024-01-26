package com.elta.android.data.di

import com.elta.android.data.features.devices.glucometer.client.GlucometerClient
import com.elta.android.data.features.devices.glucometer.client.GlucometerClientImpl
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class DeviceModule {
    //TODO: переместить в новый Module???
    @Binds
    @Singleton
    abstract fun bindManager(source: GlucometerClientImpl): GlucometerClient
}
