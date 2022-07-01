package com.elta.android.presentation.features.onboaring.di

import com.elta.android.common.di.scope.FragmentScope
import com.elta.android.presentation.features.onboaring.ui.adapter.OnBoardingDelegatesFactory
import com.nullgr.core.adapter.AdapterDelegatesFactory
import com.nullgr.core.adapter.DiffCalculator
import com.nullgr.core.adapter.DynamicAdapter
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module(includes = [OnBoardingModule.Declarations::class])
class OnBoardingModule {

    @Module
    interface Declarations {
        @Binds
        @FragmentScope
        fun delegatesFactory(factory: OnBoardingDelegatesFactory): AdapterDelegatesFactory
    }

    @Provides
    @FragmentScope
    fun dynamicAdapter(
        factory: AdapterDelegatesFactory,
        diffCalculator: DiffCalculator
    ): DynamicAdapter = DynamicAdapter(factory, diffCalculator)
}
