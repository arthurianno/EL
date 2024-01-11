package com.elta.android.data.di

import android.content.Context
import com.elta.android.data.features.devices.glucometer.storage.DbGlucometerPinStorage
import com.elta.android.data.features.devices.glucometer.builder.DefaultGlucometerEventBuilder
import com.elta.android.data.features.devices.glucometer.generator.DefaultGlucometerEventIdGenerator
import com.elta.android.data.features.devices.glucometer.builder.DefaultGlucometerInfoBuilder
import com.elta.android.data.features.devices.glucometer.builder.GlucometerEventBuilder
import com.elta.android.data.features.devices.glucometer.generator.GlucometerEventIdGenerator
import com.elta.android.data.features.devices.glucometer.builder.GlucometerInfoBuilder
import com.elta.android.data.features.devices.glucometer.storage.GlucometerPinStorage
import com.polidea.rxandroidble2.LogConstants
import com.polidea.rxandroidble2.LogOptions
import com.polidea.rxandroidble2.RxBleClient
import dagger.Binds
import dagger.Module
import dagger.Provides
import timber.log.Timber
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
    fun provideRxBleClient(context: Context): RxBleClient =
        RxBleClient
            .create(context)
            .also {
                RxBleClient.updateLogOptions(
                    LogOptions.Builder()
                        .setLogLevel(LogConstants.DEBUG)
                        .setLogger { level, tag, msg ->
                            Timber.tag(tag).log(level, msg)
                        }
                        .build()
                )
            }
}
