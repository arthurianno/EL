package com.elta.android.data.di

import com.elta.android.common.di.qualifires.Firmware
import com.elta.android.common.di.qualifires.UpdateType
import com.elta.android.data.features.devices.glucometer.client.GlucometerClient
import com.elta.android.data.features.devices.glucometer.client.GlucometerClientImpl
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class GlucometerImplModule {
    @Binds
    @Singleton
    @Firmware(UpdateType.NordicDfu)
    abstract fun bindNordicDfuGlucometerClient(@Firmware(UpdateType.NordicDfu) source: GlucometerClientImpl): GlucometerClient

    @Binds
    @Singleton
    @Firmware(UpdateType.BootMode)
    abstract fun bindBootModeGlucometerClient(@Firmware(UpdateType.BootMode) source: GlucometerClientImpl): GlucometerClient
}
