package com.elta.android.presentation.features.sync.connect.base.di

import com.elta.android.common.di.scope.FragmentScope
import com.elta.android.presentation.features.sync.connect.base.ui.adapter.DeviceDelegatesFactory
import com.nullgr.core.adapter.AdapterDelegatesFactory
import com.nullgr.core.adapter.DiffCalculator
import com.nullgr.core.adapter.DynamicAdapter
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module(includes = [ConnectDeviceModule.Declarations::class])
class ConnectDeviceModule {

    @Module
    interface Declarations {
        @Binds
        @FragmentScope
        fun delegatesFactory(factory: DeviceDelegatesFactory): AdapterDelegatesFactory
    }

    @Provides
    @FragmentScope
    fun dynamicAdapter(
        factory: AdapterDelegatesFactory,
        calculator: DiffCalculator
    ): DynamicAdapter = DynamicAdapter(factory, calculator)
}