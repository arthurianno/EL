package com.elta.android.presentation.features.bluetooth.di

import dagger.Module

@Module(includes = [BluetoothModule.Declarations::class])
class BluetoothModule {

    @Module
    interface Declarations {
        // Place @Binds here
    }

    // Place @Provides here
}