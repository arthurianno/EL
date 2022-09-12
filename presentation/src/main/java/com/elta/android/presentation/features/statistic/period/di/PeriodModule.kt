package com.elta.android.presentation.features.statistic.period.di

import androidx.recyclerview.widget.RecyclerView
import com.elta.android.common.di.scope.FragmentScope
import dagger.Module
import dagger.Provides

@Module
class PeriodModule {

    @Provides
    @FragmentScope
    fun viewPool(): RecyclerView.RecycledViewPool = RecyclerView.RecycledViewPool()
}
