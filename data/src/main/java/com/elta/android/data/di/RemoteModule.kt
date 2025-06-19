package com.elta.android.data.di

import com.elta.android.data.features.remoteconfig.datasource.FirebaseConfigManager
import com.elta.android.data.features.remoteconfig.datasource.RemoteConfigManager
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class RemoteModule {
    @Provides
    @Singleton
    fun provideRemoteConfigManager(): RemoteConfigManager =
        FirebaseConfigManager(FirebaseRemoteConfig.getInstance())
}
