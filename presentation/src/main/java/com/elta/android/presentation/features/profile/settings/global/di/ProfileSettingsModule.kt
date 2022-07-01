package com.elta.android.presentation.features.profile.settings.global.di

import com.elta.android.common.di.scope.FragmentScope
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.ProfileSettingsDelegatesFactory
import com.nullgr.core.adapter.AdapterDelegatesFactory
import com.nullgr.core.adapter.DiffCalculator
import com.nullgr.core.adapter.DynamicAdapter
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module(includes = [ProfileSettingsModule.Declarations::class])
class ProfileSettingsModule {

    @Module
    interface Declarations {
        @Binds
        @FragmentScope
        fun delegatesFactory(factory: ProfileSettingsDelegatesFactory): AdapterDelegatesFactory
    }

    @Provides
    @FragmentScope
    fun dynamicAdapter(
        factory: AdapterDelegatesFactory,
        diffCalculator: DiffCalculator
    ): DynamicAdapter = DynamicAdapter(factory, diffCalculator)
}
