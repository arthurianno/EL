package com.elta.android.data.di

import android.content.Context
import com.elta.android.data.features.common.storage.SyncStorage
import com.elta.android.data.features.googlefit.datasource.GoogleFitDataSource
import com.elta.android.data.features.googlefit.datasource.HealthAppDataSource
import com.elta.android.data.features.googlefit.datasource.HealthConnectDataSource
import com.elta.android.data.features.googlefit.datasource.HybridHealthDataSource
import com.elta.android.data.features.googlefit.mapper.HealthConnectExerciseToActivityMapper
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Dagger module for Health Connect and Google Fit integration
 */
@Module
class HealthConnectModule {

    @Provides
    @Singleton
    fun provideHealthConnectExerciseMapper(): HealthConnectExerciseToActivityMapper {
        return HealthConnectExerciseToActivityMapper()
    }

    @Provides
    @Singleton
    fun provideHealthConnectDataSource(
        context: Context,
        syncStorage: SyncStorage,
        mapper: HealthConnectExerciseToActivityMapper
    ): HealthConnectDataSource {
        return HealthConnectDataSource(context, syncStorage, mapper)
    }

    @Provides
    @Singleton
    fun provideHybridHealthDataSource(
        healthConnectDataSource: HealthConnectDataSource,
        googleFitDataSource: GoogleFitDataSource
    ): HybridHealthDataSource {
        return HybridHealthDataSource(healthConnectDataSource, googleFitDataSource)
    }

    @Provides
    @Singleton
    fun provideHealthAppDataSource(
        hybridHealthDataSource: HybridHealthDataSource
    ): HealthAppDataSource {
        return hybridHealthDataSource
    }
}

