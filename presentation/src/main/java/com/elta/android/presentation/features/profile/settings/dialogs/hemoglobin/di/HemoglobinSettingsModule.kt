package com.elta.android.presentation.features.profile.settings.dialogs.hemoglobin.di

import com.elta.android.common.di.scope.FragmentScope
import com.elta.android.presentation.features.profile.settings.dialogs.hemoglobin.ui.adapter.HemoglobinEventsDelegatesFactory
import com.nullgr.core.adapter.AdapterDelegatesFactory
import com.nullgr.core.adapter.DiffCalculator
import com.nullgr.core.adapter.DynamicAdapter
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module(includes = [HemoglobinSettingsModule.Declarations::class])
class HemoglobinSettingsModule {

    @Module
    interface Declarations {
        @Binds
        @FragmentScope
        fun delegatesFactory(factory: HemoglobinEventsDelegatesFactory): AdapterDelegatesFactory
    }

    @Provides
    @FragmentScope
    fun dynamicAdapter(
        factory: AdapterDelegatesFactory,
        diffCalculator: DiffCalculator
    ): DynamicAdapter = DynamicAdapter(factory, diffCalculator)
}