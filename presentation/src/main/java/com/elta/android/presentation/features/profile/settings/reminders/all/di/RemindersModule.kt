package com.elta.android.presentation.features.profile.settings.reminders.all.di

import com.elta.android.common.di.scope.FragmentScope
import com.elta.android.presentation.features.profile.settings.reminders.all.ui.adapter.RemindersDelegatesFactory
import com.nullgr.core.adapter.AdapterDelegatesFactory
import com.nullgr.core.adapter.DiffCalculator
import com.nullgr.core.adapter.DynamicAdapter
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module(includes = [RemindersModule.Declarations::class])
class RemindersModule {

    @Module
    interface Declarations {
        @Binds
        @FragmentScope
        fun delegatesFactory(factory: RemindersDelegatesFactory): AdapterDelegatesFactory
    }

    @Provides
    @FragmentScope
    fun dynamicAdapter(
        factory: AdapterDelegatesFactory,
        diffCalculator: DiffCalculator
    ): DynamicAdapter = DynamicAdapter(factory, diffCalculator)
}