package com.elta.android.presentation.features.devices.info.di

import com.elta.android.common.di.scope.FragmentScope
import com.elta.android.presentation.features.devices.info.ui.adapter.DeviceInfoDelegatesFactory
import com.nullgr.core.adapter.AdapterDelegatesFactory
import com.nullgr.core.adapter.DiffCalculator
import com.nullgr.core.adapter.DynamicAdapter
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module(includes = [DeviceInfoModule.Declarations::class])
class DeviceInfoModule {

    @Module
    interface Declarations {
        @Binds
        @FragmentScope
        fun delegatesFactory(factory: DeviceInfoDelegatesFactory): AdapterDelegatesFactory
    }

    @Provides
    @FragmentScope
    fun dynamicAdapter(
        factory: AdapterDelegatesFactory,
        diffCalculator: DiffCalculator
    ): DynamicAdapter = DynamicAdapter(factory, diffCalculator)
}