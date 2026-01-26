package com.elta.android.presentation.di

import com.elta.android.domain.features.devices.GlucoseEventsNotifier
import com.elta.android.presentation.core.notifier.GlucoseEventsNotifierImpl
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class NotifierModule {

    @Binds
    @Singleton
    abstract fun bindGlucoseEventsNotifier(impl: GlucoseEventsNotifierImpl): GlucoseEventsNotifier
}

