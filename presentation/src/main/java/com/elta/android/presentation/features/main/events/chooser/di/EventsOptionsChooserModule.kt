package com.elta.android.presentation.features.main.events.chooser.di

import com.elta.android.common.di.scope.FragmentScope
import com.elta.android.presentation.features.main.events.chooser.ui.adapter.EventsOptionsChooserDelegatesFactory
import com.nullgr.core.adapter.AdapterDelegatesFactory
import com.nullgr.core.adapter.DiffCalculator
import com.nullgr.core.adapter.DynamicAdapter
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module(includes = [EventsOptionsChooserModule.Declarations::class])
class EventsOptionsChooserModule {

    @Module
    interface Declarations {
        @Binds
        @FragmentScope
        fun delegatesFactory(factory: EventsOptionsChooserDelegatesFactory): AdapterDelegatesFactory
    }

    @Provides
    @FragmentScope
    fun dynamicAdapter(
        factory: AdapterDelegatesFactory,
        diffCalculator: DiffCalculator
    ): DynamicAdapter = DynamicAdapter(factory, diffCalculator)
}
