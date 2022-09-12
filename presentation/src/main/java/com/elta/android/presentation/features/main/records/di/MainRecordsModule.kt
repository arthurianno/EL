package com.elta.android.presentation.features.main.records.di

import androidx.recyclerview.widget.RecyclerView
import com.elta.android.common.di.scope.FragmentScope
import dagger.Module
import dagger.Provides

@Module()
class MainRecordsModule {

    @Provides
    @FragmentScope
    fun viewPool(): RecyclerView.RecycledViewPool = RecyclerView.RecycledViewPool()
}
