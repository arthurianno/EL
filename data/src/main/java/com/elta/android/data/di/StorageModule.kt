package com.elta.android.data.di

import com.elta.android.data.features.common.storage.DbSyncStorage
import com.elta.android.data.features.common.storage.LocalPreferencesHolder
import com.elta.android.data.features.common.storage.LocalUserHolder
import com.elta.android.data.features.common.storage.PreferencesHolder
import com.elta.android.data.features.common.storage.SyncStorage
import com.elta.android.data.features.common.storage.UserHolder
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
interface StorageModule {

    @Binds
    @Singleton
    fun bindSyncStorage(storage: DbSyncStorage): SyncStorage

    @Binds
    @Singleton
    fun bindUserHolder(holder: LocalUserHolder): UserHolder

    @Binds
    @Singleton
    fun bindPreferencesHolder(holder: LocalPreferencesHolder): PreferencesHolder
}
