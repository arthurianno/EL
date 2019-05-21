package com.elta.android.data.di

import com.elta.android.data.features.auth.storage.EmailDataStorage
import com.elta.android.data.features.auth.storage.EmailStorage
import com.elta.android.data.features.common.storage.DbSyncStorage
import com.elta.android.data.features.common.storage.LocalUserHolder
import com.elta.android.data.features.common.storage.SyncStorage
import com.elta.android.data.features.common.storage.UserHolder
import com.elta.android.data.features.feedback.storage.FeedbackDataStorage
import com.elta.android.data.features.feedback.storage.FeedbackStorage
import com.elta.android.data.features.user.storage.OnboardingDataStorage
import com.elta.android.data.features.user.storage.OnboardingStorage
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class StorageModule {

    @Binds
    @Singleton
    abstract fun bindSyncStorage(storage: DbSyncStorage): SyncStorage

    @Binds
    @Singleton
    abstract fun bindUserHolder(holder: LocalUserHolder): UserHolder

    @Binds
    @Singleton
    abstract fun bindFeedbackStorage(storage: FeedbackDataStorage): FeedbackStorage

    @Binds
    @Singleton
    abstract fun bindOnboardingStorage(storage: OnboardingDataStorage): OnboardingStorage

    @Binds
    @Singleton
    abstract fun bindEmailStorage(storage: EmailDataStorage): EmailStorage
}