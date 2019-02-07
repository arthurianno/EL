package com.elta.android.data.di

import android.content.Context
import com.elta.android.data.features.sale_points.cache.DbSalePointsCache
import com.elta.android.data.features.sale_points.cache.SalePointsCache
import com.elta.android.data.features.sale_points.cache.dto.MyObjectBox
import dagger.Binds
import dagger.Module
import dagger.Provides
import io.objectbox.BoxStore
import javax.inject.Singleton

@Module(includes = [CacheModule.Declarations::class])
class CacheModule(context: Context) {

    private val boxStore = MyObjectBox.builder().androidContext(context).build()

    @Module
    interface Declarations {
        @Binds
        @Singleton
        fun bindSalePointsCache(cache: DbSalePointsCache): SalePointsCache
    }

    @Provides
    @Singleton
    fun provideBoxStore(): BoxStore = boxStore
}