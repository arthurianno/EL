package com.elta.android.data.di

import com.elta.android.common.di.scope.ServiceScope
import com.elta.android.data.features.devices.glucometer.service.firmware.BootModeService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceModule {

    @ServiceScope
    @ContributesAndroidInjector
    abstract fun bindBootModeService(): BootModeService
}
