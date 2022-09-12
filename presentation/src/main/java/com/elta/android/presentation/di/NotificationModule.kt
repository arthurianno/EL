package com.elta.android.presentation.di

import com.elta.android.presentation.core.notification.NotificationHelper
import com.elta.android.presentation.core.notification.NotificationSource
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class NotificationModule {

    @Binds
    @Singleton
    abstract fun provideNotificationModule(source: NotificationHelper): NotificationSource
}
