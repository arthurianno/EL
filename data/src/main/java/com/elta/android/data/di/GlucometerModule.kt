package com.elta.android.data.di

import android.content.Context
import com.elta.android.data.features.devices.glucometer.DbGlucometerPinStorage
import com.elta.android.data.features.devices.glucometer.DefaultGlucometerEventBuilder
import com.elta.android.data.features.devices.glucometer.DefaultGlucometerEventIdGenerator
import com.elta.android.data.features.devices.glucometer.DefaultGlucometerInfoBuilder
import com.elta.android.data.features.devices.glucometer.GlucometerEventBuilder
import com.elta.android.data.features.devices.glucometer.GlucometerEventIdGenerator
import com.elta.android.data.features.devices.glucometer.GlucometerInfoBuilder
import com.elta.android.data.features.devices.glucometer.GlucometerPinStorage
import com.polidea.rxandroidble2.RxBleClient
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
    fun provideRxBleClient(context: Context): RxBleClient = RxBleClient.create(context)
}