package com.elta.android.presentation.features.profile.main.di

import android.support.v7.widget.RecyclerView
import com.elta.android.common.di.scope.FragmentScope
import com.elta.android.presentation.features.profile.main.ui.adapter.MainProfileDelegatesFactory
import com.nullgr.core.adapter.AdapterDelegatesFactory
import com.nullgr.core.adapter.DiffCalculator
import com.nullgr.core.adapter.DynamicAdapter
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module(includes = [MainProfileModule.Declarations::class])
class MainProfileModule {

    @Module
    interface Declarations {
        @Binds
        @FragmentScope
        fun delegatesFactory(factory: MainProfileDelegatesFactory): AdapterDelegatesFactory
    }

    @Provides
    @FragmentScope
    fun dynamicAdapter(
            factory: AdapterDelegatesFactory,
            diffCalculator: DiffCalculator
    ): DynamicAdapter = DynamicAdapter(factory, diffCalculator)

    @Provides
    @FragmentScope
    fun viewPool(): RecyclerView.RecycledViewPool = RecyclerView.RecycledViewPool()
}