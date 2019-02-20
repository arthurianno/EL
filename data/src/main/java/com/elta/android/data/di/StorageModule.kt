package com.elta.android.data.di

import com.elta.android.data.features.common.storage.LocalSyncStorage
import com.elta.android.data.features.common.storage.SyncStorage
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class StorageModule {

    @Binds
    @Singleton
    abstract fun bindSyncStorage(storage: LocalSyncStorage): SyncStorage
}