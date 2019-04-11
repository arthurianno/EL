package com.elta.android.data.di

import com.elta.android.data.features.devices.glucometer.DbGlucometerPinStorage
import com.elta.android.data.features.devices.glucometer.DefaultGlucometerEventBuilder
import com.elta.android.data.features.devices.glucometer.DefaultGlucometerEventIdGenerator
import com.elta.android.data.features.devices.glucometer.DefaultGlucometerInfoBuilder
import com.elta.android.data.features.devices.glucometer.GlucometerEventBuilder
import com.elta.android.data.features.devices.glucometer.GlucometerEventIdGenerator
import com.elta.android.data.features.devices.glucometer.GlucometerInfoBuilder
import com.elta.android.data.features.devices.glucometer.GlucometerPinStorage
import dagger.Binds
import dagger.Module

@Module
@Suppress("UnnecessaryAbstractClass", "TooManyFunctions")
abstract class GlucometerModule {

    @Binds
    abstract fun bindPinStorage(
        storage: DbGlucometerPinStorage
    ): GlucometerPinStorage

    @Binds
    abstract fun bindInfoBuilder(
        builder: DefaultGlucometerInfoBuilder
    ): GlucometerInfoBuilder

    @Binds
    abstract fun bindEventBuilder(
        builder: DefaultGlucometerEventBuilder
    ): GlucometerEventBuilder

    @Binds
    abstract fun bindEventIdGeneratorBuilder(
        generator: DefaultGlucometerEventIdGenerator
    ): GlucometerEventIdGenerator
}