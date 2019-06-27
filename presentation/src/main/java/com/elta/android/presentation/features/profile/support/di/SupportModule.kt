package com.elta.android.presentation.features.profile.support.di

import com.elta.android.common.di.scope.FragmentScope
import com.elta.android.presentation.features.profile.support.ui.adapter.SupportDelegatesFactory
import com.nullgr.core.adapter.AdapterDelegatesFactory
import com.nullgr.core.adapter.DiffCalculator
import com.nullgr.core.adapter.DynamicAdapter
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module(includes = [SupportModule.Declarations::class])
class SupportModule {

    @Module
    interface Declarations {
        @Binds
        @FragmentScope
        fun delegatesFactory(factory: SupportDelegatesFactory): AdapterDelegatesFactory
    }

    @Provides
    @FragmentScope
    fun dynamicAdapter(
        factory: AdapterDelegatesFactory,
        diffCalculator: DiffCalculator
    ): DynamicAdapter = DynamicAdapter(factory, diffCalculator)
}