package com.elta.android.presentation.features.shops.map.di

import com.elta.android.common.di.scope.FragmentScope
import com.elta.android.presentation.features.shops.map.ui.adapter.ShopDelegatesFactory
import com.nullgr.core.adapter.AdapterDelegatesFactory
import com.nullgr.core.adapter.DiffCalculator
import com.nullgr.core.adapter.DynamicAdapter
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module(includes = [ShopsMapModule.Declarations::class])
class ShopsMapModule {

    @Module
    interface Declarations {
        @Binds
        @FragmentScope
        fun delegatesFactory(factory: ShopDelegatesFactory): AdapterDelegatesFactory
    }

    @Provides
    fun dynamicAdapter(
        factory: AdapterDelegatesFactory,
        calculator: DiffCalculator
    ): DynamicAdapter = DynamicAdapter(factory, calculator)
}
