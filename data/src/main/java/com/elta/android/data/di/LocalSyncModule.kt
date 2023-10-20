package com.elta.android.data.di

import com.elta.android.data.features.common.dto.StateDto
import com.elta.android.data.features.sync.configuration.ClassConfiguration
import com.elta.android.data.features.sync.configuration.LocalSyncConfig
import com.elta.android.data.features.sync.mappers.EventsSyncMapper
import com.elta.android.data.features.sync.mappers.ProfileSyncMapper
import com.elta.android.domain.features.diary.events.model.EventV2
import com.elta.android.domain.features.user.model.Profile
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
@Suppress("FunctionOnlyReturningConstant", "TooManyFunctions")
class LocalSyncModule {

    @Provides
    @Singleton
    fun provideProfileConfiguration(): ClassConfiguration<Profile> =
        ClassConfiguration(
            supportedState = arrayListOf(StateDto.UPDATED),
            mapper = ProfileSyncMapper()
        )

    @Provides
    @Singleton
    fun provideEventsConfiguration(): ClassConfiguration<EventV2> =
        ClassConfiguration(
            supportedState = arrayListOf(StateDto.UPDATED, StateDto.CREATED, StateDto.DELETED),
            mapper = EventsSyncMapper()
        )

    @Provides
    @Singleton
    fun provideSyncConfiguration(
        eventsConfiguration: ClassConfiguration<EventV2>,
        profileConfiguration: ClassConfiguration<Profile>
    ): LocalSyncConfig = LocalSyncConfig(
        hashMapOf(
            EventV2::class.java to eventsConfiguration,
            Profile::class.java to profileConfiguration
        )
    )
}
